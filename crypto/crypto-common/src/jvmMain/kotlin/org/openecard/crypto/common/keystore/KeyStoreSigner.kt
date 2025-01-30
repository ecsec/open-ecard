/****************************************************************************
 * Copyright (C) 2013-2017 HS Coburg.
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
package org.openecard.crypto.common.keystore

import org.openecard.bouncycastle.crypto.CryptoException
import org.openecard.bouncycastle.crypto.Signer
import org.openecard.bouncycastle.crypto.digests.NullDigest
import org.openecard.bouncycastle.crypto.encodings.PKCS1Encoding
import org.openecard.bouncycastle.crypto.engines.RSABlindedEngine
import org.openecard.bouncycastle.crypto.params.ParametersWithRandom
import org.openecard.bouncycastle.crypto.signers.GenericSigner
import org.openecard.bouncycastle.crypto.signers.RSADigestSigner
import org.openecard.bouncycastle.crypto.util.PrivateKeyFactory
import org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm
import org.openecard.bouncycastle.tls.TlsUtils
import org.openecard.crypto.common.ReusableSecureRandom
import org.openecard.crypto.common.sal.did.CredentialPermissionDenied
import java.io.IOException
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.interfaces.RSAPrivateKey

/**
 * Wrapper for the sign functionality of keystore entries.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class KeyStoreSigner
/**
 * Creates a KeyStoreSigner and defines the InputStream to load from and the password and alias.
 *
 * @param keyStore
 * @param password
 * @param alias
 */
	(private val keyStore: KeyStore, private val password: CharArray?, private val alias: String) {
    //    private Certificate bcCert;

    @get:Throws(KeyStoreException::class, CertificateException::class)
    private val jCACertificateChain: Array<Certificate>
        /**
         * Gets the certificate for this keystore entry.
         * This function returns the certificate in encoded form.
         *
         * @return Certificate of this KeyStore entry in encoded form.
         * @throws KeyStoreException Thrown in case the keystore has not been initialized (loaded).
         * @throws CertificateException Thrown in case the certificate could not be found.
         */
        get() {
			val cert = keyStore.getCertificateChain(alias)
            if (cert == null) {
                throw CertificateException("Unknown alias.")
            }
            return cert
        }

    //    /**
    //     * Gets the certificate for this KeyStore entry converted to a BouncyCastle TLS certificate.
    //     *
    //     * @return The certificate chain in BouncyCastle format.
    //     * @throws CertificateException Thrown in case the certificate could not be found or converted.
    //     * @throws IllegalStateException Thrown in case the keystore is not initialized.
    //     */
    //    @Nonnull
    //    public synchronized Certificate getCertificateChain() throws CertificateException {
    //	if (bcCert == null) {
    //	    try {
    //		java.security.cert.Certificate[] jcaCerts = getJCACertificateChain();
    //		bcCert = KeyTools.convertCertificates(jcaCerts);
    //	    } catch (KeyStoreException ex) {
    //		throw new IllegalStateException("Uninitialized keystore supplied.");
    //	    }
    //	}
    //	return bcCert;
    //    }

    /**
     * Signs the given hash with the entry represented by this instance.
     *
     * @param sigHashAlg Signature and hash algorithm. If `null`, then use PKCS1 v1.5.
     * @param hash The hash that should be signed.
     * @return Signature of the given hash.
     * @throws SignatureException In case the signature could not be created.
     * @throws CredentialPermissionDenied In case the signature could not be performed by the token due to missing
     * permissions.
     */
    @Throws(SignatureException::class, CredentialPermissionDenied::class)
    fun sign(sigHashAlg: SignatureAndHashAlgorithm?, hash: ByteArray): ByteArray {
        try {
            val key = keyStore.getKey(alias, password)
            if (key !is RSAPrivateKey) {
                throw SignatureException("No private key available for the sign operation.")
            } else {
                val pKey = key as PrivateKey
                val bcKey = PrivateKeyFactory.createKey(pKey.encoded)
                val signer = if (sigHashAlg == null) {
                    GenericSigner(PKCS1Encoding(RSABlindedEngine()), NullDigest())
                } else {
                    val hashOid = TlsUtils.getOIDForHashAlgorithm(sigHashAlg.getHash())
                    RSADigestSigner(NullDigest(), hashOid)
                }
                signer.init(true, ParametersWithRandom(bcKey, ReusableSecureRandom.instance))
                signer.update(hash, 0, hash.size)
                val signature = signer.generateSignature()
                return signature
            }
        } catch (ex: KeyStoreException) {
            throw IllegalStateException("Keystore is not initialized.", ex)
        } catch ( /*| InvalidKeyException*/ex: UnrecoverableKeyException) {
            throw CredentialPermissionDenied("No usable key could be retrieved from the keystore.", ex)
        } catch (ex: NoSuchAlgorithmException) {
            throw SignatureException("Requested algorithm is not available.", ex)
        } catch (ex: IOException) {
            throw SignatureException("Failed to convert private key to BC class.", ex)
        } catch (ex: CryptoException) {
            throw SignatureException("Failed to compute signature.", ex)
        }
    }
}
