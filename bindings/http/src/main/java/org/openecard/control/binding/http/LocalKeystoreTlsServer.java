/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.control.binding.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.crypto.agreement.DHStandardGroups;
import org.openecard.bouncycastle.crypto.params.DHParameters;
import org.openecard.bouncycastle.crypto.tls.AbstractTlsServer;
import org.openecard.bouncycastle.crypto.tls.AlertDescription;
import org.openecard.bouncycastle.crypto.tls.AlertLevel;
import org.openecard.bouncycastle.crypto.tls.CipherSuite;
import org.openecard.bouncycastle.crypto.tls.EncryptionAlgorithm;
import org.openecard.bouncycastle.crypto.tls.HashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.KeyExchangeAlgorithm;
import org.openecard.bouncycastle.crypto.tls.MACAlgorithm;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.TlsCipher;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;
import org.openecard.bouncycastle.crypto.tls.TlsDHEKeyExchange;
import org.openecard.bouncycastle.crypto.tls.TlsECDHEKeyExchange;
import org.openecard.bouncycastle.crypto.tls.TlsFatalAlert;
import org.openecard.bouncycastle.crypto.tls.TlsKeyExchange;
import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.FileUtils;
import org.openecard.crypto.common.keystore.KeyStoreSigner;
import org.openecard.crypto.tls.TlsError;
import org.openecard.crypto.tls.auth.KeyStoreCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TLS server implementation with a keystore.
 * If no external keystore is given, a default PKCS12 keystore is loaded if available.
 *
 * @author Tobias Wich
 */
public class LocalKeystoreTlsServer extends AbstractTlsServer {

    private static final Logger logger = LoggerFactory.getLogger(LocalKeystoreTlsServer.class);

    private static final String KEYSTORE_FILE = "binding/server-keystore.p12";
    private static final String KEY_ALIAS = "www.localhost-ecard-client.de";
    private static KeyStore DEFAULT_KEYSTORE;

    static {
	try {
	    DEFAULT_KEYSTORE = loadKeyStore(KEYSTORE_FILE, getKeyPass(), "PKCS12", null);
	} catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException
		| NoSuchProviderException ex) {
	    logger.error("Failed to load default keystore.", ex);
	}
    }

    private KeyStore keyStore;
    private String alias;
    private char[] pass;

    public LocalKeystoreTlsServer() throws HttpsServiceError {
	if (DEFAULT_KEYSTORE == null) {
	    throw new HttpsServiceError("Default keystore not available.");
	}
	this.keyStore = DEFAULT_KEYSTORE;
	this.alias = KEY_ALIAS;
	this.pass = getKeyPass();
    }

    private static char[] getKeyPass() {
	try {
	    String name = "/cif" + "-repo/" + "CardInfo" + "_nPA_1-0-0" + ".xml";
	    InputStream in = FileUtils.resolveResourceAsStream(LocalKeystoreTlsServer.class, name);
	    byte[] bytes = FileUtils.toByteArray(in);
	    MessageDigest d = MessageDigest.getInstance("SHA-256");
	    d.update((byte) 23);
	    d.update(bytes);
	    d.update((byte) 42);
	    bytes = d.digest();
	    String pass = ByteUtils.toFileSafeBase64String(bytes);
	    return pass.toCharArray();
	} catch (IOException | NoSuchAlgorithmException ex) {
	    return null;
	}
    }

    public LocalKeystoreTlsServer(@Nonnull String fileName, @Nullable String keyPass, @Nonnull String alias,
	    @Nullable String aliasPass, @Nullable String type, @Nullable String provider) throws HttpsServiceError {
	try {
	    char[] keyPassChars = keyPass != null ? keyPass.toCharArray() : null;
	    this.keyStore = loadKeyStore(fileName, keyPassChars, type, provider);
	} catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException
		| NoSuchProviderException ex) {
	    throw new HttpsServiceError("Failed to load keystore.", ex);
	}
	this.alias = alias;
	if (aliasPass != null) {
	    this.pass = aliasPass.toCharArray();
	}
    }

    private static KeyStore loadKeyStore(String fileName, @Nullable char[] pass, @Nullable String type,
	    @Nullable String provider) throws IOException, KeyStoreException, CertificateException,
	    NoSuchAlgorithmException, NoSuchProviderException {
	// use bouncy castle keystore format, so that there are no problems on android
	InputStream ksStream = FileUtils.resolveResourceAsStream(LocalKeystoreTlsServer.class, fileName);
	if (type == null) {
	    type = KeyStore.getDefaultType();
	}
	KeyStore keyStore;
	if (provider != null) {
	    keyStore = KeyStore.getInstance(type, provider);
	} else {
	    keyStore = KeyStore.getInstance(type);
	}
	keyStore.load(ksStream, pass);
	return keyStore;
    }

    protected DHParameters getDHParameters() {
        return DHStandardGroups.rfc5114_2048_256;
    }

    protected TlsKeyExchange createDHEKeyExchange(int keyExchange) {
        return new TlsDHEKeyExchange(keyExchange, supportedSignatureAlgorithms, getDHParameters());
    }

    protected TlsKeyExchange createECDHEKeyExchange(int keyExchange) {
        return new TlsECDHEKeyExchange(keyExchange, supportedSignatureAlgorithms, namedCurves, clientECPointFormats,
            serverECPointFormats);
    }

    @Override
    protected ProtocolVersion getMaximumVersion() {
	return ProtocolVersion.TLSv12;
    }

    @Override
    protected ProtocolVersion getMinimumVersion() {
	return ProtocolVersion.TLSv10;
    }

    @Override
    public TlsCredentials getCredentials() throws IOException {
	KeyStoreSigner signer = new KeyStoreSigner(keyStore, pass, alias);
	TlsSignerCredentials credential = new KeyStoreCredential(signer, getSigAndHashAlg());
	return credential;
    }

    private SignatureAndHashAlgorithm getSigAndHashAlg() throws IOException {
	if (ProtocolVersion.TLSv12.isEqualOrEarlierVersionOf(getServerVersion())) {
	    if (supportedSignatureAlgorithms != null) {
		for (Object next : supportedSignatureAlgorithms) {
		    if (next instanceof SignatureAndHashAlgorithm) {
			SignatureAndHashAlgorithm nextSigHashAlg = (SignatureAndHashAlgorithm) next;
			if (nextSigHashAlg.getSignature() == SignatureAlgorithm.rsa) {
			    switch (nextSigHashAlg.getHash()) {
				case HashAlgorithm.sha512:
				case HashAlgorithm.sha384:
				case HashAlgorithm.sha256:
				case HashAlgorithm.sha1:
				    return nextSigHashAlg;
			    }
			}
		    }
		}
	    }
	    // no secure hash alg combination found
	    throw new TlsFatalAlert(AlertDescription.insufficient_security);
	}
	// TLS version < 1.2, use PKCS1 v1.5 with SHA1+MD5
	return null;
    }

    @Override
    protected int[] getCipherSuites() {
	// only list RSA based ciphers, we won't use ECDSA certificates
        return new int[] {
	    // recommended ciphers from TR-02102-2 sec. 3.3.1
	    CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,
	    // SHA1 is acceptable until 2015
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
	};
    }

    @Override
    public TlsCipher getCipher() throws IOException {
	switch (selectedCipherSuite) {
	    // CHACHA20
	    case CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256:
	    case CipherSuite.TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AEAD_CHACHA20_POLY1305, MACAlgorithm._null);
	    // AES 256 GCM
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_256_GCM, MACAlgorithm._null);
	    // AES 128 GCM
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_128_GCM, MACAlgorithm._null);
	    // AES 256 CBC SHA384
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_256_CBC, MACAlgorithm.hmac_sha384);
	    // AES 256 CBC SHA256
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_256_CBC, MACAlgorithm.hmac_sha256);
	    // AES 256 CBC SHA1
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_256_CBC, MACAlgorithm.hmac_sha1);
	    // AES 128 CBC SHA256
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256:
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_128_CBC, MACAlgorithm.hmac_sha256);
	    // AES 128 CBC SHA1
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
		return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_128_CBC, MACAlgorithm.hmac_sha1);
	    default:
		// Note: internal error here; selected a cipher suite we don't implement!
		throw new TlsFatalAlert(AlertDescription.internal_error);
	}
    }

    @Override
    public TlsKeyExchange getKeyExchange() throws IOException {
	switch (selectedCipherSuite) {
	    case CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256:
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384:
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256:
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384:
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA:
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA:
	    case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256:
		return createECDHEKeyExchange(KeyExchangeAlgorithm.ECDHE_RSA);
	    case CipherSuite.TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
	    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
		return createDHEKeyExchange(KeyExchangeAlgorithm.DHE_RSA);

	    default:
		// Note: internal error here; selected a key exchange we don't implement!
		throw new TlsFatalAlert(AlertDescription.internal_error);
	}
    }

    @Override
    public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Throwable cause) {
	TlsError error = new TlsError(alertLevel, alertDescription, message, cause);
	if (alertLevel == AlertLevel.warning && logger.isInfoEnabled()) {
	    logger.info("TLS warning sent.");
	    logger.info(error.toString());
	} else if (alertLevel == AlertLevel.fatal) {
	    logger.error("TLS error sent.");
	    logger.error(error.toString(), cause);
	}

	super.notifyAlertRaised(alertLevel, alertDescription, message, cause);
    }

    @Override
    public void notifyAlertReceived(short alertLevel, short alertDescription) {
	TlsError error = new TlsError(alertLevel, alertDescription);
	if (alertLevel == AlertLevel.warning && logger.isInfoEnabled()) {
	    logger.info("TLS warning received.");
	    logger.info(error.toString());
	} else if (alertLevel == AlertLevel.fatal) {
	    logger.error("TLS error received.");
	    logger.error(error.toString());
	}

	super.notifyAlertReceived(alertLevel, alertDescription);
    }

}
