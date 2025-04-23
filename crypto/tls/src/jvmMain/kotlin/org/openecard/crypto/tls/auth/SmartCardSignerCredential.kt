/****************************************************************************
 * Copyright (C) 2013-2018 ecsec GmbH.
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
package org.openecard.crypto.tls.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType
import org.openecard.bouncycastle.asn1.ASN1Encoding
import org.openecard.bouncycastle.asn1.DERNull
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.openecard.bouncycastle.asn1.x509.DigestInfo
import org.openecard.bouncycastle.tls.HashAlgorithm
import org.openecard.bouncycastle.tls.SignatureAlgorithm
import org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm
import org.openecard.bouncycastle.tls.TlsUtils
import org.openecard.bouncycastle.tls.crypto.TlsSigner
import org.openecard.bouncycastle.tls.crypto.TlsStreamSigner
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.WSHelper
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.SignatureAlgorithms
import org.openecard.crypto.common.SignatureAlgorithms.Companion.fromAlgId
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.DidInfo
import org.openecard.crypto.common.sal.did.NoSuchDid
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val LOG = KotlinLogging.logger { }

/**
 * Signing credential delegating all calls to a wrapped GenericCryptoSigner.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
class SmartCardSignerCredential(
	private val did: DidInfo,
) : TlsSigner {
	@Throws(IOException::class)
	override fun generateRawSignature(
		algorithm: SignatureAndHashAlgorithm?,
		hash: ByteArray,
	): ByteArray = genSig(algorithm, hash)

	@Throws(IOException::class)
	private fun genSig(
		algorithm: SignatureAndHashAlgorithm?,
		sigData: ByteArray,
	): ByteArray {
		var sigData = sigData
		val didAlg = this.didAlgorithm
		LOG.debug { "Using DID with algorithm=${didAlg.jcaAlg}." }

		if (algorithm != null) {
			val reqAlgStr =
				String.format(
					"%s-%s",
					SignatureAlgorithm.getText(algorithm.getSignature()),
					HashAlgorithm.getText(algorithm.getHash()),
				)
			LOG.debug { "Performing TLS 1.2 signature for algorithm=$reqAlgStr." }

			if (isRawSignature(didAlg)) {
				if (algorithm.getSignature() == SignatureAlgorithm.rsa) {
					// TLS >= 1.2 needs a PKCS#1 v1.5 signature and no raw RSA signature
					val hashAlgId = TlsUtils.getOIDForHashAlgorithm(algorithm.getHash())
					val digestInfo = DigestInfo(AlgorithmIdentifier(hashAlgId, DERNull.INSTANCE), sigData)
					sigData = digestInfo.getEncoded(ASN1Encoding.DER)
					LOG.debug { "Signing DigestInfo with algorithm=$hashAlgId." }
				} else if (SignatureAlgorithm.isRSAPSS(algorithm.getSignature())) {
					// cah be implemented with more recent BC versions by using the createRawSigner function
					// when implementing this, also adjust the filter function BaseSmartCardCredentialFactory.isSafeForNoneDid

					// Digest digest = crypto.createDigest(cryptoHashAlgorithm);
					// PSSSigner signer = PSSSigner.createRawSigner(new RSABlindedEngine(), digest, digest, digest.getDigestSize(), PSSSigner.TRAILER_IMPLICIT);
					// signer.init(true, new ParametersWithRandom(privateKey, crypto.getSecureRandom()));
					// signer.update(hash, 0, hash.length);
					// return signer.generateSignature();

					throw UnsupportedOperationException(
						"RSA-PSS with raw signature DIDs is not supported with this version of BouncyCastle.",
					)
				}
			}
		} else {
			LOG.debug { "Performing pre-TLS 1.2 signature." }
		}

		try {
			did.authenticateMissing()
			LOG.debug { "Calculating raw Signature of data=${ByteUtils.toHexString(sigData)}." }
			val signature = did.sign(sigData)
			LOG.debug { "Raw Signature=${ByteUtils.toHexString(signature)}." }
			return signature
		} catch (ex: WSHelper.WSException) {
			val msg = "Failed to create signature because of an unknown error."
			LOG.warn(ex) { msg }
			throw IOException(msg, ex)
		} catch (ex: SecurityConditionUnsatisfiable) {
			val msg = "Access to the signature DID could not be obtained."
			LOG.warn(ex) { msg }
			throw IOException(msg, ex)
		} catch (ex: NoSuchDid) {
			val msg = "Signing DID not available anymore."
			LOG.warn(ex) { msg }
			throw IOException(msg, ex)
		}
	}

	val didAlgorithm: SignatureAlgorithms
		get() {
			try {
				val algInfo = did.genericCryptoMarker.algorithmInfo
				val alg =
					fromAlgId(
						algInfo!!.getAlgorithmIdentifier().getAlgorithm(),
					)
				return alg
			} catch (ex: UnsupportedAlgorithmException) {
				throw RuntimeException("Error evaluating algorithm", ex)
			} catch (ex: WSHelper.WSException) {
				throw RuntimeException("Error evaluating algorithm", ex)
			}
		}

	@Throws(WSHelper.WSException::class)
	private fun supportsExternalHashing(): Boolean {
		val cryptoMarker = did.genericCryptoMarker
		return cryptoMarker.hashGenerationInfo == HashGenerationInfoType.NOT_ON_CARD
	}

	@Throws(IOException::class)
	override fun getStreamSigner(algorithm: SignatureAndHashAlgorithm?): TlsStreamSigner? {
		// the right thing to do would be to use the streaming hash functionality of DidAuthenticate, but this is not
		// implemented and also not needed if hashes are not created on the card

		try {
			if (algorithm != null && supportsExternalHashing()) {
				val hashAlg: Short =
					if (SignatureAlgorithm.isRSAPSS(algorithm.getSignature())) {
						SignatureAlgorithm.getRSAPSSHashAlgorithm(algorithm.getSignature())
					} else {
						algorithm.getHash()
					}

				val digestName = HashAlgorithm.getName(hashAlg)
				val md = MessageDigest.getInstance(digestName)
				val os: OutputStream =
					object : OutputStream() {
						@Throws(IOException::class)
						override fun write(b: Int) {
							md.update(b.toByte())
						}
					}
				// create stream signer for use with real data, not the hash of it
				return object : TlsStreamSigner {
					@Throws(IOException::class)
					override fun getOutputStream(): OutputStream = os

					@Throws(IOException::class)
					override fun getSignature(): ByteArray = genSig(algorithm, md.digest())
				}
			} else {
				return null
			}
		} catch (ex: NoSuchAlgorithmException) {
			throw IOException("Failed to create stream signer.", ex)
		} catch (ex: WSHelper.WSException) {
			throw IOException("Failed to create stream signer.", ex)
		}
	}
}

private fun isRawSignature(alg: SignatureAlgorithms): Boolean = isRawRSA(alg) || isRawECDSA(alg)

private fun isRawRSA(alg: SignatureAlgorithms): Boolean = alg == SignatureAlgorithms.CKM_RSA_PKCS

private fun isRawECDSA(alg: SignatureAlgorithms): Boolean = alg == SignatureAlgorithms.CKM_ECDSA
