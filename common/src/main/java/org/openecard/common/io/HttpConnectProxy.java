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

package org.openecard.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.crypto.tls.TlsClientProtocol;
import org.openecard.bouncycastle.util.encoders.Base64;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.SocketWrapper;
import org.openecard.crypto.tls.TlsNoAuthentication;
import org.openecard.crypto.tls.verify.JavaSecVerifier;


/**
 * HTTP proxy supporting only CONNECT tunnel.
 * This class is initialised with the proxy parameters and can then establish a tunnel with the getSocket method.
 * The authentication parameters are optional. Scheme must be one of http and https.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class HttpConnectProxy extends Proxy {

    private final String proxyScheme;
    private final boolean proxyValidate;
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUser;
    private final String proxyPass;

    /**
     * Create a HTTP proxy instance configured with the given values.
     * This method does not perform any reachability checks, it only saves the values for later use.
     *
     * @param proxyScheme HTTP or HTTPS
     * @param proxyHost Hostname of the proxy
     * @param proxyPort Port of the proxy.
     * @param proxyUser Optional username for authentication against the proxy.
     * @param proxyPass Optional pass for authentication against the proxy.
     */
    public HttpConnectProxy(@Nonnull String proxyScheme, boolean proxyValidate, @Nonnull String proxyHost,
	    @Nonnegative int proxyPort, @Nullable String proxyUser, @Nullable String proxyPass) {
	super(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
	this.proxyScheme = proxyScheme;
	this.proxyValidate = proxyValidate;
	this.proxyHost = proxyHost;
	this.proxyPort = proxyPort;
	this.proxyUser = proxyUser;
	this.proxyPass = proxyPass;
    }

    /**
     * Gets a freshly allocated socket representing a tunnel to the given host through the configured proxy.
     *
     * @param host Hostname of the target. IP addresses are allowed.
     * @param port Port number of the target.
     * @return Connected socket to the target.
     * @throws IOException Thrown in case the proxy or the target are not functioning correctly.
     */
    @Nonnull
    public Socket getSocket(@Nonnull String host, @Nonnegative int port) throws IOException {
	Socket sock = connectSocket();
	String requestStr = makeRequestStr(host, port);
	
	// deliver request
	sock.getOutputStream().write(requestStr.getBytes());

	// get HTTP response and validate it
	InputStream in = sock.getInputStream();
	String proxyResponse = getResponse(in);
	validateResponse(proxyResponse);

	// validation passed, slurp in the rest of the data and return
	if (in.available() > 0) {
	    in.skip(in.available());
	}
	return sock;
    }

    private Socket connectSocket() throws IOException {
	// Socket object connecting to proxy
	Socket sock = new Socket();
	SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
	sock.setKeepAlive(true);
	 // this is pretty much, but not a problem, as this only shifts the responsibility to the server
	sock.setSoTimeout(5 * 60 * 1000);
	sock.connect(addr, 60 * 1000);

	// evaluate scheme
	if ("https".equals(proxyScheme)) {
	    ClientCertDefaultTlsClient tlsClient = new ClientCertDefaultTlsClient(proxyHost);
	    TlsNoAuthentication tlsAuth = new TlsNoAuthentication();
	    tlsAuth.setHostname(proxyHost);
	    if (proxyValidate) {
		try {
		    tlsAuth.setCertificateVerifier(new JavaSecVerifier());
		} catch (GeneralSecurityException ex) {
		    throw new IOException("Failed to load certificate verifier.", ex);
		}
	    }
	    tlsClient.setAuthentication(tlsAuth);
	    TlsClientProtocol proto = new TlsClientProtocol(sock.getInputStream(), sock.getOutputStream());
	    proto.connect(tlsClient);
	    // wrap socket
	    Socket tlsSock = new SocketWrapper(sock, proto.getInputStream(), proto.getOutputStream());
	    return tlsSock;
	} else {
	    return sock;
	}
    }

    private String makeRequestStr(String host, int port) {
	// HTTP CONNECT protocol RFC 2616
	StringBuilder requestStr = new StringBuilder(1024);
	requestStr.append("CONNECT ").append(host).append(":").append(port). append(" HTTP/1.0\r\n");
	// Add Proxy Authorization if proxyUser and proxyPass is set
	if (proxyUser != null && proxyPass != null) {
	    String proxyUserPass = String.format("%s:%s", proxyUser, proxyPass);
	    proxyUserPass = Base64.toBase64String(proxyUserPass.getBytes());
	    requestStr.append("Proxy-Authorization: Basic ").append(proxyUserPass).append("\r\n");
	}
	// finalize request
	requestStr.append("\r\n");

	return requestStr.toString();
    }

    private String getResponse(InputStream in) throws IOException {
	byte[] tmpBuffer = new byte[512];
	int len = in.read(tmpBuffer, 0, tmpBuffer.length);
	if (len == 0) {
	    throw new SocketException("Invalid response from proxy.");
	}

	String proxyResponse = new String(tmpBuffer, 0, len, "UTF-8");
	return proxyResponse;
    }

    private void validateResponse(String response) throws IOException {
	// desctruct response status line
	// upgrade to HTTP/1.1 is ok, as nobody evaluates that stuff further
	// the last . catches everything including newlines, which is important because the match fails then
	Pattern p = Pattern.compile("HTTP/1\\.(1|0) (\\d{3}) (.*)\\r\\n(?s).*");
	Matcher m = p.matcher(response);
	if (m.matches()) {
	    String code = m.group(2);
	    String desc = m.group(3);
	    int codeNum = Integer.parseInt(code);

	    // expecting 200 in order to continue
	    if (codeNum != 200) {
		throw new HttpConnectProxyException("Failed to create proxy socket.", codeNum, desc);
	    }
	} else {
	    throw new HttpConnectProxyException("Invalid HTTP response from proxy.", 500, "Response malformed.");
	}
	// if we passed the exceptions the response is fine
    }

}
