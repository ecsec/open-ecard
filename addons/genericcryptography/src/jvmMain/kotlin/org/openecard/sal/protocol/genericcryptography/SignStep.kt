/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.sal.protocol.genericcryptography

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.bouncycastle.asn1.*
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.openecard.bouncycastle.asn1.x509.DigestInfo
import org.openecard.common.ECardConstants
import org.openecard.common.ECardException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResult
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.apdu.GetResponse
import org.openecard.common.apdu.InternalAuthenticate
import org.openecard.common.apdu.ManageSecurityEnvironment
import org.openecard.common.apdu.common.APDUTemplateException
import org.openecard.common.apdu.common.BaseTemplateContext
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.CardCommandTemplate
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.apdu.utils.SALErrorUtils
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.Assert
import org.openecard.common.sal.exception.IncorrectParameterException
import org.openecard.common.sal.util.SALUtils
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.SignatureAlgorithms.Companion.fromAlgId
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.CryptoMarkerType
import org.openecard.sal.protocol.genericcryptography.apdu.PSOComputeDigitalSignature
import org.openecard.sal.protocol.genericcryptography.apdu.PSOHash
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.math.BigInteger
import java.util.*

private val LOG = KotlinLogging.logger { }

/**
 * Implements the Sign step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.9.
 *
 * @param dispatcher Dispatcher
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class SignStep(
	private val dispatcher: Dispatcher,
) : ProtocolStep<Sign?, SignResponse?> {
	override fun getFunctionType(): FunctionType = FunctionType.Sign

	override fun perform(
		sign: Sign?,
		internalData: Map<String, Any>,
	): SignResponse {
		var response: SignResponse =
			WSHelper.makeResponse<Class<SignResponse>, SignResponse>(
				iso.std.iso_iec._24727.tech.schema.SignResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)

		try {
			val connectionHandle = SALUtils.getConnectionHandle(sign)
			val didName = SALUtils.getDIDName(sign)
			val cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle)
			val didStructure = SALUtils.getDIDStructure(sign, didName, cardStateEntry, connectionHandle)
			val cryptoMarker = CryptoMarkerType(didStructure.didMarker)

			val slotHandle = connectionHandle.slotHandle
			val applicationID = connectionHandle.cardApplication
			Assert.securityConditionDID(
				cardStateEntry.cardEntry,
				applicationID,
				didName,
				CryptographicServiceActionName.SIGN,
			)

			val message = sign!!.message
			val keyReference = cryptoMarker.cryptoKeyInfo!!.keyRef.keyRef
			val algorithmIdentifier = cryptoMarker.algorithmInfo!!.cardAlgRef
			val hashRef = cryptoMarker.algorithmInfo!!.hashAlgRef
			val hashInfo = cryptoMarker.hashGenerationInfo

			// add DigestInfo for RSA-SSA if hashing is not to be done on card
			val hashValue = prepareMessage(message, hashInfo, cryptoMarker.algorithmInfo!!)

			if (didStructure.didScope == DIDScopeType.LOCAL) {
				keyReference[0] = (0x80 or keyReference[0].toInt()).toByte()
			}

			response =
				if (cryptoMarker.getSignatureGenerationInfo() != null) {
					performSignature(
						cryptoMarker,
						keyReference,
						algorithmIdentifier,
						hashValue,
						slotHandle,
						hashRef,
						hashInfo,
					)
				} else {
					// assuming that legacySignatureInformation exists
					val templateContext =
						BaseTemplateContext().apply {
							put(HASH_TO_SIGN, hashValue)
							put(KEY_REFERENCE, keyReference)
							put(ALGORITHM_IDENTIFIER, algorithmIdentifier)
							put(HASHALGORITHM_REFERENCE, hashRef)
						}
					performLegacySignature(cryptoMarker, connectionHandle, templateContext)
				}
		} catch (e: ECardException) {
			response.result = e.result
		} catch (e: Exception) {
			LOG.warn(e) { e.message }
			response.result = makeResult(e)
		}

		return response
	}

	@Throws(UnsupportedAlgorithmException::class, IOException::class)
	private fun prepareMessage(
		hash: ByteArray,
		hashInfo: HashGenerationInfoType?,
		algorithmInfo: AlgorithmInfoType,
	): ByteArray {
		val algorithm = fromAlgId(algorithmInfo.algorithmIdentifier.algorithm)
		if (algorithm.isRsaSsa) {
			// Cards don't build the DigestInfo struct, so we have to do it here
			val hashAlgId = algorithm.hashAlg!!.oid
			val digestInfo = DigestInfo(AlgorithmIdentifier(hashAlgId, DERNull.INSTANCE), hash)
			return digestInfo.getEncoded(ASN1Encoding.DER)
		} else {
			return hash
		}
	}

	/**
	 * This method performs the signature creation according to BSI TR-03112 part 7.
	 *
	 * @param cryptoMarker The [CryptoMarkerType] containing the SignatureCreationInfo for creating the signature.
	 * @param keyReference A byte array containing the reference of the key to use.
	 * @param algorithmIdentifier A byte array containing the identifier of the signing algorithm.
	 * @param message The message to sign.
	 * @param slotHandle The slotHandle identifying the card.
	 * @param hashRef The variable contains the reference for the hash algorithm which have to be used.
	 * @param hashInfo A HashGenerationInfo object which indicates how the hash computation is to perform.
	 * @return A [SignResponse] object containing the signature of the **message**.
	 * @throws TLVException Thrown if the TLV creation for the key identifier or algorithm identifier failed.
	 * @throws IncorrectParameterException Thrown if the SignatureGenerationInfo does not contain PSO_CDS or INT_AUTH
	 * after an MSE_KEY command.
	 * @throws APDUException Thrown if one of the command to create the signature failed.
	 * @throws WSHelper.WSException Thrown if the checkResults method of WSHelper failed.
	 */
	@Throws(TLVException::class, IncorrectParameterException::class, APDUException::class, WSHelper.WSException::class)
	private fun performSignature(
		cryptoMarker: CryptoMarkerType,
		keyReference: ByteArray?,
		algorithmIdentifier: ByteArray?,
		message: ByteArray?,
		slotHandle: ByteArray?,
		hashRef: ByteArray?,
		hashInfo: HashGenerationInfoType?,
	): SignResponse {
		val response: SignResponse =
			WSHelper.makeResponse<Class<SignResponse>, SignResponse>(
				iso.std.iso_iec._24727.tech.schema.SignResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)

		val tagAlgorithmIdentifier =
			TLV().apply {
				setTagNumWithClass(CARD_ALG_REF)
				value = algorithmIdentifier
			}

		val tagKeyReference =
			TLV().apply {
				setTagNumWithClass(KEY_REFERENCE_PRIVATE_KEY)
				value = keyReference
			}

		var cmdAPDU: CardCommandAPDU? = null
		var responseAPDU: CardResponseAPDU? = null

		val signatureGenerationInfo = cryptoMarker.getSignatureGenerationInfo()
		for (command in signatureGenerationInfo!!) {
			val signGenInfo = signatureGenerationInfo.toSet()

			cmdAPDU =
				when (command) {
					"MSE_KEY" -> {
						val mseData = tagKeyReference.toBER()

						if (signGenInfo.contains("PSO_CDS")) {
							ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.DST, mseData)
						} else if (signGenInfo.contains("INT_AUTH") && !signGenInfo.contains("PSO_CDS")) {
							ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.AT, mseData)
						} else {
							val msg = "The command 'MSE_KEY' followed by 'INT_AUTH' and 'PSO_CDS' is currently not supported."
							LOG.error { msg }
							throw IncorrectParameterException(msg)
						}
					}

					"PSO_CDS" -> {
						PSOComputeDigitalSignature(message!!, BLOCKSIZE)
					}

					"INT_AUTH" -> {
						InternalAuthenticate(message, BLOCKSIZE)
					}
					"MSE_RESTORE" -> {
						ManageSecurityEnvironment.Restore(ManageSecurityEnvironment.DST)
					}
					"MSE_HASH" -> {
						val mseDataTLV =
							TLV().apply {
								setTagNumWithClass(0x80.toByte())
								value = hashRef
							}
						ManageSecurityEnvironment.Set(SET_COMPUTATION, ManageSecurityEnvironment.HT).apply {
							setData(mseDataTLV.toBER())
						}
					}
					"PSO_HASH" -> {
						if (hashInfo == HashGenerationInfoType.LAST_ROUND_ON_CARD ||
							hashInfo == HashGenerationInfoType.NOT_ON_CARD
						) {
							PSOHash(PSOHash.P2_SET_HASH_OR_PART, message)
						} else {
							PSOHash(PSOHash.P2_HASH_MESSAGE, message)
						}
					}
					"MSE_DS" -> {
						val mseData = tagAlgorithmIdentifier.toBER()
						ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.DST, mseData)
					}
					"MSE_KEY_DS" -> {
						val mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER())
						ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.DST, mseData)
					}
					"MSE_INT_AUTH" -> {
						val mseData = tagKeyReference.toBER()
						ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.AT, mseData)
					}
					"MSE_KEY_INT_AUTH" -> {
						val mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER())
						ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.AT, mseData)
					}
					else -> {
						val msg = "The signature generation command '$command' is unknown."
						throw IncorrectParameterException(msg)
					}
				}

			responseAPDU = cmdAPDU.transmit(dispatcher, slotHandle, mutableListOf<ByteArray?>())
		}

		var signedMessage = responseAPDU!!.data

		// check if further response data is available
		while (responseAPDU!!.trailer[0] == 0x61.toByte()) {
			val getResponseData = GetResponse()
			responseAPDU = getResponseData.transmit(dispatcher, slotHandle, mutableListOf<ByteArray?>())
			signedMessage =
				org.openecard.bouncycastle.util.Arrays
					.concatenate(signedMessage, responseAPDU.data)
		}

		if (!org.openecard.bouncycastle.util.Arrays.areEqual(
				responseAPDU.trailer,
				byteArrayOf(0x90.toByte(), 0x00.toByte()),
			)
		) {
			val minor = SALErrorUtils.getMinor(responseAPDU.trailer)
			response.setResult(makeResultError(minor, responseAPDU.statusMessage))
			return response
		}

		response.signature = signedMessage
		return response
	}

	/**
	 * The method performs the SignatureCreation if no standard commands are possible.
	 * This method creates a signature with APDUs which are not covered by the methods defined in TR-03112 part 7.
	 *
	 * @param cryptoMarker A [CryptoMarkerType] object containing the information about the creation of a signature
	 * in a legacy way.
	 * @param templateCTX A Map containing the context data for the evaluation of the template variables. This object
	 * contains per default the message to sign and the [TLVFunction].
	 * @return A [SignResponse] object containing the signature of the **message**.
	 * @throws APDUTemplateException Thrown if the evaluation of the [CardCommandTemplate] failed.
	 * @throws APDUException Thrown if one of the commands to execute failed.
	 * @throws WSHelper.WSException Thrown if the checkResult method of WSHelper failed.
	 */
	@Throws(APDUTemplateException::class, APDUException::class, WSHelper.WSException::class)
	private fun performLegacySignature(
		cryptoMarker: CryptoMarkerType,
		connectionHandle: ConnectionHandleType,
		templateCTX: BaseTemplateContext,
	): SignResponse {
		val response: SignResponse =
			WSHelper.makeResponse<Class<SignResponse>, SignResponse>(
				iso.std.iso_iec._24727.tech.schema.SignResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)
		val legacyCommands = cryptoMarker.getLegacySignatureGenerationInfo()
		var cmdAPDU: CardCommandAPDU?
		var responseAPDU: CardResponseAPDU? = null
		val slotHandle = connectionHandle.slotHandle
		var signedMessage: ByteArray

		for (next in legacyCommands!!) {
			if (next is CardCallTemplateType) {
				val cctt = next
				val template = CardCommandTemplate(cctt)
				cmdAPDU = template.evaluate(templateCTX)
				responseAPDU = cmdAPDU.transmit(dispatcher, slotHandle, mutableListOf<ByteArray?>())
			} else if (next is LegacySignatureGenerationType.APICommand) {
				sendAPICommand(connectionHandle, next)
			}
		}

		signedMessage = responseAPDU!!.data

		// check if further response data is available
		while (responseAPDU!!.trailer[0] == 0x61.toByte()) {
			val getResponseData =
				CardCommandAPDU(
					0x00.toByte(),
					0xC0.toByte(),
					0x00.toByte(),
					0x00.toByte(),
					responseAPDU.trailer[1],
				)
			responseAPDU = getResponseData.transmit(dispatcher, slotHandle, mutableListOf<ByteArray?>())
			signedMessage =
				org.openecard.bouncycastle.util.Arrays
					.concatenate(signedMessage, responseAPDU.getData())
		}

		if (!org.openecard.bouncycastle.util.Arrays.areEqual(
				responseAPDU.trailer,
				byteArrayOf(0x90.toByte(), 0x00.toByte()),
			)
		) {
			val minor = SALErrorUtils.getMinor(responseAPDU.trailer)
			response.setResult(makeResultError(minor, responseAPDU.statusMessage))
			return response
		}

		when (cryptoMarker.legacyOutputFormat) {
			"rawRS" -> signedMessage = encodeRawRS(signedMessage)
			null -> {}
			else ->
				LOG.warn { "Unsupport outputFormat=${cryptoMarker.legacyOutputFormat} specified in LegacySignatureGenerationInfo." }
		}

		response.setSignature(signedMessage)
		return response
	}

	private fun sendAPICommand(
		handle: ConnectionHandleType,
		cmd: LegacySignatureGenerationType.APICommand,
	) {
		// TODO: make this a utility function in common
		val callObj = cmd.apiCall.any[0]

		// set connection handle
		try {
			val func = callObj.javaClass.getMethod("setConnectionHandle", ConnectionHandleType::class.java)
			func.invoke(callObj, handle)
		} catch (ex: NoSuchMethodException) {
			// ignore as this is totally valid
		} catch (ex: IllegalAccessException) {
			LOG.warn(ex) { "Failed to execute setConnectionHandle." }
		} catch (ex: IllegalArgumentException) {
			LOG.warn(ex) { "Failed to execute setConnectionHandle." }
		} catch (ex: InvocationTargetException) {
			LOG.warn(ex) { "Failed to execute setConnectionHandle." }
		}

		// set slot handle
		try {
			val func = callObj.javaClass.getMethod("setSlotHandle", ByteArray::class.java)
			func.invoke(callObj, *arrayOf<Any?>(handle.slotHandle))
		} catch (ex: NoSuchMethodException) {
			// ignore as this is totally valid
		} catch (ex: IllegalAccessException) {
			LOG.warn(ex) { "Failed to execute setSlotHandle." }
		} catch (ex: IllegalArgumentException) {
			LOG.warn(ex) { "Failed to execute setSlotHandle." }
		} catch (ex: InvocationTargetException) {
			LOG.warn(ex) { "Failed to execute setSlotHandle." }
		}

		LOG.debug { "Sending API call." }
		val result = dispatcher.safeDeliver(callObj)
		// TODO: match against APIResponse objects
	}

	@Throws(WSHelper.WSException::class)
	private fun encodeRawRS(signature: ByteArray): ByteArray {
		try {
			LOG.info("Reencoding raw RS parameters as ECDSA signature.")
			val n = signature.size / 2
			val bytes = ByteArray(n)
			System.arraycopy(signature, 0, bytes, 0, n)
			val r = BigInteger(1, bytes)
			System.arraycopy(signature, n, bytes, 0, n)
			val s = BigInteger(1, bytes)

			val v =
				ASN1EncodableVector().apply {
					add(ASN1Integer(r))
					add(ASN1Integer(s))
				}

			return DERSequence(v).getEncoded(ASN1Encoding.DER)
		} catch (ex: IOException) {
			throw createException(
				makeResultError(
					ECardConstants.Minor.App.INT_ERROR,
					"Failed to reencode raw RS parameters as ECDSA signature.",
				),
			)
		}
	}

	companion object {
		// TODO extract the blocksize from somewhere
		private val BLOCKSIZE = 256.toByte()
		private val SET_COMPUTATION = 0x41.toByte()
		private val KEY_REFERENCE_PRIVATE_KEY = 0x84.toByte()
		private val CARD_ALG_REF = 0x80.toByte()

		private const val HASH_TO_SIGN = "hashToSign"
		private const val KEY_REFERENCE = "keyReference"
		private const val ALGORITHM_IDENTIFIER = "algorithmIdentifier"
		private const val HASHALGORITHM_REFERENCE = "hashAlgorithmReference"
	}
}
