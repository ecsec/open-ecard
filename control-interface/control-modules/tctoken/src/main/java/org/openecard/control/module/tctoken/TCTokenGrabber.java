/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.control.module.tctoken;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;
import org.openecard.common.io.LimitedInputStream;
import org.openecard.control.ControlException;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.TlsNoAuthentication2;
import org.openecard.crypto.tls.verify.JavaSecVerifier;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.StreamHttpClientConnection;


/**
 * Implements a grabber to fetch TCTokens from an URI.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class TCTokenGrabber {

    /**
     * Opens a stream to the given URI.
     *
     * @param uri URI
     * @return Resource as a stream
     * @throws TCTokenException
     */
    public InputStream getStream(String uri) throws TCTokenException, MalformedURLException, KeyStoreException,
	    IOException, GeneralSecurityException, HttpException {
	String targetUrl = uri;
	HttpEntity entity = null;
	boolean finished = false;

	do {
	    if (targetUrl == null || !targetUrl.startsWith("https")) {
		// FIXME: refactor exception handling
		throw new ControlException("Specified URL is not a https-URL.");
	    }

	    URL url = new URL(targetUrl);
	    String hostname = url.getHost();
	    int port = url.getPort();
	    if (port == -1) {
		port = url.getDefaultPort();
	    }
	    String resource = url.getFile();

	    // TODO: verify certificate chain provided by TlsNoAuthentication2
	    TlsNoAuthentication2 tlsAuth = new TlsNoAuthentication2();
	    tlsAuth.setHostname(hostname);
	    tlsAuth.setCertificateVerifier(new JavaSecVerifier());
	    ClientCertTlsClient tlsClient = new ClientCertDefaultTlsClient(hostname);
	    tlsClient.setAuthentication(tlsAuth);
	    tlsClient.setClientVersion(ProtocolVersion.TLSv11);

	    Socket socket = new Socket(hostname, port);
	    TlsProtocolHandler h = new TlsProtocolHandler(socket.getInputStream(), socket.getOutputStream());
	    h.connect(tlsClient);
	    StreamHttpClientConnection conn = new StreamHttpClientConnection(h.getInputStream(), h.getOutputStream());

	    HttpContext ctx = new BasicHttpContext();
	    HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

	    BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("GET", resource);
	    req.setParams(conn.getParams());
	    HttpRequestHelper.setDefaultHeader(req, hostname);
	    req.setHeader("Accept", "text/xml, */*;q=0.8");
	    req.setHeader("Accept-Charset", "utf-8, *;q=0.8");
	    HttpResponse response = httpexecutor.execute(req, conn, ctx);
	    StatusLine status = response.getStatusLine();
	    int statusCode = status.getStatusCode();

	    switch (statusCode) {
		case 301:
		// fall through
		case 302:
		// fall through
		case 303:
		// fall through
		case 307:
		    Header[] headers = response.getHeaders("Location");
		    if (headers.length > 0) {
			targetUrl = headers[0].getValue();
		    } else {
			// FIXME: refactor exception handling
			throw new TCTokenException("TCToken cannot be retrieved. Missing Location header in HTTP response.");
		    }
		    break;
		default:
		    conn.receiveResponseEntity(response);
		    entity = response.getEntity();
		    finished = true;
	    }

	} while (!finished);

	LimitedInputStream is = new LimitedInputStream(entity.getContent());
	return is;
    }

    /**
     * Fetch the data from the URI.
     *
     * @param uri URI
     * @return Resource fetched from the URI
     * @throws TCTokenException
     */
    public String getResource(String uri) throws TCTokenException {
	LimitedInputStream is = null;

	try {
	    is = new LimitedInputStream(getStream(uri));
	    StringBuilder sb = new StringBuilder(2048);
	    byte[] buf = new byte[1024];

	    while (true) {
		int num = is.read(buf);
		if (num == -1) {
		    break;
		}
		sb.append(new String(buf, 0, num));
	    }

	    return sb.toString();
	} catch (Exception e) {
	    throw new TCTokenException(e.getMessage(), e);
	} finally {
	    try {
		is.close();
	    } catch (Exception ignore) {
	    }
	}
    }

}
