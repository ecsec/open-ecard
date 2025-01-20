/****************************************************************************
 * Copyright (C) 2013-2023 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.bouncycastle.asn1.x500.X500Name
import org.openecard.bouncycastle.tls.*
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import org.openecard.bouncycastle.tls.crypto.TlsCryptoParameters
import org.openecard.bouncycastle.tls.crypto.TlsSigner
import org.openecard.bouncycastle.util.io.pem.PemObject
import org.openecard.bouncycastle.util.io.pem.PemWriter
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.WSHelper
import org.openecard.common.interfaces.Dispatcher
import org.openecard.crypto.common.HashAlgorithms
import org.openecard.crypto.common.KeyTypes
import org.openecard.crypto.common.SignatureAlgorithms
import org.openecard.crypto.common.SignatureAlgorithms.Companion.fromAlgId
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.DidInfo
import org.openecard.crypto.common.sal.did.NoSuchDid
import org.openecard.crypto.common.sal.did.TokenCache
import java.io.IOException
import java.io.StringWriter
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.annotation.Nonnull
import javax.security.auth.x500.X500Principal

private val LOG = KotlinLogging.logger { }

abstract class BaseSmartCardCredentialFactory protected constructor(
	@param:Nonnull protected val dispatcher: Dispatcher,
	protected val filterAlwaysReadable: Boolean
) : CredentialFactory, ContextAware {

	protected var ctx: TlsContext? = null
	protected val tokenCache: TokenCache = TokenCache(dispatcher)

	abstract val usedHandle: ConnectionHandleType?

	override fun setContext(context: TlsContext) {
		this.ctx = context
	}

	protected fun getClientCredentialsForCard(
		cr: CertificateRequest,
		handle: ConnectionHandleType
	): List<TlsCredentialedSigner> {
		val credentials = mutableListOf<TlsCredentialedSigner>()
		val tlsCrypto = TlsCryptoParameters(ctx)

		LOG.debug { "Selecting a suitable DID for the following requested algorithms:" }
		var crSigAlgs = getCrSigAlgs(cr)
		crSigAlgs = removeUnsupportedAlgs(crSigAlgs)
		for (reqAlg in crSigAlgs) {
			val reqAlgStr = String.format(
				"%s-%s", SignatureAlgorithm.getText(reqAlg.getSignature()),
				HashAlgorithm.getText(reqAlg.getHash())
			)
			LOG.debug { "  $reqAlgStr" }
		}

		//	try {
		val didInfos = tokenCache.getInfo(null, handle)
		var infos = didInfos.cryptoDidInfos

		printCerts(infos)

		// remove unsuitable DIDs
		LOG.info { "Sorting out DIDs not able to handle the TLS request." }
		infos = removeSecretCertDids(infos)
		infos = removeNonAuthDids(infos)
		infos = removeUnsupportedDidAlgs(infos)
		infos = removeUnsupportedCerts(cr, infos)

		//infos = nonRawFirst(infos);
		LOG.info { "Creating signer instances for the TLS Client Certificate signature." }

		// TLS < 1.2
		if (crSigAlgs.isEmpty()) {
			LOG.info { "Looking for a raw RSA DID." }

			for (info in infos) {
				try {
					LOG.debug { "Checking DID=${info.didName}." }

					val cred: TlsCredentialedSigner?
					val chain = info.relatedCertificateChain
					val clientCert = convertCert(ctx!!.crypto, chain)

					if (isRawRSA(info)) {
						LOG.debug { "Adding raw RSA signer." }
						val signer: TlsSigner = SmartCardSignerCredential(info)
						cred = DefaultTlsCredentialedSigner(tlsCrypto, signer, clientCert, null)
						credentials.add(cred)
					}
				} catch (ex: SecurityConditionUnsatisfiable) {
					LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
				} catch (ex: NoSuchDid) {
					LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
				} catch (ex: CertificateException) {
					LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
				} catch (ex: IOException) {
					LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
				} catch (ex: UnsupportedAlgorithmException) {
					LOG.error(ex) { "Unsupported algorithm used in CIF. Skipping DID ${info.didName}." }
				} catch (ex: WSHelper.WSException) {
					LOG.error(ex) { "Unknown error accessing DID ${info.didName}." }
				}
			}
		} else {
			// TLS >= 1.2
			LOG.info { "Looking for most specific DIDs." }

			// looping over the servers alg list preserves its ordering
			for (reqAlg in crSigAlgs) {
				for (info in infos) {
					LOG.debug { "Checking DID=${info.didName}." }

					try {
						val algInfo = info.genericCryptoMarker.algorithmInfo
						val alg = fromAlgId(algInfo!!.getAlgorithmIdentifier().getAlgorithm())

						val chain = info.relatedCertificateChain
						val clientCert = convertCert(ctx!!.crypto, chain)

						// find one DID for this problem, then continue with the next algorithm
						if (matchesAlg(reqAlg, alg) && (alg.hashAlg != null || isSafeForNoneDid(reqAlg))) {
							LOG.debug { "Adding ${alg.jcaAlg} signer." }
							val signer: TlsSigner = SmartCardSignerCredential(info)
							val cred = DefaultTlsCredentialedSigner(tlsCrypto, signer, clientCert, reqAlg)
							credentials.add(cred)
							break
							//return credentials;
						}
					} catch (ex: SecurityConditionUnsatisfiable) {
						LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
					} catch (ex: NoSuchDid) {
						LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
					} catch (ex: CertificateException) {
						LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
					} catch (ex: IOException) {
						LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${info.didName}." }
					} catch (ex: UnsupportedAlgorithmException) {
						LOG.error(ex) { "Unsupported algorithm used in CIF. Skipping DID ${info.didName}." }
					} catch (ex: WSHelper.WSException) {
						LOG.error(ex) { "Unknown error accessing DID ${info.didName}." }
					}
				}
			}
		}

		//	} catch (NoSuchDid | WSHelper.WSException ex) {
//	    LOG.error("Failed to access DIDs of smartcard. Proceeding without client authentication.", ex);
//	}
		return credentials
	}

	@Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class)
	private fun isCertNeedsPin(info: DidInfo): Boolean {
		var needsPin = false
		val dsis = info.relatedDataSets
		for (dsi in dsis) {
			if (!needsPin) {
				needsPin = dsi.needsPin()
			}
		}
		return needsPin
	}

	private fun matchesCertReq(cr: CertificateRequest, chain: List<X509Certificate>): Boolean {
		// no issuers mean accept anything
		if (cr.getCertificateAuthorities().isEmpty()) {
			LOG.debug { "Any certificate matches." }
			return true
		}

		// check if any of the certificates has an issuer matching the request
		for (issuerObj in cr.getCertificateAuthorities()) {
			try {
				val issuer = issuerObj as X500Name
				val reqIss = X500Principal(issuer.getEncoded("DER"))
				for (cert in chain) {
					if (cert.getIssuerX500Principal() == reqIss) {
						LOG.debug { "Issuer $issuer matched one of the certificate issuers." }
						return true
					}
				}
			} catch (ex: IOException) {
				LOG.error { "Unencodable issuer in requested authorities. Skipping entry." }
			}
		}

		// no issuer matched
		LOG.debug { "No issuer matches." }
		return false
	}

	@Throws(WSHelper.WSException::class)
	private fun isAuthCert(info: DidInfo, chain: List<X509Certificate>): Boolean {
		val algInfo = info.genericCryptoMarker.algorithmInfo
		if (!algInfo!!.getSupportedOperations().contains("Compute-signature")) {
			LOG.debug { "DID (${info.didName}): AlgorithmInfo does not provide Compute-signature flag." }
			return false
		}

		// check authentication (digital signature) flag
		val cert = chain[0]
		val isAuthCert = cert.keyUsage[0]
		val isSigCert = cert.keyUsage[1]
		if (!isAuthCert || isSigCert) {
			LOG.debug { "DID (${info.didName}): Certificate key usage does not permit authentication signatures." }
			return false
		}

		return true
	}


	private fun matchesAlg(reqAlg: SignatureAndHashAlgorithm, alg: SignatureAlgorithms): Boolean {
		try {
			val bcAlg = getCompatibleAlgorithms(alg)

			// RAW signature
			if (bcAlg.isEmpty()) {
				// filter out unmatching signature type
				if (alg.keyType != convertSigType(reqAlg.getSignature())) {
					return false
				}

				// Only allow a certain set of Hash algs. Some hashes are too large for the cards.
				return when (reqAlg.getHash()) {
					HashAlgorithm.sha1, HashAlgorithm.sha224, HashAlgorithm.sha256, HashAlgorithm.sha384, HashAlgorithm.sha512 -> true
					else -> false
				}
			} else {
				// match everything else
				return bcAlg.contains(reqAlg)
			}
		} catch (ex: IllegalArgumentException) {
			return false
		}
	}

	@Throws(WSHelper.WSException::class, UnsupportedAlgorithmException::class)
	private fun isRawRSA(info: DidInfo): Boolean {
		val algInfo = info.genericCryptoMarker.algorithmInfo
		val alg = fromAlgId(algInfo!!.getAlgorithmIdentifier().getAlgorithm())
		return SignatureAlgorithms.CKM_RSA_PKCS == alg
	}

	private fun convertSigType(sigType: Short): KeyTypes? {
		return when (sigType) {
			SignatureAlgorithm.rsa_pss_pss_sha256, SignatureAlgorithm.rsa_pss_pss_sha384, SignatureAlgorithm.rsa_pss_pss_sha512, SignatureAlgorithm.rsa_pss_rsae_sha256, SignatureAlgorithm.rsa_pss_rsae_sha384, SignatureAlgorithm.rsa_pss_rsae_sha512, SignatureAlgorithm.rsa -> KeyTypes.CKK_RSA
			SignatureAlgorithm.ecdsa -> KeyTypes.CKK_EC
			else -> null
		}
	}

	@Throws(IOException::class, CertificateEncodingException::class)
	private fun convertCert(crypto: TlsCrypto, chain: List<X509Certificate>): Certificate {
		val cert = crypto.createCertificate(chain[0].encoded)
		return Certificate(arrayOf(cert))
	}

	private fun nonRawFirst(infos: List<DidInfo>): List<DidInfo> {
		val result = mutableListOf<DidInfo>()

		// first add all the non raw RSA DIDs
		for (info in infos) {
			try {
				if (!isRawRSA(info)) {
					result.add(info)
				}
			} catch (ex: UnsupportedAlgorithmException) {
				LOG.error(ex) { "Invalid DID or error accessing the DID." }
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Invalid DID or error accessing the DID." }
			}
		}
		// then add all raw RSA DIDs
		for (info in infos) {
			try {
				if (isRawRSA(info)) {
					result.add(info)
				}
			} catch (ex: UnsupportedAlgorithmException) {
				LOG.error(ex) { "Invalid DID or error accessing the DID." }
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Invalid DID or error accessing the DID." }
			}
		}

		return result
	}

	private fun removeNonAuthDids(infos: List<DidInfo>): List<DidInfo> {
		val result = mutableListOf<DidInfo>()

		for (next in infos) {
			try {
				val certs = next.relatedCertificateChain
				if (!certs.isEmpty()) {
					val isAuthCert = isAuthCert(next, certs)
					if (isAuthCert) {
						result.add(next)
					}
				}
			} catch (ex: SecurityConditionUnsatisfiable) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: NoSuchDid) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: CertificateException) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Unknown error accessing DID ${next.didName}." }
			}
		}

		return result
	}

	private fun getCrSigAlgs(cr: CertificateRequest): List<SignatureAndHashAlgorithm> {
		val result = mutableListOf<SignatureAndHashAlgorithm>()
		if (cr.getSupportedSignatureAlgorithms() != null) {
			for (next in cr.getSupportedSignatureAlgorithms()) {
				result.add((next as org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm))
			}
		}
		return result
	}

	private fun removeUnsupportedCerts(cr: CertificateRequest, infos: List<DidInfo>): List<DidInfo> {
		val result = mutableListOf<DidInfo>()

		for (next in infos) {
			try {
				val chain = next.relatedCertificateChain
				if (matchesCertReq(cr, chain)) {
					result.add(next)
				}
			} catch (ex: SecurityConditionUnsatisfiable) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: NoSuchDid) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: CertificateException) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Unknown error accessing DID ${next.didName}." }
			}
		}

		return result
	}

	private fun removeSecretCertDids(infos: List<DidInfo>): List<DidInfo> {
		val result = mutableListOf<DidInfo>()

		for (next in infos) {
			try {
				// filter out dids having secret certificates
				if (!(filterAlwaysReadable && isCertNeedsPin(next))) {
					result.add(next)
				}
			} catch (ex: SecurityConditionUnsatisfiable) {
				LOG.error(ex) { "Failed to get ACL for certificates of DID ${next.didName}." }
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Unknown error accessing DID ${next.didName}." }
			}
		}

		return result
	}

	private fun printCerts(infos: List<DidInfo>) {
		for (next in infos) {
			try {
				val chain = next.relatedCertificateChain

				if (LOG.isDebugEnabled()) {
					for (cert in chain) {
						val out = StringWriter()
						val pw = PemWriter(out)
						pw.writeObject(PemObject("CERTIFICATE", cert.encoded))
						pw.close()
						LOG.debug { "Certificate for DID ${next.didName}\n${out}" }
						LOG.debug { "Certificate details\n${cert}" }
					}
				}
			} catch (ex: SecurityConditionUnsatisfiable) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: NoSuchDid) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: CertificateException) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: IOException) {
				LOG.error(ex) { "Failed to read certificates from card. Skipping DID ${next.didName}." }
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Unknown error accessing DID ${next.didName}." }
			}
		}
	}

	private fun removeUnsupportedDidAlgs(infos: List<DidInfo>): List<DidInfo> {
		val result = mutableListOf<DidInfo>()

		for (next in infos) {
			try {
				val algInfo = next.genericCryptoMarker.algorithmInfo
				val algStr = algInfo!!.getAlgorithmIdentifier().getAlgorithm()
				val alg = fromAlgId(algStr)

				when (alg) {
					SignatureAlgorithms.CKM_ECDSA,
					SignatureAlgorithms.CKM_ECDSA_SHA256,
					SignatureAlgorithms.CKM_ECDSA_SHA384,
					SignatureAlgorithms.CKM_ECDSA_SHA512,
					SignatureAlgorithms.CKM_RSA_PKCS,
					SignatureAlgorithms.CKM_SHA256_RSA_PKCS,
					SignatureAlgorithms.CKM_SHA384_RSA_PKCS,
					SignatureAlgorithms.CKM_SHA512_RSA_PKCS,
					SignatureAlgorithms.CKM_SHA256_RSA_PKCS_PSS,
					SignatureAlgorithms.CKM_SHA384_RSA_PKCS_PSS,
					SignatureAlgorithms.CKM_SHA512_RSA_PKCS_PSS -> result.add(
						next
					)

					else -> {}
				}
			} catch (ex: UnsupportedAlgorithmException) {
				LOG.error(ex) { "Unsupported algorithm used in CIF. Skipping DID ${next.didName}." }
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Unknown error accessing DID ${next.didName}." }
			}
		}

		return result
	}

	private fun removeUnsupportedAlgs(crSigAlgss: List<SignatureAndHashAlgorithm>): List<SignatureAndHashAlgorithm> {
		val crSigAlgs = crSigAlgss.toMutableList()
		val it = crSigAlgs.iterator()
		while (it.hasNext()) {
			val alg = it.next()
			when (alg.getSignature()) {
				SignatureAlgorithm.ecdsa,
				SignatureAlgorithm.rsa,
				SignatureAlgorithm.rsa_pss_rsae_sha256,
				SignatureAlgorithm.rsa_pss_rsae_sha384,
				SignatureAlgorithm.rsa_pss_rsae_sha512 -> {
				}

				else -> {
					it.remove()
					continue
				}
			}

			val hashAlg: Short
			if (SignatureAlgorithm.isRSAPSS(alg.getSignature())) {
				hashAlg = SignatureAlgorithm.getRSAPSSHashAlgorithm(alg.getSignature())
			} else {
				hashAlg = alg.getHash()
			}
			when (hashAlg) {
				HashAlgorithm.sha512,
				HashAlgorithm.sha384,
				HashAlgorithm.sha256 -> {
				}

				else -> {
					it.remove()
					continue
				}
			}
		}

		return crSigAlgs
	}


	private fun isSafeForNoneDid(reqAlg: SignatureAndHashAlgorithm): Boolean {
		// PSS is currently not supported by the stack
		if (SignatureAlgorithm.isRSAPSS(reqAlg.getSignature())) {
			return false
		}

		return when (reqAlg.getHash()) {
			HashAlgorithm.sha1,
			HashAlgorithm.sha224,
			HashAlgorithm.sha256,
			HashAlgorithm.sha384,
			HashAlgorithm.sha512 -> true

			else -> false
		}
	}

	companion object {
		private fun getCompatibleAlgorithms(alg: SignatureAlgorithms): Set<SignatureAndHashAlgorithm> {
			val hashAlg = alg.hashAlg
			val keyType = alg.keyType

			if (alg.isRsaPss && alg.hashAlg != null) {
				val pssAlg: SignatureAndHashAlgorithm?
				val rsaeAlg: SignatureAndHashAlgorithm?
				when (alg.hashAlg) {
					HashAlgorithms.CKM_SHA256 -> {
						pssAlg =
							SignatureAndHashAlgorithm(HashAlgorithm.Intrinsic, SignatureAlgorithm.rsa_pss_pss_sha256)
						rsaeAlg =
							SignatureAndHashAlgorithm(HashAlgorithm.Intrinsic, SignatureAlgorithm.rsa_pss_rsae_sha256)
					}

					HashAlgorithms.CKM_SHA384 -> {
						pssAlg =
							SignatureAndHashAlgorithm(HashAlgorithm.Intrinsic, SignatureAlgorithm.rsa_pss_pss_sha384)
						rsaeAlg =
							SignatureAndHashAlgorithm(HashAlgorithm.Intrinsic, SignatureAlgorithm.rsa_pss_rsae_sha384)
					}

					HashAlgorithms.CKM_SHA512 -> {
						pssAlg =
							SignatureAndHashAlgorithm(HashAlgorithm.Intrinsic, SignatureAlgorithm.rsa_pss_pss_sha512)
						rsaeAlg =
							SignatureAndHashAlgorithm(HashAlgorithm.Intrinsic, SignatureAlgorithm.rsa_pss_rsae_sha512)
					}

					else -> throw IllegalArgumentException("Unsupported hash algorithm selected.")
				}
				return setOf(pssAlg, rsaeAlg)
			}

			val hash = if (hashAlg != null) {
				when (hashAlg) {
					HashAlgorithms.CKM_SHA_1 -> HashAlgorithm.sha1
					HashAlgorithms.CKM_SHA224 -> HashAlgorithm.sha224
					HashAlgorithms.CKM_SHA256 -> HashAlgorithm.sha256
					HashAlgorithms.CKM_SHA384 -> HashAlgorithm.sha384
					HashAlgorithms.CKM_SHA512 -> HashAlgorithm.sha512
				}
			} else {
				return setOf<SignatureAndHashAlgorithm>()
			}

			val sig = when (keyType) {
				KeyTypes.CKK_RSA -> SignatureAlgorithm.rsa
				KeyTypes.CKK_EC -> SignatureAlgorithm.ecdsa
				else -> throw IllegalArgumentException("Unsupported signature algorithm selected.")
			}

			return setOf(SignatureAndHashAlgorithm(hash, sig))
		}
	}
}
