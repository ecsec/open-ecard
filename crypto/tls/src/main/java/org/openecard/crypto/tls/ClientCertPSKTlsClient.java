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

package org.openecard.crypto.tls;

import org.openecard.crypto.tls.auth.DynamicAuthentication;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.tls.AlertLevel;
import org.openecard.bouncycastle.tls.CipherSuite;
import org.openecard.bouncycastle.tls.ECPointFormat;
import org.openecard.bouncycastle.tls.HashAlgorithm;
import org.openecard.bouncycastle.tls.NameType;
import org.openecard.bouncycastle.tls.NamedGroup;
import org.openecard.bouncycastle.tls.PSKTlsClient;
import org.openecard.bouncycastle.tls.ProtocolVersion;
import org.openecard.bouncycastle.tls.ServerName;
import org.openecard.bouncycastle.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.tls.TlsAuthentication;
import org.openecard.bouncycastle.tls.TlsDHUtils;
import org.openecard.bouncycastle.tls.TlsECCUtils;
import org.openecard.bouncycastle.tls.TlsExtensionsUtils;
import org.openecard.bouncycastle.tls.TlsPSKIdentity;
import org.openecard.bouncycastle.tls.TlsUtils;
import org.openecard.bouncycastle.tls.crypto.TlsCrypto;
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

    private static final Logger LOG = LoggerFactory.getLogger(ClientCertPSKTlsClient.class);

    private final String host;
    private TlsAuthentication tlsAuth;

    protected List<ServerName> serverNames;
    protected ProtocolVersion clientVersion = ProtocolVersion.TLSv12;
    protected ProtocolVersion minClientVersion = ProtocolVersion.TLSv10;

    /**
     * Create a ClientCertPSKTlsClient for the given parameters.
     *
     * @param tcf Cipher factory to use in this client.
     * @param pskId PSK to use for this connection.
     * @param host Host or IP address. Value must not be null.
     * @param doSni Control whether the server should send the SNI Header in the Client Hello.
     */
    public ClientCertPSKTlsClient(@Nonnull TlsCrypto tcf, @Nonnull TlsPSKIdentity pskId, @Nullable String host,
	    boolean doSni) {
	super(tcf, pskId);
	if (doSni) {
	    this.serverNames = Collections.singletonList(makeServerName(host));
	}
	boolean tls1 = Boolean.valueOf(OpenecardProperties.getProperty("legacy.tls1"));
	this.minClientVersion = tls1 ? ProtocolVersion.TLSv10 : ProtocolVersion.TLSv11;
	this.host = host;
    }

    public void setServerName(@Nonnull String serverName) {
	serverNames = Collections.singletonList(makeServerName(serverName));
    }

    public void setServerNames(@Nonnull List<String> serverNames) {
	this.serverNames = new ArrayList<>();
	for (String next : serverNames) {
	    this.serverNames.add(makeServerName(next));
	}
    }

    @Override
    protected Vector getSNIServerNames() {
	return serverNames != null ? new Vector(serverNames) : null;
    }

    private ServerName makeServerName(String name) {
	return new ServerName(NameType.host_name, name);
    }

    @Override
    public ProtocolVersion getClientVersion() {
	return this.clientVersion;
    }

    @Override
    public void setClientVersion(ProtocolVersion version) {
	this.clientVersion = version;
    }

    @Override
    public void setMinimumVersion(ProtocolVersion minClientVersion) {
	this.minClientVersion = minClientVersion;
    }

    @Override
    public ProtocolVersion getMinimumVersion() {
	return this.minClientVersion;
    }


    @Override
    public int[] getCipherSuites() {
	ArrayList<Integer> ciphers = new ArrayList<>(Arrays.asList(
		// recommended ciphers from TR-02102-2 sec. 3.3.1
		CipherSuite.TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384,
		CipherSuite.TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256,
		CipherSuite.TLS_DHE_PSK_WITH_AES_256_GCM_SHA384,
		CipherSuite.TLS_DHE_PSK_WITH_AES_128_GCM_SHA256,
		CipherSuite.TLS_DHE_PSK_WITH_AES_256_CBC_SHA384,
		CipherSuite.TLS_DHE_PSK_WITH_AES_128_CBC_SHA256,
		CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384,
		CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256,
		// TODO: see if this still holds: this cipher suite does not work with the governikus eID server, so it is excluded here
		CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384,
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
    protected Vector getSupportedSignatureAlgorithms() {
	TlsCrypto crypto = context.getCrypto();
        short[] hashAlgorithms = new short[]{ HashAlgorithm.sha512, HashAlgorithm.sha384, HashAlgorithm.sha256,
	    HashAlgorithm.sha224 };
        short[] signatureAlgorithms = new short[]{ SignatureAlgorithm.rsa, SignatureAlgorithm.ecdsa };

        Vector result = new Vector();
        for (int i = 0; i < signatureAlgorithms.length; ++i)
        {
            for (int j = 0; j < hashAlgorithms.length; ++j)
            {
                SignatureAndHashAlgorithm alg = new SignatureAndHashAlgorithm(hashAlgorithms[j], signatureAlgorithms[i]);
                if (crypto.hasSignatureAndHashAlgorithm(alg))
                {
                    result.addElement(alg);
                }
            }
        }
        return result;
    }

    @Override
    public Hashtable getClientExtensions() throws IOException {
	Hashtable clientExtensions = super.getClientExtensions();
	clientExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(clientExtensions);

	// code taken from AbstractTlsClient, if that should ever change modify it here too
	Vector supportedGroups = new Vector();
	if (TlsECCUtils.containsECCipherSuites(getCipherSuites())) {
	    // other possible parameters TR-02102-2 sec. 3.6
            supportedGroups.add(NamedGroup.brainpoolP512r1);
	    supportedGroups.add(NamedGroup.brainpoolP384r1);
	    supportedGroups.add(NamedGroup.secp384r1);
	    // required parameters TR-03116-4 sec. 4.1.4
	    supportedGroups.add(NamedGroup.brainpoolP256r1);
	    supportedGroups.add(NamedGroup.secp256r1);
	    supportedGroups.add(NamedGroup.secp224r1);

	    this.clientECPointFormats = new short[]{
		ECPointFormat.ansiX962_compressed_prime, ECPointFormat.uncompressed
	    };

	    TlsECCUtils.addSupportedPointFormatsExtension(clientExtensions, clientECPointFormats);
	}
        if (TlsDHUtils.containsDHECipherSuites(getCipherSuites())) {
	    // RFC 7919
            supportedGroups.addElement(NamedGroup.ffdhe2048);
            supportedGroups.addElement(NamedGroup.ffdhe3072);
            supportedGroups.addElement(NamedGroup.ffdhe4096);
            supportedGroups.addElement(NamedGroup.ffdhe6144);
            supportedGroups.addElement(NamedGroup.ffdhe8192);
        }

	if (! supportedGroups.isEmpty()) {
	    this.supportedGroups = supportedGroups;
	    TlsExtensionsUtils.addSupportedGroupsExtension(clientExtensions, supportedGroups);
	}

	return clientExtensions;
    }

    @Override
    public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Throwable cause) {
	TlsError error = new TlsError(alertLevel, alertDescription, message, cause);
	if (alertLevel == AlertLevel.warning && LOG.isInfoEnabled()) {
	    LOG.info("TLS warning sent.");
	    if (LOG.isDebugEnabled()) {
		LOG.info(error.toString(), cause);
	    } else {
		LOG.info(error.toString());
	    }
	} else if (alertLevel == AlertLevel.fatal) {
	    LOG.error("TLS error sent.");
	    LOG.error(error.toString(), cause);
	}

	super.notifyAlertRaised(alertLevel, alertDescription, message, cause);
    }

    @Override
    public void notifyAlertReceived(short alertLevel, short alertDescription) {
	TlsError error = new TlsError(alertLevel, alertDescription);
	if (alertLevel == AlertLevel.warning && LOG.isInfoEnabled()) {
	    LOG.info("TLS warning received.");
	    LOG.info(error.toString());
	} else if (alertLevel == AlertLevel.fatal) {
	    LOG.error("TLS error received.");
	    LOG.error(error.toString());
	}

	super.notifyAlertReceived(alertLevel, alertDescription);
    }

    @Override
    public void notifySecureRenegotiation(boolean secureRenegotiation) throws IOException {
	// pretend we accept it
    }

}
