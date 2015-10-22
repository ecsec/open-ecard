/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.crypto.tls;

import org.openecard.crypto.tls.auth.DynamicAuthentication;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.AlertLevel;
import org.openecard.bouncycastle.crypto.tls.CipherSuite;
import org.openecard.bouncycastle.crypto.tls.ECPointFormat;
import org.openecard.bouncycastle.crypto.tls.HashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.NamedCurve;
import org.openecard.bouncycastle.crypto.tls.PSKTlsClient;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCipherFactory;
import org.openecard.bouncycastle.crypto.tls.TlsECCUtils;
import org.openecard.bouncycastle.crypto.tls.TlsExtensionsUtils;
import org.openecard.bouncycastle.crypto.tls.TlsKeyExchange;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;
import org.openecard.bouncycastle.crypto.tls.TlsUtils;
import org.openecard.common.OpenecardProperties;
import org.openecard.crypto.tls.auth.ContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PSK TLS client also implementing the ClientCertTlsClient interface. <br>
 * If not modified, the TlsAuthentication instance returned by {@link #getAuthentication()} is of type
 * {@link DynamicAuthentication} without further modifications.
 *
 * @author Tobias Wich
 */
public class ClientCertPSKTlsClient extends PSKTlsClient implements ClientCertTlsClient {

    private static final Logger logger = LoggerFactory.getLogger(ClientCertPSKTlsClient.class);

    private final String host;
    private TlsAuthentication tlsAuth;

    /**
     * Create a ClientCertPSKTlsClient for the given parameters.
     *
     * @param pskId PSK to use for this connection.
     * @param host Host or IP address. Value must not be null.
     * @param doSni Control whether the server should send the SNI Header in the Client Hello.
     */
    public ClientCertPSKTlsClient(@Nonnull TlsPSKIdentity pskId, @Nonnull String host, boolean doSni) {
	super(pskId);
	if (doSni) {
	    setServerName(host);
	}
	boolean tls1 = Boolean.valueOf(OpenecardProperties.getProperty("legacy.tls1"));
	setMinimumVersion(tls1 ? ProtocolVersion.TLSv10 : ProtocolVersion.TLSv11);
	this.host = host;
    }
    /**
     * Create a ClientCertPSKTlsClient for the given parameters.
     *
     * @param tcf Cipher factory to use in this client.
     * @param pskId PSK to use for this connection.
     * @param host Host or IP address. Value must not be null.
     * @param doSni Control whether the server should send the SNI Header in the Client Hello.
     */
    public ClientCertPSKTlsClient(@Nonnull TlsCipherFactory tcf, @Nonnull TlsPSKIdentity pskId, @Nonnull String host,
	    boolean doSni) {
	super(tcf, pskId);
	if (doSni) {
	    setServerName(host);
	}
	boolean tls1 = Boolean.valueOf(OpenecardProperties.getProperty("legacy.tls1"));
	setMinimumVersion(tls1 ? ProtocolVersion.TLSv10 : ProtocolVersion.TLSv11);
	this.host = host;
    }


    @Override
    public int[] getCipherSuites() {
	ArrayList<Integer> ciphers = new ArrayList<>(Arrays.asList(
		// recommended ciphers from TR-02102-2 sec. 3.3.1
		CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384,
		// this cipher suite does not work with the governikus eID server, so it is excluded here
		// CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384,
		CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256,
		CipherSuite.TLS_RSA_PSK_WITH_AES_128_CBC_SHA256,
		// must have according to TR-03124-1 sec. 4.4
		CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA
	));

	// remove unsupported cipher suites
	Iterator<Integer> it = ciphers.iterator();
	while (it.hasNext()) {
	    Integer cipher = it.next();
	    if (! TlsUtils.isValidCipherSuiteForVersion(cipher, clientVersion)) {
		it.remove();
	    }
	}
	// copy to array
	int[] result = new int[ciphers.size()];
	for (int i = 0; i < ciphers.size(); i++) {
	    result[i] = ciphers.get(i);
	}
	return result;
    }

    @Override
    protected TlsKeyExchange createPSKKeyExchange(int keyExchange) {
	if (! Boolean.valueOf(OpenecardProperties.getProperty("legacy.weak_crypto"))) {
	    return new TlsPSKKeyExchangeStrengthCheck(keyExchange, supportedSignatureAlgorithms, pskIdentity, null,
		    null, namedCurves, clientECPointFormats, serverECPointFormats);
	} else {
	    return super.createPSKKeyExchange(keyExchange);
	}
    }

    @Override
    public synchronized TlsAuthentication getAuthentication() throws IOException {
	if (tlsAuth == null) {
	    tlsAuth = new DynamicAuthentication(host);
	}
	if (tlsAuth instanceof ContextAware) {
	    ((ContextAware) tlsAuth).setContext(context);
	}
	return tlsAuth;
    }

    @Override
    public synchronized void setAuthentication(TlsAuthentication tlsAuth) {
	this.tlsAuth = tlsAuth;
    }

    @Override
    public Hashtable getClientExtensions() throws IOException {
	Hashtable clientExtensions = super.getClientExtensions();
	clientExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(clientExtensions);

	// code taken from AbstractTlsClient, if that should ever change modify it here too
	if (TlsECCUtils.containsECCCipherSuites(getCipherSuites())) {
            this.namedCurves = new int[] {
		// required parameters TR-03116-4 sec. 4.1.4
		NamedCurve.secp224r1, NamedCurve.secp256r1, NamedCurve.brainpoolP256r1,
		// other possible parameters TR-02102-2 sec. 3.6
		NamedCurve.secp384r1, NamedCurve.brainpoolP384r1, NamedCurve.brainpoolP512r1,
	    };
	    this.clientECPointFormats = new short[]{
		ECPointFormat.ansiX962_compressed_prime, ECPointFormat.uncompressed
	    };

            TlsECCUtils.addSupportedEllipticCurvesExtension(clientExtensions, namedCurves);
	    TlsECCUtils.addSupportedPointFormatsExtension(clientExtensions, clientECPointFormats);
	}

	// overwrite hash and signature algorithms
        if (TlsUtils.isSignatureAlgorithmsExtensionAllowed(clientVersion)) {
            short[] hashAlgorithms = new short[]{ HashAlgorithm.sha512, HashAlgorithm.sha384, HashAlgorithm.sha256,
                HashAlgorithm.sha224 };

            short[] signatureAlgorithms = new short[]{ SignatureAlgorithm.rsa, SignatureAlgorithm.ecdsa };

            this.supportedSignatureAlgorithms = new Vector();
            for (int i = 0; i < hashAlgorithms.length; ++i) {
                for (int j = 0; j < signatureAlgorithms.length; ++j) {
                    this.supportedSignatureAlgorithms.addElement(new SignatureAndHashAlgorithm(hashAlgorithms[i],
                        signatureAlgorithms[j]));
                }
            }

            clientExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(clientExtensions);

            TlsUtils.addSignatureAlgorithmsExtension(clientExtensions, supportedSignatureAlgorithms);
        }

	return clientExtensions;
    }

    @Override
    public void notifySecureRenegotiation(boolean secureRenegotiation) throws IOException {
	// allow renegotiation
    }

    @Override
    public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Throwable cause) {
	TlsError error = new TlsError(alertLevel, alertDescription, message, cause);
	if (alertLevel == AlertLevel.warning && logger.isInfoEnabled()) {
	    logger.info("TLS warning sent.");
	    if (logger.isDebugEnabled()) {
		logger.info(error.toString(), cause);
	    } else {
		logger.info(error.toString());
	    }
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
