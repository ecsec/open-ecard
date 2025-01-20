/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
package org.openecard.crypto.tls.verify

import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.bouncycastle.tls.crypto.TlsCertificate
import org.openecard.common.util.Pair
import org.openecard.crypto.tls.CertificateVerificationException
import org.openecard.crypto.tls.CertificateVerifier
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.cert.*
import java.security.cert.PKIXRevocationChecker

/**
 * Java Security based certificate verifier.
 * This implementation converts the BouncyCastle certificates to java.security certificates and uses the Java-bundled
 * PKIX mechanism to verify the certificate chain.
 *
 * @author Tobias Wich
 */
open class JavaSecVerifier @JvmOverloads constructor(protected val checkRevocation: Boolean = false) :
    CertificateVerifier {
    protected val certPathValidator: CertPathBuilder

    /**
     * Create a JavaSecVerifier and load the internal certificate path validator.
     *
     * @throws RuntimeException Thrown in case the validator could not be loaded due to a missing algorithm.
     */
    init {
        try {
            certPathValidator = CertPathBuilder.getInstance("PKIX")
        } catch (ex: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to load CertPathBuilder")
        }
    }

    protected open val trustStore: Set<TrustAnchor>
        get() = TrustStoreLoader().trustAnchors

    @Throws(CertificateVerificationException::class)
    override fun isValid(chain: TlsServerCertificate, hostname: String) {
        validateCertificate(chain, hostname)
    }

	@Throws(CertificateVerificationException::class)
    protected fun validateCertificate(chain: TlsServerCertificate, hostname: String): CertPathBuilderResult? {
        try {
            val trustStore = this.trustStore
            val path = buildChain(chain)

            val cpb = certPathValidator
            val targetSelector = X509CertSelector()
            targetSelector.setCertificate(path.p1)

            // create the parameters for the validator
            val cpp = PKIXBuilderParameters(trustStore, targetSelector)
            cpp.addCertStore(path.p2)
            if (checkRevocation) {
                //				cpp.setRevocationEnabled(true);
                //				System.setProperty("com.sun.security.enableCRLDP", "true");
				cpp.isRevocationEnabled = false
                val revChecker = cpb.revocationChecker as PKIXRevocationChecker
                val revOpts = mutableSetOf<PKIXRevocationChecker.Option>()
                //revOpts.add(PKIXRevocationChecker.Option.ONLY_END_ENTITY);
                revChecker.setOptions(revOpts)
                // TODO: add OCSP responses
                //revChecker.setOcspResponses(responses);
				cpp.certPathCheckers = null
                cpp.addCertPathChecker(revChecker)
            } else {
                // disable CRL checking since we are not supplying any CRLs yet
				cpp.isRevocationEnabled = false
            }

            // build path performs the validation - exception marks failure
            val result = cpb.build(cpp)
            return result
        } catch (ex: CertPathBuilderException) {
            throw CertificateVerificationException(ex.message)
        } catch (ex: GeneralSecurityException) {
            throw CertificateVerificationException(ex.message)
        } catch (ex: IOException) {
            throw CertificateVerificationException("Error converting certificate chain to java.security format.")
        }
    }

    @Throws(
        CertificateException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class,
        NoSuchAlgorithmException::class
    )
    private fun buildChain(chain: TlsServerCertificate): Pair<X509Certificate, CertStore> {
        val auxCerts = mutableListOf<X509Certificate>()
        val cf = CertificateFactory.getInstance("X.509")

        for (next in chain.certificate.getCertificateList()) {
            val nextConverted = convertCertificateInt(cf, next)
            auxCerts.add(nextConverted)
        }

        val eeCert = auxCerts[0]
        val auxCertStore = buildAuxCertStore(auxCerts)

        return Pair(eeCert, auxCertStore)
    }

    @Throws(InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class)
    private fun buildAuxCertStore(certs: MutableCollection<*>): CertStore {
        val params = CollectionCertStoreParameters(certs)
        val store = CertStore.getInstance("Collection", params)
        return store
    }


    companion object {
        @Throws(CertificateException::class, IOException::class)
        fun convertChain(chain: TlsServerCertificate): CertPath {
            val numCerts = chain.certificate.getCertificateList().size
            val result = ArrayList<Certificate?>(numCerts)
            val cf = CertificateFactory.getInstance("X.509")

            for (next in chain.certificate.getCertificateList()) {
                val nextConverted: Certificate? = convertCertificateInt(cf, next)
                result.add(nextConverted)
            }

            return cf.generateCertPath(result)
        }

        @Throws(CertificateException::class, IOException::class)
        fun convertCertificate(cert: TlsCertificate): Certificate {
            val cf = CertificateFactory.getInstance("X.509")
            return convertCertificateInt(cf, cert)
        }

        @Throws(CertificateException::class, IOException::class)
        fun convertCertificateInt(cf: CertificateFactory, cert: TlsCertificate): X509Certificate {
            val nextData = cert.encoded
            val nextDataStream = ByteArrayInputStream(nextData)
            val nextConverted = cf.generateCertificate(nextDataStream) as X509Certificate
            return nextConverted
        }
    }
}
