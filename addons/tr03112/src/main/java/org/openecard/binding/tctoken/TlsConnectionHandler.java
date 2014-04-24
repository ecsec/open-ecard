/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import org.openecard.crypto.tls.auth.SmartCardCredentialFactory;
import org.openecard.crypto.tls.proxy.ProxySettings;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
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
    private boolean usesTls = false;

    public TlsConnectionHandler(Dispatcher dispatcher, TCTokenRequest tokenRequest, ConnectionHandleType handle)
	    throws ConnectionError {
	this.dispatcher = dispatcher;
	this.tokenRequest = tokenRequest;
	this.handle = handle;
    }

    public void setUpClient() throws ConnectionError {
	try {
	    TCTokenType token = tokenRequest.getTCToken();
	    String cardType = null;
	    if (handle.getRecognitionInfo() != null) {
		cardType = handle.getRecognitionInfo().getCardType();
	    }
	    if (cardType == null) {
		cardType = tokenRequest.getCardType();
	    }
	    // eID servers usually have problems with sni, so disable it for them
	    boolean noSni = "http://bsi.bund.de/cif/npa.xml".equals(cardType);

	    sessionId = token.getSessionIdentifier();
	    serverAddress = new URL(token.getServerAddress());
	    String serverHost = serverAddress.getHost();
	    String secProto = token.getPathSecurityProtocol();

	    serverAddress = fixServerAddress(serverAddress, sessionId);

	    // extract connection parameters from endpoint
	    hostname = serverAddress.getHost();
	    port = serverAddress.getPort();
	    if (port == -1) {
		port = serverAddress.getDefaultPort();
	    }
	    resource = serverAddress.getFile();

	    // TODO: remove this workaround as soon as eGK server uses HTTPS
	    if (serverAddress.getProtocol().equals("http")) {
		usesTls = false;
		return;
	    } else {
		usesTls = true;
	    }

	    // Set up TLS connection
	    if (secProto.equals("urn:ietf:rfc:4279") || secProto.equals("urn:ietf:rfc:5487")) {
		DynamicAuthentication tlsAuth = new DynamicAuthentication();
		tlsAuth.setHostname(serverHost);
		// FIXME: verify certificate chain as soon as a usable solution exists fpr the trust problem
		//tlsAuth.setCertificateVerifier(new JavaSecVerifier());
		byte[] psk = token.getPathSecurityParameters().getPSK();
		TlsPSKIdentity pskId = new TlsPSKIdentityImpl(sessionId.getBytes(), psk);
		tlsClient = new ClientCertPSKTlsClient(pskId, noSni ? null : serverHost);
		tlsClient.setAuthentication(tlsAuth);
		tlsClient.setClientVersion(ProtocolVersion.TLSv12);
	    } else if (secProto.equals("urn:ietf:rfc:4346")) {
		DynamicAuthentication tlsAuth = new DynamicAuthentication();
		tlsAuth.setHostname(serverHost);
		// use a smartcard for client authentication if needed
		tlsAuth.setCredentialFactory(makeSmartCardCredential());
		// FIXME: verify certificate chain as soon as a usable solution exists fpr the trust problem
		//tlsAuth.setCertificateVerifier(new JavaSecVerifier());
		tlsClient = new ClientCertDefaultTlsClient(noSni ? null : serverHost);
		tlsClient.setAuthentication(tlsAuth);
		tlsClient.setClientVersion(ProtocolVersion.TLSv12);
	    } else {
		throw new ConnectionError("Unknow security protocol '" + secProto + "' requested.");
	    }

	} catch (MalformedURLException ex) {
	    throw new ConnectionError(ex);
	}
    }

    public boolean usesTls() {
	return usesTls;
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
	Socket socket = ProxySettings.getDefault().getSocket(hostname, port);
	tlsClient.setClientVersion(tlsVersion);
	// TLS
	InputStream sockIn = socket.getInputStream();
	OutputStream sockOut = socket.getOutputStream();
	TlsClientProtocol handler = new TlsClientProtocol(sockIn, sockOut);
	handler.connect(tlsClient);

	return handler;
    }

    private static URL fixServerAddress(URL serverAddress, String sessionIdentifier) throws MalformedURLException {
	URL realServerAddress = serverAddress;
	// FIXME: remove this hilariously stupid bull*#@%&/ code which satisfies a mistake introduced by the AA
	String queryPart = serverAddress.getQuery();
	if (queryPart == null || ! (queryPart.contains("?sessionid=") || queryPart.contains("&sessionid="))) {
	    String sAddr = serverAddress.toString();
	    // fix path of url
	    if (serverAddress.getPath().isEmpty()) {
		sAddr += "/";
	    }
	    // add parameter
	    if (sAddr.endsWith("?")) {
		sAddr += "sessionid=" + sessionIdentifier;
	    } else if (sAddr.contains("?")) {
		sAddr += "&sessionid=" + sessionIdentifier;
	    } else {
		sAddr += "?sessionid=" + sessionIdentifier;
	    }
	    realServerAddress = new URL(sAddr);
	}
	// END: ugly fix

	return realServerAddress;
    }

    private CredentialFactory makeSmartCardCredential() {
	GenericCryptoSignerFinder finder = new GenericCryptoSignerFinder(dispatcher, handle, false);
	SmartCardCredentialFactory scFac = new SmartCardCredentialFactory(finder);
	return scFac;
    }

}
