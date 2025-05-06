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
import org.openecard.addons.cg.activate.CGTrustStoreLoader
import org.openecard.addons.cg.ex.InvalidSubjectException
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.openecard.bouncycastle.cert.X509CertificateHolder
import org.openecard.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.openecard.bouncycastle.cms.CMSException
import org.openecard.bouncycastle.cms.CMSProcessable
import org.openecard.bouncycastle.cms.CMSProcessableByteArray
import org.openecard.bouncycastle.cms.CMSSignedData
import org.openecard.bouncycastle.cms.CMSVerifierCertificateNotValidException
import org.openecard.bouncycastle.cms.SignerId
import org.openecard.bouncycastle.cms.jcajce.JcaSignerInfoVerifierBuilder
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider
import org.openecard.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder
import org.openecard.bouncycastle.operator.OperatorCreationException
import org.openecard.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.openecard.bouncycastle.util.Selector
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.security.cert.CertPathBuilder
import java.security.cert.CertPathBuilderException
import java.security.cert.CertStore
import java.security.cert.CertStoreParameters
import java.security.cert.CertificateException
import java.security.cert.CollectionCertStoreParameters
import java.security.cert.PKIXBuilderParameters
import java.security.cert.PKIXCertPathBuilderResult
import java.security.cert.PKIXRevocationChecker
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate
import java.util.Date

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class SignatureVerifier(
	private val trustStore: KeyStore,
	private val challenge: ByteArray,
) {
	var isRevocationCheck = ChipGatewayProperties.isRevocationCheck

	constructor(
		challenge: ByteArray,
	) : this(CGTrustStoreLoader().trustStore!!, challenge)

	private class X509CertificateHolderSignerId(
		private val signerId: SignerId,
	) : Selector<X509CertificateHolder> {
		override fun match(p0: X509CertificateHolder): Boolean = signerId.match(p0)

		override fun clone(): X509CertificateHolderSignerId = X509CertificateHolderSignerId(signerId)
	}

	@Throws(KeyStoreException::class, SignatureInvalid::class)
	fun validate(signature: ByteArray) {
		try {
			// load BC provider, so that the algorithms are available for the signature verification
			Security.addProvider(BouncyCastleProvider())

			val wrappedChallenge: CMSProcessable = CMSProcessableByteArray(challenge)
			val signedData = CMSSignedData(wrappedChallenge, signature)

			val certStore = signedData.certificates
			val signerInfoStore = signedData.signerInfos
			val signers = signerInfoStore.signers

			val allCerts = convertCertificates(certStore.getMatches(AllSelector()))

			for (signer in signers) {
				val certCollection = certStore.getMatches(X509CertificateHolderSignerId(signer.sid))

				val cert = certCollection.iterator().next()

				val dp = JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build()
				val verifyBuilder = JcaSignerInfoVerifierBuilder(dp).setProvider(BouncyCastleProvider.PROVIDER_NAME)
				verifyBuilder.setSignatureAlgorithmFinder(
					object : DefaultSignatureAlgorithmIdentifierFinder() {
						override fun find(sigAlgName: String?): AlgorithmIdentifier? {
							require(
								AllowedSignatureAlgorithms.isKnownJcaAlgorithm(sigAlgName),
							) { "Unsupported signature algorithm used." }
							return super.find(sigAlgName)
						}
					},
				)
				val verify = verifyBuilder.build(cert)

				// verify the signature
				if (!signer.verify(verify)) {
					throw SignatureInvalid("Signer information could not be verified.")
				}

				// verify the path and certificate
				val x509Cert = convertCertificate(cert)
				// TODO: verify that the signature is not too old. How old can it be at max? 1 minute?
				validatePath(x509Cert, allCerts, null)

				// check that the end certificate is under the admissable certificates
				if (ChipGatewayProperties.isUseSubjectWhitelist) {
					val subj = x509Cert.subjectX500Principal
					if (!AllowedSubjects.isInSubjects(subj)) {
						throw InvalidSubjectException("The certificate used in the signature has an invalid subject: ${subj.name}")
					}
				}
			}

			// fail if there is no signature in the SignedData structure
			if (signers.isEmpty()) {
				throw SignatureInvalid("No signatures present in the given SignedData element.")
			}
		} catch (ex: CertificateException) {
			throw SignatureInvalid("Failed to read a certificate form the CMS data structure.", ex)
		} catch (ex: CertPathBuilderException) {
			throw SignatureInvalid("Failed to build certificate path for PKIX validation.", ex)
		} catch (ex: CMSVerifierCertificateNotValidException) {
			throw SignatureInvalid("Signer certificate was not valid when the signature was created.", ex)
		} catch (ex: CMSException) {
			throw SignatureInvalid("Failed to validate CMS data structure.", ex)
		} catch (ex: InvalidSubjectException) {
			throw SignatureInvalid("Certificate with invalid subject used in signature.", ex)
		} catch (ex: NoSuchAlgorithmException) {
			throw SignatureInvalid("Invalid or unsupported algorithm or algorithm parameter used in signature.", ex)
		} catch (ex: InvalidAlgorithmParameterException) {
			throw SignatureInvalid("Invalid or unsupported algorithm or algorithm parameter used in signature.", ex)
		} catch (ex: OperatorCreationException) {
			throw SignatureInvalid("Invalid or unsupported algorithm or algorithm parameter used in signature.", ex)
		} catch (ex: IllegalArgumentException) {
			throw SignatureInvalid("Signature contains an invalid value.", ex)
		}
	}

	@Throws(
		NoSuchAlgorithmException::class,
		KeyStoreException::class,
		InvalidAlgorithmParameterException::class,
		CertPathBuilderException::class,
	)
	private fun validatePath(
		cert: X509Certificate,
		intermediateCerts: Collection<X509Certificate>,
		checkDate: Date?,
	): PKIXCertPathBuilderResult {
		// enable downloading of missing certificates based on the AIA extension
		try {
			System.setProperty("com.sun.security.enableAIAcaIssuers", "true")
		} catch (ex: SecurityException) {
			logger.warn { "Failed to enable AIA evaluation. Skipping downloads of missing certificates." }
		}

		val builder = CertPathBuilder.getInstance("PKIX")

		// configure path building
		val target = X509CertSelector().apply { certificate = cert }
		val intermediates: CertStoreParameters = CollectionCertStoreParameters(intermediateCerts)
		val params =
			PKIXBuilderParameters(trustStore, target).apply {
				addCertStore(CertStore.getInstance("Collection", intermediates))
				date = checkDate
				isRevocationEnabled = false
			}

		if (ChipGatewayProperties.isRevocationCheck) {
			val revChecker = builder.revocationChecker as PKIXRevocationChecker
			// revOpts.add(PKIXRevocationChecker.Option.ONLY_END_ENTITY);
			revChecker.options = mutableSetOf()
			params.certPathCheckers = null
			params.addCertPathChecker(revChecker)
		}

		// try to build the path
		return builder.build(params) as PKIXCertPathBuilderResult
	}

	@Throws(CertificateException::class)
	private fun convertCertificate(certHolder: X509CertificateHolder): X509Certificate =
		JcaX509CertificateConverter().getCertificate(certHolder)

	private fun convertCertificates(certHolders: Collection<X509CertificateHolder>): Collection<X509Certificate> =
		certHolders.mapNotNull {
			try {
				convertCertificate(it)
			} catch (_: CertificateException) {
				null
			}
		}

	private class AllSelector<T> : Selector<T> {
		override fun match(obj: T) = true

		override fun clone() = AllSelector<T>()
	}
}
