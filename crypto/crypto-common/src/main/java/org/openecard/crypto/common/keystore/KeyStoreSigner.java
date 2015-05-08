/****************************************************************************
 * Copyright (C) 2013-2015 HS Coburg.
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

package org.openecard.crypto.common.keystore;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.crypto.CryptoException;
import org.openecard.bouncycastle.crypto.Signer;
import org.openecard.bouncycastle.crypto.digests.NullDigest;
import org.openecard.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.openecard.bouncycastle.crypto.engines.RSABlindedEngine;
import org.openecard.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.openecard.bouncycastle.crypto.params.ParametersWithRandom;
import org.openecard.bouncycastle.crypto.signers.GenericSigner;
import org.openecard.bouncycastle.crypto.signers.RSADigestSigner;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.TlsUtils;
import org.openecard.bouncycastle.crypto.util.PrivateKeyFactory;
import org.openecard.crypto.common.ReusableSecureRandom;
import org.openecard.crypto.common.sal.CredentialPermissionDenied;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for the sign functionality of keystore entries.
 * 
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class KeyStoreSigner {

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreSigner.class);

    private final KeyStore keyStore;
    private final char[] password;
    private final String alias;
    private Certificate bcCert;

    /**
     * Creates a KeyStoreSigner and defines the InputStream to load from and the password and alias.
     *
     * @param keyStore
     * @param password
     * @param alias
     */
    public KeyStoreSigner(@Nonnull KeyStore keyStore, char[] password, String alias) {
	this.keyStore = keyStore;
	this.password = password;
	this.alias = alias;
    }

    /**
     * Gets the certificate for this keystore entry.
     * This function returns the certificate in encoded form.
     *
     * @return Certificate of this KeyStore entry in encoded form.
     * @throws KeyStoreException Thrown in case the keystore has not been initialized (loaded).
     * @throws CertificateException Thrown in case the certificate could not be found.
     */
    private java.security.cert.Certificate[] getJCACertificateChain() throws KeyStoreException, CertificateException {
	java.security.cert.Certificate[] cert;
	cert = keyStore.getCertificateChain(alias);
	if (cert == null) {
	    throw new CertificateException("Unknown alias.");
	}
	return cert;
    }

    /**
     * Gets the certificate for this KeyStore entry converted to a BouncyCastle TLS certificate.
     * 
     * @return The certificate chain in BouncyCastle format.
     * @throws CertificateException Thrown in case the certificate could not be found or converted.
     * @throws IllegalStateException Thrown in case the keystore is not initialized.
     */
    @Nonnull
    public synchronized Certificate getCertificateChain() throws CertificateException {
	if (bcCert == null) {
	    try {
		java.security.cert.Certificate[] jcaCerts = getJCACertificateChain();
		bcCert = KeyTools.convertCertificates(jcaCerts);
	    } catch (KeyStoreException ex) {
		throw new IllegalStateException("Uninitialized keystore supplied.");
	    }
	}
	return bcCert;
    }

    /**
     * Signs the given hash with the entry represented by this instance.
     *
     * @param sigHashAlg Signature and hash algorithm. If {@code null}, then use PKCS1 v1.5.
     * @param hash The hash that should be signed.
     * @return Signature of the given hash.
     * @throws SignatureException In case the signature could not be created.
     * @throws CredentialPermissionDenied In case the signature could not be performed by the token due to missing
     *   permissions.
     */
    public byte[] sign(@Nullable SignatureAndHashAlgorithm sigHashAlg, @Nonnull byte[] hash) throws SignatureException,
	    CredentialPermissionDenied {
	try {
	    Key key = keyStore.getKey(alias, password);
	    if (! (key instanceof RSAPrivateKey)) {
		throw new SignatureException("No private key available for the sign operation.");
	    } else {
		PrivateKey pKey = (PrivateKey) key;
		AsymmetricKeyParameter bcKey = PrivateKeyFactory.createKey(pKey.getEncoded());
		Signer signer;
		if (sigHashAlg == null) {
		    signer = new GenericSigner(new PKCS1Encoding(new RSABlindedEngine()), new NullDigest());
		} else {
		    ASN1ObjectIdentifier hashOid = TlsUtils.getOIDForHashAlgorithm(sigHashAlg.getHash());
		    signer = new RSADigestSigner(new NullDigest(), hashOid);
		}
		signer.init(true, new ParametersWithRandom(bcKey, ReusableSecureRandom.getInstance()));
		signer.update(hash, 0, hash.length);
		byte[] signature = signer.generateSignature();
		return signature;
	    }
	} catch (KeyStoreException ex) {
	    throw new IllegalStateException("Keystore is not initialized.");
	} catch (UnrecoverableKeyException /*| InvalidKeyException*/ ex) {
	    throw new CredentialPermissionDenied("No usable key could be retrieved from the keystore.", ex);
	} catch (NoSuchAlgorithmException ex) {
	    throw new SignatureException("Requested algorithm is not available.", ex);
	} catch (IOException ex) {
	    throw new SignatureException("Failed to convert private key to BC class.");
	} catch (CryptoException ex) {
	    throw new SignatureException("Failed to compute signature.", ex);
	}
    }

}
