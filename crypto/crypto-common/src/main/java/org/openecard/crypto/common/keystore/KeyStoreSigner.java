/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.CryptoException;
import org.openecard.bouncycastle.crypto.Signer;
import org.openecard.bouncycastle.crypto.digests.NullDigest;
import org.openecard.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.openecard.bouncycastle.crypto.engines.RSABlindedEngine;
import org.openecard.bouncycastle.crypto.params.ParametersWithRandom;
import org.openecard.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.openecard.bouncycastle.crypto.signers.GenericSigner;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.crypto.common.sal.CredentialPermissionDenied;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for the sign functionality of keystore entries.
 * 
 * @author Dirk Petrautzki <dirk.petrautzki@hs-coburg.de>
 */
public class KeyStoreSigner {

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreSigner.class);

    private byte[] rawCertData;
    private Map<String, java.security.cert.Certificate[]> javaCerts;
    private org.openecard.bouncycastle.crypto.tls.Certificate bcCert;
    private KeyStore keyStore;
    private char[] password;
    private String alias;
    private InputStream inputStream;

    /**
     * Creates a KeyStoreSigner and defines the InputStream to load from and the password and alias.
     *
     * @param keyStore
     * @param inputStream
     * @param password
     * @param alias
     */
    public KeyStoreSigner(@Nonnull KeyStore keyStore, InputStream inputStream, char[] password, String alias) {
	this.keyStore = keyStore;
	this.password = password;
	this.alias = alias;
	this.inputStream = inputStream;
	this.javaCerts = new HashMap<String, java.security.cert.Certificate[]>();
    }

    /**
     * Gets the certificate for this keystore entry.
     * This function returns the certificate in encoded form.
     *
     * @return Certificate of this KeyStore entry in encoded form.
     * @throws CredentialPermissionDenied In case the certificate could not be read from the token.
     * @throws IOException In case any other error occurred during the reading of the certificate.
     */
    public synchronized byte[] getCertificateChain() throws CredentialPermissionDenied, IOException {
	if (rawCertData == null) {
	    try {
		keyStore.load(inputStream, password);
		java.security.cert.Certificate[] cert;
		cert = keyStore.getCertificateChain(alias);
		// TODO this is only the users certificate
		rawCertData = cert[0].getEncoded();
	    } catch (KeyStoreException e) {
		throw new IOException("Keystore is not initialized.", e);
	    } catch (NoSuchAlgorithmException e) {
		throw new IOException(e);
	    } catch (CertificateException e) {
		throw new IOException(e);
	    }
	}
	return rawCertData;
    }

    /**
     * Gets the certificate for this KeyStore entry converted to a Java security certificate.
     * This method is just a convenience function to call the equivalent with the parameter {@code X.509}.
     *
     * @return
     * @throws CredentialPermissionDenied
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException 
     *
     * @see #getJavaSecCertificateChain(java.lang.String)
     */
    public java.security.cert.Certificate[] getJavaSecCertificateChain() throws CredentialPermissionDenied,
	    CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
	return getJavaSecCertificateChain("X.509");
    }

    /**
     * Gets the certificate for this KeyStore entry converted to a Java security certificate.
     * The type parameter is used to determine the requested certificate type. Each certificate type is cached 
     * once it is requested.
     *
     * @param certType Certificate type according to <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#CertificateFactory">
     *   CertificateFactory Types</a>
     * @return An array representing the certificate chain of this entry.
     * @throws CredentialPermissionDenied In case the certificate could not be read from the token. See
     *   {@link #getCertificateChain()}.
     * @throws IOException In case any other error occurred during the reading of the certificate. See
     *   {@link #getCertificateChain()}.
     * @throws CertificateException In case the certificate could not be converted.
     */
    @Nonnull
    public java.security.cert.Certificate[] getJavaSecCertificateChain(@Nonnull String certType) 
	    throws CertificateException, CredentialPermissionDenied, IOException {
	// is the certificate already available in java.security form?
	if (! javaCerts.containsKey(certType)) {
	    byte[] certs = getCertificateChain();
	    CertificateFactory cf = CertificateFactory.getInstance(certType);
	    Collection<? extends java.security.cert.Certificate> javaCert;
	    javaCert = cf.generateCertificates(new ByteArrayInputStream(certs));
	    javaCerts.put(certType, javaCert.toArray(new java.security.cert.Certificate[javaCert.size()]));
	}

	return javaCerts.get(certType);
    }

    /**
     * Gets the certificate for this KeyStore entry converted to a BouncyCastle TLS certificate.
     * 
     * @return The certificate chain in BouncyCastle format.
     * @throws CredentialPermissionDenied In case the certificate could not be read from the token. See
     *   {@link #getCertificateChain()}.
     * @throws IOException In case any other error occurred during the reading of the certificate. See
     *   {@link #getCertificateChain()}.
     * @throws CertificateException In case the certificate could not be converted.
     */
    @Nonnull
    public org.openecard.bouncycastle.crypto.tls.Certificate getBCCertificateChain() throws CredentialPermissionDenied,
	    CertificateException, IOException {
	// is the certificate already available in BC form?
	if (bcCert == null) {
	    byte[] certs = getCertificateChain();
	    bcCert = convertToBCCertificate(certs);
	}

	return bcCert;
    }

    /**
     * Signs the given hash with the entry represented by this instance.
     *
     * @param hash The hash that should be signed.
     * @return Signature of the given hash.
     * @throws SignatureException In case the signature could not be created.
     * @throws CredentialPermissionDenied In case the signature could not be performed by the token due to missing
     *   permissions.
     */
    public byte[] sign(@Nonnull byte[] hash) throws SignatureException, CredentialPermissionDenied {
	try {
	    KeyStore.PasswordProtection param = new KeyStore.PasswordProtection(password);
	    Entry entry = keyStore.getEntry(alias, param);
	    if (entry == null) {
		throw new SignatureException("No key entry for the given alias found.");
	    }
	    KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) entry;
	    PrivateKey myPrivateKey = pkEntry.getPrivateKey();

	    RSAPrivateCrtKey privCrtKey = (RSAPrivateCrtKey) myPrivateKey;
	    RSAPrivateCrtKeyParameters key = new RSAPrivateCrtKeyParameters(privCrtKey.getModulus(),
		    privCrtKey.getPublicExponent(), privCrtKey.getPrivateExponent(), privCrtKey.getPrimeP(),
		    privCrtKey.getPrimeQ(), privCrtKey.getPrimeExponentP(), privCrtKey.getPrimeExponentQ(),
		    privCrtKey.getCrtCoefficient());

	    Signer signer = new GenericSigner(new PKCS1Encoding(new RSABlindedEngine()), new NullDigest());

	    signer.init(true, new ParametersWithRandom(key, new SecureRandom()));
	    signer.update(hash, 0, hash.length);
	    return signer.generateSignature();
	} catch (NoSuchAlgorithmException e) {
	    throw new SignatureException(e);
	} catch (UnrecoverableEntryException e) {
	    String msg = "Private key entry couldn't be recovered from keystore. May a wrong password is used.";
	    throw new CredentialPermissionDenied(msg, e);
	} catch (KeyStoreException e) {
	    throw new SignatureException(e);
	} catch (CryptoException e) {
	    throw new SignatureException(e);
	}
    }

    private Certificate convertToBCCertificate(byte[] certificateBytes) {
	org.openecard.bouncycastle.asn1.x509.Certificate x509Certificate;
	x509Certificate = org.openecard.bouncycastle.asn1.x509.Certificate.getInstance(certificateBytes);
	org.openecard.bouncycastle.asn1.x509.Certificate[] certs;
	certs = new org.openecard.bouncycastle.asn1.x509.Certificate[] { x509Certificate };
	Certificate cert = new Certificate(certs);
	return cert;
    }

}
