/****************************************************************************
 * Copyright (C) 2013-2017 ecsec GmbH.
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
import javax.annotation.Nullable;
import org.openecard.bouncycastle.tls.ProtocolVersion;
import org.openecard.bouncycastle.tls.TlsClient;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.bouncycastle.tls.TlsPSKIdentity;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertPSKTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.auth.CredentialFactory;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.crypto.tls.verify.SameCertVerifier;
import org.openecard.crypto.tls.auth.SmartCardCredentialFactory;
import org.openecard.crypto.tls.proxy.ProxySettings;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import static org.openecard.common.ECardConstants.*;

import org.openecard.bouncycastle.tls.BasicTlsPSKIdentity;
import org.openecard.bouncycastle.tls.crypto.TlsCrypto;
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.openecard.common.DynamicContext;
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
	    // use same channel as demanded in TR-03124 sec. 2.4.3
	    if (tokenRequest.isSameChannel()) {
		tlsClient = tokenRequest.getTokenContext().getTlsClient();
		if (tlsClient instanceof ClientCertDefaultTlsClient) {
		    ((ClientCertDefaultTlsClient) tlsClient).setEnforceSameSession(true);
		}
		// save the info that we have a same channel situtation
		DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
		dynCtx.put(TR03112Keys.SAME_CHANNEL, Boolean.TRUE);
	    } else {
		// kill open channel in tctoken request, it is not needed anymore
		if (tokenRequest.getTokenContext() != null) {
		    tokenRequest.getTokenContext().closeStream();
		}

		// Set up TLS connection
		DynamicAuthentication tlsAuth = new DynamicAuthentication(serverHost);

		TlsCrypto crypto = new BcTlsCrypto(ReusableSecureRandom.getInstance());
		switch (secProto) {
		    case PATH_SEC_PROTO_TLS_PSK:
			{
			    byte[] psk = token.getPathSecurityParameters().getPSK();
			    TlsPSKIdentity pskId = new BasicTlsPSKIdentity(sessionId, psk);
			    tlsClient = new ClientCertPSKTlsClient(crypto, pskId, serverHost, true);
			    break;
			}
		    case PATH_SEC_PROTO_MTLS:
			{
			    // use a smartcard for client authentication if needed
			    tlsAuth.setCredentialFactory(makeSmartCardCredential());
			    tlsClient = new ClientCertDefaultTlsClient(crypto, serverHost, true);
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
		tlsAuth.addCertificateVerifier(new SaveEidServerCertHandler());

		// set the authentication class in the tls client
		tlsClient.setAuthentication(tlsAuth);
	    }

	} catch (MalformedURLException ex) {
	    throw new ConnectionError(MALFORMED_URL, ex, "ServerAddress");
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
	if (! tokenRequest.isSameChannel()) {
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
	TlsClientProtocol handler = new TlsClientProtocol(sockIn, sockOut);
	handler.connect(tlsClient);

	return handler;
    }

    @Nullable
    private CredentialFactory makeSmartCardCredential() {
	if (handle != null) {
	    SmartCardCredentialFactory scFac = new SmartCardCredentialFactory(dispatcher, handle, true);
	    return scFac;
	} else {
	    return null;
	}
    }

}
