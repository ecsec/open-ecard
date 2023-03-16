/****************************************************************************
 * Copyright (C) 2016-2017 ecsec GmbH.
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

package org.openecard.addons.cg.activate;

import org.openecard.addons.cg.ex.ConnectionError;
import org.openecard.addons.cg.tctoken.TCToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import org.openecard.bouncycastle.tls.ProtocolVersion;
import org.openecard.bouncycastle.tls.TlsClient;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.crypto.tls.verify.SameCertVerifier;
import org.openecard.crypto.tls.proxy.ProxySettings;
import static org.openecard.addons.cg.ex.ErrorTranslations.*;
import static org.openecard.common.ECardConstants.PATH_SEC_PROTO_MTLS;

import org.openecard.addons.cg.ex.InvalidTCTokenElement;
import org.openecard.addons.cg.impl.ChipGatewayProperties;
import org.openecard.bouncycastle.tls.crypto.TlsCrypto;
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.openecard.crypto.common.ReusableSecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class TlsConnectionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TlsConnectionHandler.class);

    private final TCToken token;

    private URL serverAddress;
    private String hostname;
    private int port;
    private String resource;
    private String sessionId;
    private ClientCertTlsClient tlsClient;

    public TlsConnectionHandler(TCToken token)
	    throws ConnectionError {
	this.token = token;
    }

    public void setUpClient() throws InvalidTCTokenElement {
	try {
	    sessionId = token.getSessionIdentifier();
	    serverAddress = new URL(token.getServerAddress());
	    String serverHost = serverAddress.getHost();

	    // extract connection parameters from endpoint
	    hostname = serverAddress.getHost();
	    port = serverAddress.getPort();
	    if (port == -1) {
		port = serverAddress.getDefaultPort();
	    }
	    resource = serverAddress.getFile();
	    resource = resource.isEmpty() ? "/" : resource;

	    String secProto = token.getPathSecurityProtocol();

	    // determine TLS version to use
	    ProtocolVersion version = ProtocolVersion.TLSv12;
	    ProtocolVersion minVersion = ProtocolVersion.TLSv12;
	    switch (secProto) {
		case PATH_SEC_PROTO_MTLS:
		case "http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption":
		    // no changes
		    break;
	    }

	    // Set up TLS connection
	    DynamicAuthentication tlsAuth = new DynamicAuthentication(serverHost);

	    switch (secProto) {
		case PATH_SEC_PROTO_MTLS:
		case "http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption":
		    {
			// use a smartcard for client authentication if needed
			TlsCrypto crypto = new BcTlsCrypto(ReusableSecureRandom.getInstance());
			tlsClient = new ClientCertDefaultTlsClient(crypto, serverHost, true);
			tlsClient.setClientVersion(version);
			tlsClient.setMinimumVersion(minVersion);
			// add PKIX verifier
			if (ChipGatewayProperties.isValidateServerCert()) {
			    tlsAuth.addCertificateVerifier(new CGJavaSecVerifier());
			} else {
			    LOG.warn("Skipping server certificate validation of the ChipGateway server.");
			}
			break;
		    }
		default:
		    throw new InvalidTCTokenElement(ELEMENT_VALUE_INVALID, "PathSecurity-Protocol");
	    }

	    // make sure nobody changes the server when the connection gets reestablished
	    tlsAuth.addCertificateVerifier(new SameCertVerifier());

	    // set the authentication class in the tls client
	    tlsClient.setAuthentication(tlsAuth);

	} catch (MalformedURLException ex) {
	    throw new InvalidTCTokenElement(MALFORMED_URL, "ServerAddress");
	}
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

    public TlsClientProtocol createTlsConnection(ProtocolVersion tlsVersion) throws IOException, URISyntaxException {
	// normal procedure, create a new channel
	Socket socket = ProxySettings.getDefault().getSocket("https", hostname, port);
	tlsClient.setClientVersion(tlsVersion);
	// TLS
	InputStream sockIn = socket.getInputStream();
	OutputStream sockOut = socket.getOutputStream();
	TlsClientProtocol handler = new TlsClientProtocol(sockIn, sockOut);
	handler.connect(tlsClient);

	return handler;
    }

}
