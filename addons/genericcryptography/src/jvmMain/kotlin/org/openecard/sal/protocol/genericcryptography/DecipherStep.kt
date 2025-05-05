/****************************************************************************
 * Copyright (C) 2012-2014 HS Coburg.
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
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName
import iso.std.iso_iec._24727.tech.schema.DIDScopeType
import iso.std.iso_iec._24727.tech.schema.Decipher
import iso.std.iso_iec._24727.tech.schema.DecipherResponse
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.common.ECardException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.makeResult
import org.openecard.common.apdu.ManageSecurityEnvironment
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.Assert
import org.openecard.common.sal.util.SALUtils
import org.openecard.common.tlv.TLV
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.sal.did.CryptoMarkerType
import org.openecard.sal.protocol.genericcryptography.apdu.PSODecipher
import java.io.ByteArrayOutputStream
import java.math.BigInteger

private val logger = KotlinLogging.logger { }

/**
 * Implements the Decipher step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.6.
 *
 * @param dispatcher Dispatcher
 *
 * @author Dirk Petrautzki
 */
class DecipherStep(
	private val dispatcher: Dispatcher,
) : ProtocolStep<Decipher?, DecipherResponse?> {
	override fun getFunctionType(): FunctionType = FunctionType.Decipher

	override fun perform(
		request: Decipher?,
		internalData: Map<String, Any>?,
	): DecipherResponse? {
		val response: DecipherResponse =
			WSHelper.makeResponse<Class<DecipherResponse>, DecipherResponse>(
				iso.std.iso_iec._24727.tech.schema.DecipherResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)

		try {
			val connectionHandle = SALUtils.getConnectionHandle(request)
			val applicationID = connectionHandle.cardApplication
			val didName = SALUtils.getDIDName(request)
			val cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle)

			Assert.securityConditionDID(
				cardStateEntry.cardEntry,
				applicationID,
				didName,
				CryptographicServiceActionName.DECIPHER,
			)

			val didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle)
			val cryptoMarker = CryptoMarkerType(didStructure.didMarker)
			val keyReference = cryptoMarker.cryptoKeyInfo!!.keyRef.keyRef
			val algorithmIdentifier = cryptoMarker.algorithmInfo!!.getCardAlgRef()
			val slotHandle = connectionHandle.getSlotHandle()

			// TODO eGK specific requirement
			// See eGK specification, part 1, version 2.2.0, section 15.9.6.
			if (didStructure.didScope == DIDScopeType.LOCAL) {
				keyReference[0] = (0x80 or keyReference[0].toInt()).toByte()
			}

			val tagKeyReference =
				TLV().apply {
					tagNumWithClass = 0x84
					value = keyReference
				}
			val tagAlgorithmIdentifier =
				TLV().apply {
					tagNumWithClass = 0x80
					value = algorithmIdentifier
				}
			val mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER())

			var apdu: CardCommandAPDU = ManageSecurityEnvironment(0x41.toByte(), ManageSecurityEnvironment.CT, mseData)
			apdu.transmit(dispatcher, slotHandle)

			val ciphertext = request!!.cipherText
			val baos = ByteArrayOutputStream()
			val bitKeySize = cryptoMarker.cryptoKeyInfo!!.keySize
			val blocksize = bitKeySize.divide(BigInteger("8")).toInt()

			// check if the ciphertext length is divisible by the blocksize without rest
			if ((ciphertext.size % blocksize) != 0) {
				return WSHelper.makeResponse<Class<DecipherResponse>, DecipherResponse>(
					iso.std.iso_iec._24727.tech.schema.DecipherResponse::class.java,
					org.openecard.common.WSHelper.makeResultError(
						org.openecard.common.ECardConstants.Minor.App.INCORRECT_PARM,
						"The length of the ciphertext should be a multiple of the blocksize.",
					),
				)
			}

			// decrypt the ciphertext block for block
			var offset = 0
			while (offset < ciphertext.size) {
				val ciphertextblock = ByteUtils.copy(ciphertext, offset, blocksize)
				apdu = PSODecipher(ByteUtils.concatenate(PADDING_INDICATOR_BYTE, ciphertextblock), blocksize.toByte())
				val responseAPDU = apdu.transmit(dispatcher, slotHandle)
				baos.write(responseAPDU.getData())
				offset += blocksize
			}

			response.plainText = baos.toByteArray()
		} catch (e: ECardException) {
			response.setResult(e.result)
		} catch (e: Exception) {
			logger.error(e) { "Error in decipher step" }
			response.setResult(makeResult(e))
		}

		return response
	}

	companion object {
		private val PADDING_INDICATOR_BYTE = 0x00.toByte()
	}
}
