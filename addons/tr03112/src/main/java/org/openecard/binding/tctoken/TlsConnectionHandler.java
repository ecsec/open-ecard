/****************************************************************************
 * Copyright (C) 2013-2016 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import generated.TCTokenType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.TlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsClientProtocol;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.crypto.common.sal.GenericCryptoSignerFinder;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertPSKTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.TlsPSKIdentityImpl;
import org.openecard.crypto.tls.auth.CredentialFactory;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.crypto.tls.verify.SameCertVerifier;
import org.openecard.crypto.tls.auth.SmartCardCredentialFactory;
import org.openecard.crypto.tls.proxy.ProxySettings;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.UrlBuilder;
import org.openecard.crypto.common.ReusableSecureRandom;
import org.openecard.crypto.tls.verify.JavaSecVerifier;


/**
 *
 * @author Tobias Wich
 */
public class TlsConnectionHandler {

    private final Dispatcher dispatcher;
    private final TCTokenRequest tokenRequest;
    private final ConnectionHandleType handle;

    private URL serverAddress;
    private String hostname;
    private int port;
    private String resource;
    private String sessionId;
    private ClientCertTlsClient tlsClient;
    private boolean verifyCertificates = true;

    public TlsConnectionHandler(Dispatcher dispatcher, TCTokenRequest tokenRequest, ConnectionHandleType handle)
	    throws ConnectionError {
	this.dispatcher = dispatcher;
	this.tokenRequest = tokenRequest;
	this.handle = handle;
    }

    public TlsConnectionHandler(Dispatcher dispatcher, TCTokenRequest tokenRequest)
	    throws ConnectionError {
	this(dispatcher, tokenRequest, null);
    }

    public void setUpClient() throws ConnectionError {
	try {
	    TCTokenType token = tokenRequest.getTCToken();
	    String cardType = null;
	    if (handle != null) {
		if (handle.getRecognitionInfo() != null) {
		    cardType = handle.getRecognitionInfo().getCardType();
		}
		if (cardType == null) {
		    cardType = tokenRequest.getCardType();
		}
	    }
	    // eID servers usually have problems with sni, so disable it for them
	    // TODO: check occasionally if this still holds
	    boolean doSni = ! "http://bsi.bund.de/cif/npa.xml".equals(cardType);

	    sessionId = token.getSessionIdentifier();
	    serverAddress = new URL(token.getServerAddress());
	    String serverHost = serverAddress.getHost();

	    if (Boolean.valueOf(OpenecardProperties.getProperty("legacy.session"))) {
		serverAddress = fixServerAddress(serverAddress, sessionId);
	    }

	    // extract connection parameters from endpoint
	    hostname = serverAddress.getHost();
	    port = serverAddress.getPort();
	    if (port == -1) {
		port = serverAddress.getDefaultPort();
	    }
	    resource = serverAddress.getFile();
	    resource = resource.isEmpty() ? "/" : resource;

	    String secProto = token.getPathSecurityProtocol();
	    // use same channel as demanded in TR-03124 sec. 2.4.3
	    if (isSameChannel()) {
		tlsClient = tokenRequest.getTokenContext().getTlsClient();
		if (tlsClient instanceof ClientCertDefaultTlsClient) {
		    ((ClientCertDefaultTlsClient) tlsClient).setEnforceSameSession(true);
		}
	    } else {
		// kill open channel in tctoken request, it is not needed anymore
		if (tokenRequest.getTokenContext() != null) {
		    tokenRequest.getTokenContext().closeStream();
		}

		// determine TLS version to use
		ProtocolVersion version = ProtocolVersion.TLSv12;
		ProtocolVersion minVersion = ProtocolVersion.TLSv12;
		switch (secProto) {
		    case "urn:ietf:rfc:5246":
			// no changes
			break;
		    case "urn:ietf:rfc:4279":
			minVersion = ProtocolVersion.TLSv11;
			break;
		}

		// Set up TLS connection
		DynamicAuthentication tlsAuth = new DynamicAuthentication(serverHost);

		switch (secProto) {
		    case "urn:ietf:rfc:4279":
			{
			    byte[] psk = token.getPathSecurityParameters().getPSK();
			    TlsPSKIdentity pskId = new TlsPSKIdentityImpl(sessionId.getBytes(), psk);
			    tlsClient = new ClientCertPSKTlsClient(pskId, serverHost, doSni);
			    tlsClient.setClientVersion(version);
			    tlsClient.setMinimumVersion(minVersion);
			    break;
			}
		    case "urn:ietf:rfc:5246":
			{
			    // use a smartcard for client authentication if needed
			    tlsAuth.setCredentialFactory(makeSmartCardCredential());
			    tlsClient = new ClientCertDefaultTlsClient(serverHost, doSni);
			    tlsClient.setClientVersion(version);
			    tlsClient.setMinimumVersion(minVersion);
			    // add PKIX verifier
			    if (verifyCertificates) {
				tlsAuth.addCertificateVerifier(new JavaSecVerifier());
			    }
			    break;
			}
		    default:
			throw new ConnectionError(UNKNOWN_SEC_PROTOCOL, secProto);
		}

		// make sure nobody changes the server when the connection gets reestablished
		tlsAuth.addCertificateVerifier(new SameCertVerifier());
		// save eService certificate for use in EAC
		tlsAuth.addCertificateVerifier(new SaveEServiceCertHandler());

		// set the authentication class in the tls client
		tlsClient.setAuthentication(tlsAuth);
	    }

	} catch (MalformedURLException ex) {
	    throw new ConnectionError(MALFORMED_URL, ex, "ServerAddress");
	}
    }

    public boolean isSameChannel() {
	TCTokenType token = tokenRequest.getTCToken();
	String secProto = token.getPathSecurityProtocol();
	// check security proto
	if (secProto == null || "".equals(secProto)) {
	    return true;
	}
	// check PSK value
	if (secProto.equals("urn:ietf:rfc:4279")) {
	    TCTokenType.PathSecurityParameters pathsecParams = token.getPathSecurityParameters();
	    return pathsecParams == null || pathsecParams.getPSK() == null || pathsecParams.getPSK().length == 0;
	} else {
	    return false;
	}
    }

    public void setVerifyCertificates(boolean verifyCertificates) {
	this.verifyCertificates = verifyCertificates;
    }

    public URL getServerAddress() {
	return serverAddress;
    }

    public String getHostname() {
	return hostname;
    }

    public int getPort() {
	return port;
    }

    public String getResource() {
	return resource;
    }

    public String getSessionId() {
	return sessionId;
    }

    public TlsClient getTlsClient() {
	return tlsClient;
    }

    public TlsClientProtocol createTlsConnection() throws IOException, URISyntaxException {
	return createTlsConnection(tlsClient.getClientVersion());
    }
    public TlsClientProtocol createTlsConnection(ProtocolVersion tlsVersion)
	    throws IOException, URISyntaxException {
	if (! isSameChannel()) {
	    // normal procedure, create a new channel
	    return createNewTlsConnection(tlsVersion);
	} else {
	    // if something fucks up the channel we may try session resumption
	    TlsClientProtocol proto = tokenRequest.getTokenContext().getTlsClientProto();
	    if (proto.isClosed()) {
		return createNewTlsConnection(tlsVersion);
	    } else {
		return proto;
	    }
	}
    }

    private TlsClientProtocol createNewTlsConnection(ProtocolVersion tlsVersion) throws IOException, URISyntaxException {
	Socket socket = ProxySettings.getDefault().getSocket("https", hostname, port);
	tlsClient.setClientVersion(tlsVersion);
	// TLS
	InputStream sockIn = socket.getInputStream();
	OutputStream sockOut = socket.getOutputStream();
	SecureRandom sr = ReusableSecureRandom.getInstance();
	TlsClientProtocol handler = new TlsClientProtocol(sockIn, sockOut, sr);
	handler.connect(tlsClient);

	return handler;
    }

    private static URL fixServerAddress(URL serverAddress, String sessionIdentifier) throws MalformedURLException {
	// FIXME: remove this hilariously stupid bull*#@%&/ code which satisfies a mistake introduced by the AA
	try {
	    UrlBuilder b = UrlBuilder.fromUrl(serverAddress);
	    return b.queryParam("sessionid", sessionIdentifier, false).build().toURL();
	} catch (URISyntaxException ex) {
	    throw new MalformedURLException(ex.getMessage());
	}
    }

    @Nullable
    private CredentialFactory makeSmartCardCredential() {
	if (handle != null) {
	    GenericCryptoSignerFinder finder = new GenericCryptoSignerFinder(dispatcher, handle, false);
	    SmartCardCredentialFactory scFac = new SmartCardCredentialFactory(finder);
	    return scFac;
	} else {
	    return null;
	}
    }

}
