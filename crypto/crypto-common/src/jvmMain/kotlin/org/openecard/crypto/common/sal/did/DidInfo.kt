/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
package org.openecard.crypto.common.sal.did

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.ECardConstants
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateInputType
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateOutputType
import org.openecard.common.util.ByteUtils
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.xml.parsers.ParserConfigurationException

private val LOG = KotlinLogging.logger {}

/**
 *
 * @author Tobias Wich
 */
class DidInfo {

	private val didInfos: DidInfos
	private val application: ByteArray
	private val didTarget: TargetNameType
	private var didScope: DIDScopeType?
	private var pin: CharArray?

	@get:Throws(CertificateException::class)
	private val certFactory: CertificateFactory by lazy {
		CertificateFactory.getInstance("X.509")
	}

	internal constructor(didInfos: DidInfos, application: ByteArray, didName: String, pin: CharArray?) {
		this.didInfos = didInfos
		this.application = ByteUtils.clone(application)
		this.didTarget = TargetNameType()
		this.didTarget.setDIDName(didName)
		if (pin != null) {
			this.pin = pin.clone()
		} else {
			this.pin = null
		}

		// we are in the same application, so set it to null and get the real value
		this.didScope = null
		try {
			val didStruct: DIDStructureType? = this.DID
			if (didStruct != null) {
				this.didScope = didStruct.getDIDScope()
			}
		} catch (_: WSHelper.WSException) {
			// too bad ;-)
			LOG.warn { "Failed to retrieve DID qualifier." }
		}
	}

	internal constructor(didInfos: DidInfos, application: ByteArray, didStruct: DIDStructureType, pin: CharArray?) {
		this.didInfos = didInfos
		this.application = application.copyOf()
		this.didTarget = TargetNameType()
		this.didTarget.setDIDName(didStruct.getDIDName())
		this.didScope = didStruct.getDIDScope()
		if (pin != null) {
			this.pin = pin.clone()
		} else {
			this.pin = null
		}
	}

	fun setPin(pin: CharArray?) {
		if (pin != null) {
			this.pin?.fill(' ')
			this.pin = pin.clone()
		} else {
			this.pin = null
		}
	}

	@get:Throws(WSHelper.WSException::class)
	val ACL: AccessControlListType
		get() {
			val req = ACLList()
			req.setConnectionHandle(didInfos.getHandle(application))
			req.setTargetName(didTarget)

			val res = didInfos.dispatcher.safeDeliver(req) as ACLListResponse
			checkResult<ACLListResponse>(res)

			return res.getTargetACL()
		}

	@get:Throws(WSHelper.WSException::class)
	val DID: DIDStructureType
		get() {
			val req = DIDGet()
			req.setConnectionHandle(didInfos.getHandle(application))
			req.setDIDName(this.didName)
			req.setDIDScope(didScope)

			val res = didInfos.dispatcher.safeDeliver(req) as DIDGetResponse
			checkResult<DIDGetResponse>(res)

			return res.getDIDStructure()
		}

	val didName: String by lazy { didTarget.getDIDName() }

	@get:Throws(WSHelper.WSException::class)
	val protocol = this.DID.getDIDMarker().getProtocol()

	@get:Throws(WSHelper.WSException::class)
	val isAuthenticated = this.DID.isAuthenticated

	@get:Throws(
		WSHelper.WSException::class,
		SecurityConditionUnsatisfiable::class
	)
	val missingDids: List<DIDStructureType>
		get() {
			val resolver = ACLResolver(didInfos.dispatcher, didInfos.getHandle(application))
			val missingDids = resolver.getUnsatisfiedDIDs(didTarget, this.ACL.getAccessRule())
			return missingDids
		}

	@get:Throws(
		WSHelper.WSException::class,
		SecurityConditionUnsatisfiable::class,
		NoSuchDid::class
	)
	val missingDidInfos: MutableList<DidInfo>
		get() {
			val result = ArrayList<DidInfo>()
			for (didStruct in this.missingDids) {
				result.add(didInfos.getDidInfo(didStruct.getDIDName()))
			}
			return result
		}

	@get:Throws(WSHelper.WSException::class)
	val isPinSufficient: Boolean
		get() {
			try {
				val missingDids = this.missingDids
				// check if there is anything other than a pin did in the list
				for (missingDid in missingDids) {
					val infoObj: DidInfo = if (missingDid.getDIDScope() == DIDScopeType.GLOBAL) {
						didInfos.getDidInfo(missingDid.getDIDName())
					} else {
						didInfos.getDidInfo(application, missingDid.getDIDName())
					}
					if (!infoObj.isPinDid) {
						return false
					}
				}
				// no PIN DID in missing list
				return true
			} catch (_: SecurityConditionUnsatisfiable) {
				// not satisfiable means pin does not suffice
				return false
			} catch (ex: NoSuchDid) {
				val msg = "DID referenced in CIF could not be resolved."
				LOG.error(ex) { msg }
				throw createException(
					makeResultError(
						ECardConstants.Minor.App.INT_ERROR,
						msg
					)
				)
			}
		}

	@Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class)
	fun needsPin(): Boolean {
		try {
			val missingDids = this.missingDids
			// check if there is a pin did in the list
			for (missingDid in missingDids) {
				val infoObj = if (missingDid.getDIDScope() == DIDScopeType.GLOBAL) {
					didInfos.getDidInfo(missingDid.getDIDName())
				} else {
					didInfos.getDidInfo(application, missingDid.getDIDName())
				}
				if (infoObj.isPinDid) {
					return true
				}
			}
			// no PIN DID in missing list
			return false
		} catch (ex: NoSuchDid) {
			val msg = "DID referenced in CIF could not be resolved."
			LOG.error(ex) { msg }
			throw createException(makeResultError(ECardConstants.Minor.App.INT_ERROR, msg))
		}
	}


	@get:Throws(WSHelper.WSException::class)
	val rawMarker: DIDAbstractMarkerType
		get() = this.DID.getDIDMarker()


	@get:Throws(WSHelper.WSException::class)
	val isPinDid: Boolean
		get() = "urn:oid:1.3.162.15480.3.0.9" == this.protocol

	@get:Throws(WSHelper.WSException::class)
	val pinCompareMarker: PinCompareMarkerType
		get() = PinCompareMarkerType(this.DID.getDIDMarker())

	@JvmOverloads
	@Throws(WSHelper.WSException::class)
	fun enterPin(pin: CharArray? = this.pin): BigInteger {
		check(this.isPinDid) { "Enter PIN called for a DID which is not a PIN DID." }

		try {
			var data = PinCompareDIDAuthenticateInputType()
			data.setProtocol(this.protocol)
			// add PIN
			if (pin != null && pin.isNotEmpty()) {
				val builder = PINCompareDIDAuthenticateInputType(data)
				builder.setPIN(pin)
				data = builder.getAuthDataType()
				builder.setPIN(null)
			}

			val req = DIDAuthenticate()
			req.setConnectionHandle(didInfos.getHandle(application))
			req.setDIDName(didTarget.getDIDName())
			req.setDIDScope(this.didScope)
			req.setAuthenticationProtocolData(data)

			val res = didInfos.dispatcher.safeDeliver(req) as DIDAuthenticateResponse
			checkResult<DIDAuthenticateResponse>(res)

			// check retry counter
			val protoData = PINCompareDIDAuthenticateOutputType(res.getAuthenticationProtocolData())
			val retryCounter = protoData.retryCounter
			return retryCounter
		} catch (ex: ParserConfigurationException) {
			val msg = "Unexpected protocol data received in PIN Compare output."
			LOG.error(ex) { msg }
			throw IllegalStateException(msg)
		}
	}


	@get:Throws(WSHelper.WSException::class)
	val isCryptoDid: Boolean
		get() = "urn:oid:1.3.162.15480.3.0.25" == this.protocol

	@get:Throws(WSHelper.WSException::class)
	val genericCryptoMarker: CryptoMarkerType
		get() = CryptoMarkerType(this.DID.getDIDMarker())

	@Throws(WSHelper.WSException::class)
	fun hash(data: ByteArray): ByteArray {
		check(this.isCryptoDid) { "Hash called for a DID which is not a Generic Crypto DID." }

		val hashReq = Hash()
		hashReq.setMessage(data)
		hashReq.setDIDName(didTarget.getDIDName())
		hashReq.setDIDScope(DIDScopeType.LOCAL)
		hashReq.setConnectionHandle(didInfos.getHandle(application))
		val res = didInfos.dispatcher.safeDeliver(hashReq) as HashResponse
		checkResult<HashResponse>(res)

		val digest = res.getHash()
		return digest
	}

	@Throws(WSHelper.WSException::class)
	fun sign(data: ByteArray): ByteArray {
		check(this.isCryptoDid) { "Sign called for a DID which is not a Generic Crypto DID." }

		val sign = Sign()
		sign.setMessage(data)
		sign.setDIDName(didTarget.getDIDName())
		sign.setDIDScope(DIDScopeType.LOCAL)
		sign.setConnectionHandle(didInfos.getHandle(application))
		val res = didInfos.dispatcher.safeDeliver(sign) as SignResponse
		checkResult<SignResponse>(res)

		val sig = res.getSignature()
		return sig
	}

	@get:Throws(WSHelper.WSException::class)
	val relatedDataSets: List<DataSetInfo>
		get() {
			try {
				val result = ArrayList<DataSetInfo?>()
				val foundDataSets: MutableSet<String> = HashSet()

				if (this.isCryptoDid) {
					val m = this.genericCryptoMarker
					for (cert in m.certificateRefs) {
						val datasetName = cert.getDataSetName()

						//String dsiName = cert.getDSIName();

						// add if it is not already present in the result list
						if (!foundDataSets.contains(datasetName)) {
							val ds = didInfos.getDataSetInfo(datasetName)
							result.add(ds)
							foundDataSets.add(datasetName)
						}
					}
				}

				return Collections.unmodifiableList<DataSetInfo?>(result)
			} catch (ex: NoSuchDataSet) {
				val msg = "DataSet referenced in CIF could not be resolved."
				LOG.error(ex) { msg }
				throw createException(
					makeResultError(
						ECardConstants.Minor.App.INT_ERROR,
						msg
					)
				)
			}
		}

	@get:Throws(
		WSHelper.WSException::class,
		CertificateException::class,
		SecurityConditionUnsatisfiable::class,
		NoSuchDid::class
	)
	@get:Synchronized
	val relatedCertificateChain: List<X509Certificate> by lazy {
		if (this.isCryptoDid) {
			// read certs from card
			var allCertsRead = true
			val rawCerts = ArrayList<ByteArray>()
			for (dsi in this.relatedDataSets) {
				if (dsi.isPinSufficient) {
					dsi.connectApplication()
					dsi.authenticate()
					val data = dsi.read()
					rawCerts.add(data)
				} else {
					allCertsRead = false
					break
				}
			}

			// convert certs
			if (allCertsRead) {
				parseCerts(rawCerts)
			} else {
				throw CertificateException("No readable certificates available.")
			}
		} else {
			listOf()
		}
	}


	@Throws(CertificateException::class)
	private fun parseCerts(certsData: MutableList<ByteArray>): List<X509Certificate> {
		val allCerts: MutableList<Certificate> = ArrayList<Certificate>()

		for (nextBlob in certsData) {
			allCerts.addAll(this.certFactory.generateCertificates(ByteArrayInputStream(nextBlob)))
		}

		// get first cert and build path
		return if (!allCerts.isEmpty()) {
			@Suppress("UNCHECKED_CAST")
			this.certFactory.generateCertPath(allCerts).certificates as List<X509Certificate>
		} else {
			listOf<X509Certificate>()
		}
	}

	@Throws(WSHelper.WSException::class)
	fun connectApplication() {
		didInfos.connectApplication(application)
	}

	@Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class, NoSuchDid::class)
	fun authenticateMissing() {
		for (nextDid in this.missingDidInfos) {
			if (nextDid.isPinDid) {
				nextDid.enterPin(pin)
			} else {
				throw SecurityConditionUnsatisfiable("Only PIN DIDs are supported at the moment.")
			}
		}
	}

}
