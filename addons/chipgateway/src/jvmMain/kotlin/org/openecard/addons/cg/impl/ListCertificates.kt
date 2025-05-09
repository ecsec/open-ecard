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

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.addons.cg.ex.ParameterInvalid
import org.openecard.addons.cg.ex.SlotHandleInvalid
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.ASN1OctetString
import org.openecard.bouncycastle.asn1.ASN1String
import org.openecard.bouncycastle.asn1.x500.X500Name
import org.openecard.bouncycastle.asn1.x500.style.BCStyle
import org.openecard.bouncycastle.asn1.x509.CertificatePolicies
import org.openecard.bouncycastle.asn1.x509.Extension
import org.openecard.bouncycastle.crypto.digests.SHA256Digest
import org.openecard.common.ECardConstants
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.ThreadTerminateException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.HandlerBuilder
import org.openecard.common.util.StringUtils
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.DidInfo
import org.openecard.crypto.common.sal.did.NoSuchDid
import org.openecard.crypto.common.sal.did.TokenCache
import org.openecard.ws.chipgateway.CertificateFilterType
import org.openecard.ws.chipgateway.CertificateInfoType
import org.openecard.ws.chipgateway.KeyUsageType
import java.io.IOException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.regex.Pattern

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class ListCertificates(
	private val tokenCache: TokenCache,
	private val certFilter: List<CertificateFilterType>,
	private val pin: CharArray?,
	sessionId: String?,
	slotHandle: ByteArray,
) {
	private val handle: ConnectionHandleType

	init {
		if (slotHandle.isEmpty()) {
			throw ParameterInvalid("Slot handle is empty.")
		}

		handle =
			HandlerBuilder
				.create()
				.setSlotHandle(ByteUtils.clone(slotHandle))
				.setSessionId(sessionId)
				.buildConnectionHandle()
	}

	@get:Throws(
		WSHelper.WSException::class,
		NoSuchDid::class,
		CertificateException::class,
		CertificateEncodingException::class,
		SecurityConditionUnsatisfiable::class,
		ParameterInvalid::class,
		SlotHandleInvalid::class,
	)
	val certificates: MutableList<CertificateInfoType?>
		get() {
			try {
				val result =
					ArrayList<CertificateInfoType?>()
				// get crypto dids
				val didInfos = tokenCache.getInfo(pin, handle)
				val cryptoDids: List<DidInfo> =
					didInfos.cryptoDidInfos

				// get certificates for each crypto did
				for (nextDid in cryptoDids) {
					logger.debug { "Reading certificates from DID=${nextDid.didName}." }
					val certChain =
						getCertChain(nextDid)
					if (!certChain.isEmpty() && matchesFilter(certChain)) {
						val algInfo =
							nextDid.genericCryptoMarker.algorithmInfo
						try {
							val jcaAlg = convertAlgInfo(algInfo!!)
							val cert = certChain[0]

							val certInfo =
								CertificateInfoType().apply {
									uniqueSSN = getUniqueIdentifier(cert)
									algorithm = jcaAlg
									didName = nextDid.didName
								}
							certChain.forEach {
								certInfo.certificate.add(it.encoded)
							}

							result.add(certInfo)
						} catch (ex: UnsupportedAlgorithmException) {
							// ignore this DID
							logger.warn("Ignoring DID with unsupported algorithm ${algInfo?.algorithmIdentifier?.algorithm}).")
						}
					}
				}

				return result
			} catch (ex: WSHelper.WSException) {
				val minor = StringUtils.nullToEmpty(ex.resultMinor)
				when (minor) {
					ECardConstants.Minor.App.INCORRECT_PARM -> throw ParameterInvalid(
						ex.message,
						ex,
					)

					ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE -> throw SlotHandleInvalid(
						ex.message,
						ex,
					)

					ECardConstants.Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED -> throw SecurityConditionUnsatisfiable(
						ex.message,
						ex,
					)

					ECardConstants.Minor.IFD.CANCELLATION_BY_USER, ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
					-> throw ThreadTerminateException(
						"Certificate retrieval interrupted.",
						ex,
					)

					else -> throw ex
				}
			} catch (ex: InvocationTargetExceptionUnchecked) {
				when (val cause = ex.cause) {
					is InterruptedException, is ThreadTerminateException -> {
						val msg = "Certificate retrieval interrupted."
						logger.debug(msg, ex)
						throw ThreadTerminateException(msg)
					}
					else -> {
						throw createException(
							makeResultError(
								ECardConstants.Minor.App.INT_ERROR,
								cause?.message,
							),
						)
					}
				}
			} finally {
				tokenCache.clearPins()
			}
		}

	@Throws(CertificateException::class, ParameterInvalid::class)
	private fun matchesFilter(certChain: List<X509Certificate>): Boolean {
		// check if any of the filters matches
		if (!certFilter.isEmpty()) {
			for (filter in certFilter) {
				if (filter.getPolicy() != null) {
					if (!matchesPolicy(filter.getPolicy(), certChain)) {
						continue
					}
				}
				if (filter.getIssuer() != null) {
					if (!matchesIssuer(filter.getIssuer(), certChain)) {
						continue
					}
				}
				if (filter.getKeyUsage() != null) {
					if (!matchesKeyUsage(filter.getKeyUsage(), certChain)) {
						continue
					}
				}

				// all filters passed, we have a match
				return true
			}

			// no filter matched
			return false
		} else {
			// no filter to apply, so every chain matches
			return true
		}
	}

	@Throws(CertificateException::class, ParameterInvalid::class)
	private fun matchesPolicy(
		policy: String,
		certChain: List<X509Certificate>,
	): Boolean {
		try {
			val policyId = ASN1ObjectIdentifier(policy)
			val cert = certChain[0]
			var encodedPolicy = cert.getExtensionValue(Extension.certificatePolicies.id)
			if (encodedPolicy != null) {
				encodedPolicy = ASN1OctetString.getInstance(encodedPolicy).octets
				try {
					// extract policy object
					val certPolicies = CertificatePolicies.getInstance(encodedPolicy)
					// see if any of the policies matches
					val targetPolicy = certPolicies?.getPolicyInformation(policyId)
					return targetPolicy != null
				} catch (ex: IllegalArgumentException) {
					throw CertificateException("Certificate contains invalid policy.")
				}
			} else {
				// no policy defined in certificate, so no match
				return false
			}
		} catch (ex: IllegalArgumentException) {
			throw ParameterInvalid("Requested policy filter is not an OID.")
		}
	}

	private fun matchesIssuer(
		issuer: String,
		certChain: List<X509Certificate>,
	): Boolean {
		var issuer = issuer
		val cert = certChain[0]

		// determine where to add wildcards
		var prefixWildcard = false
		var infixWildcard = false
		if (issuer.startsWith("*")) {
			issuer = issuer.substring(1)
			prefixWildcard = true
		}
		if (issuer.endsWith("*")) {
			issuer = issuer.substring(0, issuer.length - 1)
			infixWildcard = true
		}
		// quote the remaining text to prevent regex injection
		issuer = Pattern.quote(issuer)
		// add the wildcards
		if (prefixWildcard) {
			issuer = ".*$issuer"
		}
		if (infixWildcard) {
			issuer = "$issuer.*"
		}
		val searchPattern = Pattern.compile(issuer)

		// extract RDNs if they exist
		val certIssuer = cert.issuerX500Principal.name
		val issuerDn = X500Name(certIssuer)

		// compare CN
		if (matchesRdn(searchPattern, issuerDn, BCStyle.CN)) {
			return true
		}
		// compare GN
		if (matchesRdn(searchPattern, issuerDn, BCStyle.GIVENNAME)) {
			return true
		}

		// no match
		return false
	}

	private fun matchesRdn(
		searchPattern: Pattern,
		name: X500Name,
		rdnIdentifier: ASN1ObjectIdentifier?,
	): Boolean {
		val rdns = name.getRDNs(rdnIdentifier)
		if (rdns.size >= 1) {
			// only compare first as everything else would be non standard in X509 certs
			val rdnAttr = rdns[0]!!.first
			val attrStr = rdnAttr.value.toASN1Primitive() as ASN1String
			val rdnStr = attrStr.string
			return searchPattern.matcher(rdnStr).matches()
		} else {
			return false
		}
	}

	private fun matchesKeyUsage(
		keyUsage: KeyUsageType,
		certChain: List<X509Certificate>,
	): Boolean {
		val cert = certChain[0]
		val certUsage = cert.keyUsage
		return when (keyUsage) {
			KeyUsageType.AUTHENTICATION -> // digitalSignature
				certUsage[0]

			KeyUsageType.SIGNATURE -> // nonRepudiation
				certUsage[1]

			KeyUsageType.ENCRYPTION -> // keyEncipherment, dataEncipherment, keyAgreement
				certUsage[2] || certUsage[3] || certUsage[4]
		}
	}

	private fun getUniqueIdentifier(cert: X509Certificate): String? {
		// try to get SERIALNUMBER from subject
		val sub = X500Name.getInstance(cert.subjectX500Principal.encoded)
		val serials = sub!!.getRDNs(BCStyle.SERIALNUMBER)
		if (serials.size >= 1) {
			val serialValueType = serials[0]!!.first
			val serialValue = serialValueType.value
			if (ASN1String::class.java.isInstance(serialValue)) {
				return ASN1String::class.java.cast(serialValue).string
			}
		}

		// no SERIALNUMBER, hash subject and cross fingers that this is unique across replacement cards
		try {
			val digest = SHA256Digest()
			val subData = sub.encoded
			digest.update(subData, 0, subData.size)
			val hashResult = ByteArray(digest.digestSize)
			digest.doFinal(hashResult, 0)
			val hashedSub = ByteUtils.toWebSafeBase64String(hashResult)
			return hashedSub
		} catch (ex: IOException) {
			throw RuntimeException("Failed to encode subject.", ex)
		}
	}

	@Throws(UnsupportedAlgorithmException::class)
	private fun convertAlgInfo(isoAlgInfo: AlgorithmInfoType): String {
		val oid = isoAlgInfo.algorithmIdentifier.algorithm
		val jcaAlg = AllowedSignatureAlgorithms.algIdtoJcaName(oid)
		return jcaAlg
	}

	@Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class, NoSuchDid::class)
	private fun getCertChain(nextDid: DidInfo): List<X509Certificate> {
		try {
			return nextDid.relatedCertificateChain
		} catch (ex: CertificateException) {
			logger.warn { "DID ${nextDid.didName} did not contain any certificates." }
			logger.debug { "Cause:$ex" }
			return listOf()
		}
	}
}
