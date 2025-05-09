/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType
import org.openecard.addons.cg.ex.ParameterInvalid
import org.openecard.addons.cg.ex.PinBlocked
import org.openecard.addons.cg.ex.SlotHandleInvalid
import org.openecard.bouncycastle.asn1.ASN1Encoding
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.DERNull
import org.openecard.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.openecard.bouncycastle.asn1.x509.DigestInfo
import org.openecard.bouncycastle.asn1.x509.X509ObjectIdentifiers
import org.openecard.common.ECardConstants
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.ThreadTerminateException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.StringUtils
import org.openecard.crypto.common.HashAlgorithms
import org.openecard.crypto.common.SignatureAlgorithms
import org.openecard.crypto.common.SignatureAlgorithms.Companion.fromAlgId
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.NoSuchDid
import org.openecard.crypto.common.sal.did.TokenCache
import java.io.IOException
import java.security.InvalidParameterException
import java.util.concurrent.Semaphore

/**
 *
 * @author Tobias Wich
 */
class Signer(
	private val tokenCache: TokenCache,
	slotHandle: ByteArray?,
	private val didName: String,
	private val pin: CharArray?,
) {
	private val handle: ConnectionHandleType =
		ConnectionHandleType().apply {
			this.slotHandle = ByteUtils.clone(slotHandle)
		}

	@Throws(
		NoSuchDid::class,
		WSHelper.WSException::class,
		SecurityConditionUnsatisfiable::class,
		ParameterInvalid::class,
		SlotHandleInvalid::class,
		PinBlocked::class,
	)
	fun sign(data: ByteArray): ByteArray {
		val s: Semaphore = getLock(handle.ifdName)
		var acquired = false
		try {
			s.acquire()
			acquired = true
			// get crypto dids
			val didInfos = tokenCache.getInfo(pin, handle)
			val didInfo = didInfos.getDidInfo(didName)

			didInfo.connectApplication()
			didInfo.authenticateMissing()

			val cryptoMarker = didInfo.genericCryptoMarker
			val algUri = cryptoMarker.algorithmInfo!!.algorithmIdentifier.algorithm
			try {
				val alg = fromAlgId(algUri)

				// calculate hash if needed
				var digest = data
				if (alg.hashAlg != null &&
					cryptoMarker.hashGenerationInfo == HashGenerationInfoType.NOT_ON_CARD
				) {
					digest = didInfo.hash(digest)
				}

				// wrap hash in DigestInfo if needed
				if (alg == SignatureAlgorithms.CKM_RSA_PKCS) {
					try {
						val digestOid = getHashAlgOid(data)
						val di = DigestInfo(AlgorithmIdentifier(digestOid, DERNull.INSTANCE), digest)
						val sigMsg = di.getEncoded(ASN1Encoding.DER)
						digest = sigMsg
					} catch (ex: IOException) {
						val msg = "Error encoding DigestInfo object."
						val r = makeResultError(ECardConstants.Minor.App.INT_ERROR, msg)
						throw createException(r)
					} catch (ex: InvalidParameterException) {
						val msg = "Hash algorithm could not be determined for the given hash."
						val r = makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg)
						throw createException(r)
					}
				}

				val signature = didInfo.sign(digest)
				return signature
			} catch (ex: UnsupportedAlgorithmException) {
				val msg = "DID uses unsupported algorithm $algUri."
				throw createException(makeResultError(ECardConstants.Minor.App.INT_ERROR, msg))
			}
		} catch (ex: WSHelper.WSException) {
			val minor = StringUtils.nullToEmpty(ex.resultMinor)
			when (minor) {
				ECardConstants.Minor.App.INCORRECT_PARM -> throw ParameterInvalid(ex.message, ex)
				ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE -> throw SlotHandleInvalid(ex.message, ex)

				ECardConstants.Minor.IFD.PASSWORD_BLOCKED,
				ECardConstants.Minor.IFD.PASSWORD_SUSPENDED,
				ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED,
				-> throw PinBlocked(
					ex.message,
					ex,
				)

				ECardConstants.Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED -> throw SecurityConditionUnsatisfiable(
					ex.message,
					ex,
				)

				ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
				ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
				-> throw ThreadTerminateException(
					"Signature generation cancelled.",
					ex,
				)
				else -> throw ex
			}
		} catch (ex: InvocationTargetExceptionUnchecked) {
			when (val cause = ex.cause) {
				is InterruptedException, is ThreadTerminateException -> {
					throw ThreadTerminateException("Signature creation interrupted.")
				}
				else -> {
					throw createException(makeResultError(ECardConstants.Minor.App.INT_ERROR, cause?.message))
				}
			}
		} catch (ex: InterruptedException) {
			throw ThreadTerminateException("Signature creation interrupted.")
		} finally {
			tokenCache.clearPins()
			if (acquired) {
				s.release()
			}
		}
	}

	@Throws(UnsupportedAlgorithmException::class, InvalidParameterException::class)
	private fun getHashAlgOid(hash: ByteArray): ASN1ObjectIdentifier =
		when (getHashAlg(hash)) {
			HashAlgorithms.CKM_SHA_1 -> X509ObjectIdentifiers.id_SHA1
			HashAlgorithms.CKM_SHA224 -> NISTObjectIdentifiers.id_sha224
			HashAlgorithms.CKM_SHA256 -> NISTObjectIdentifiers.id_sha256
			HashAlgorithms.CKM_SHA384 -> NISTObjectIdentifiers.id_sha384
			HashAlgorithms.CKM_SHA512 -> NISTObjectIdentifiers.id_sha512
			// else -> {
			// 	val msg = "Hash algorithm is not supported."
			// 	throw UnsupportedAlgorithmException(msg)
			// }
		}

	@Throws(InvalidParameterException::class)
	private fun getHashAlg(hash: ByteArray): HashAlgorithms =
		when (hash.size) {
			20 -> HashAlgorithms.CKM_SHA_1
			28 -> HashAlgorithms.CKM_SHA224
			32 -> HashAlgorithms.CKM_SHA256
			48 -> HashAlgorithms.CKM_SHA384
			64 -> HashAlgorithms.CKM_SHA512
			else -> {
				val msg = "Size of the Hash does not match any supported algorithm."
				throw InvalidParameterException(msg)
			}
		}

	companion object {
		private val IFD_LOCKS: MutableMap<String, Semaphore> = mutableMapOf()

		@Synchronized
		private fun getLock(ifdName: String): Semaphore {
			var s: Semaphore? = IFD_LOCKS[ifdName]
			if (s == null) {
				s = Semaphore(1, true)
				IFD_LOCKS.put(ifdName, s)
			}
			return s
		}
	}
}
