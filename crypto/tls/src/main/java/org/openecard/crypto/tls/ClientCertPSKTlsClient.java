/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.tls.AlertLevel;
import org.openecard.bouncycastle.tls.CipherSuite;
import org.openecard.bouncycastle.tls.HashAlgorithm;
import org.openecard.bouncycastle.tls.NameType;
import org.openecard.bouncycastle.tls.NamedGroup;
import org.openecard.bouncycastle.tls.NamedGroupRole;
import org.openecard.bouncycastle.tls.PSKTlsClient;
import org.openecard.bouncycastle.tls.ProtocolVersion;
import org.openecard.bouncycastle.tls.ServerName;
import org.openecard.bouncycastle.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.openecard.bouncycastle.tls.TlsAuthentication;
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

    protected ArrayList<ServerName> serverNames;
    protected ProtocolVersion clientVersion = ProtocolVersion.TLSv12;
    protected ProtocolVersion minClientVersion = ProtocolVersion.TLSv12;

    /**
     * Create a ClientCertPSKTlsClient for the given parameters.
     *
     * @param tcf Cipher factory to use in this client.
     * @param pskId PSK to use for this connection.
     * @param host Host or IP address. Value must not be null.
     * @param doSni Control whether the server should send the SNI Header in the Client Hello.
     */
    public ClientCertPSKTlsClient(@Nonnull TlsCrypto tcf, @Nonnull TlsPSKIdentity pskId, @Nullable String host, boolean doSni) {
	super(tcf, pskId);
	this.serverNames = new ArrayList<>();
	if (doSni) {
	    this.serverNames.add(makeServerName(host));
	}
	boolean legacyTls = Boolean.valueOf(OpenecardProperties.getProperty("legacy.tls1"));
	if (legacyTls) {
	    this.minClientVersion = ProtocolVersion.TLSv10;
	}
	this.host = host;
    }

    public void setServerName(@Nonnull String serverName) {
	serverNames.clear();
	serverNames.add(makeServerName(serverName));
    }

    public void setServerNames(@Nonnull List<String> serverNames) {
	this.serverNames.clear();
	for (String next : serverNames) {
	    this.serverNames.add(makeServerName(next));
	}
    }

    @Override
    protected Vector getSNIServerNames() {
	return serverNames.isEmpty() ? null : new Vector(serverNames);
    }

    private ServerName makeServerName(String name) {
	name = IDN.toASCII(name);
	return new ServerName(NameType.host_name, name.getBytes(StandardCharsets.US_ASCII));
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

    public ProtocolVersion getMinimumVersion() {
	return this.minClientVersion;
    }

    @Override
    public ProtocolVersion[] getSupportedVersions() {
	ProtocolVersion desiredVersion = getClientVersion();
	ProtocolVersion minVersion = getMinimumVersion();

	if (! desiredVersion.isLaterVersionOf(minVersion)) {
	    return new ProtocolVersion[] { desiredVersion };
	} else {
	    return getClientVersion().downTo(minVersion);
	}
    }

    @Override
    public int[] getCipherSuites() {
	ArrayList<Integer> ciphers = new ArrayList<>(Arrays.asList(
		// recommended ciphers from TR-02102-2 sec. 3.3.1
		CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384,
		CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256,
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

	    Vector result = ClientCertDefaultTlsClient.getDefaultSignatureAlgorithms(crypto, minClientVersion == ProtocolVersion.TLSv10);
	    return result;
    }

    @Override
    protected Vector getSupportedGroups(Vector namedGroupRoles) {
        Vector groups = new Vector();

        if (namedGroupRoles.contains(NamedGroupRole.ecdh) || namedGroupRoles.contains(NamedGroupRole.ecdsa)) {
	    // other possible parameters TR-02102-2 sec. 3.6
            groups.add(NamedGroup.brainpoolP512r1);
	    groups.add(NamedGroup.brainpoolP384r1);
	    groups.add(NamedGroup.secp384r1);
	    // required parameters TR-03116-4 sec. 4.1.4
	    groups.add(NamedGroup.brainpoolP256r1);
	    groups.add(NamedGroup.secp256r1);
	    groups.add(NamedGroup.secp224r1);
        }

        return groups;
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
