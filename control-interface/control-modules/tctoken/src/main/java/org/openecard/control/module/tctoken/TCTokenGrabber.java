/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import javax.annotation.Nonnull;
import org.openecard.apache.http.Header;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.StatusLine;
import org.openecard.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openecard.apache.http.protocol.BasicHttpContext;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.apache.http.protocol.HttpRequestExecutor;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;
import org.openecard.common.io.LimitedInputStream;
import org.openecard.common.io.ProxySettings;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.Pair;
import org.openecard.control.ControlException;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.StreamHttpClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a grabber to fetch TCTokens from an URL.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TCTokenGrabber {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenGrabber.class);

    /**
     * Opens a stream to the given URL.
     *
     * @param url URL pointing to the TCToken.
     * @return Resource as a stream and the certificate of the TCToken service.
     * @throws TCTokenException
     */
    public static Pair<InputStream, Certificate> getStream(URL url) throws TCTokenException, MalformedURLException,
	    KeyStoreException, IOException, GeneralSecurityException, HttpException, URISyntaxException {
	HttpEntity entity = null;
	Certificate cert = null;
	boolean finished = false;

	while (! finished) {
	    logger.info("Trying to load TCToken from: {}", url);

	    String protocol = url.getProtocol();
	    String hostname = url.getHost();
	    int port = url.getPort();
	    if (port == -1) {
		port = url.getDefaultPort();
	    }
	    String resource = url.getFile();

	    if (! "https".equals(protocol)) {
		// FIXME: refactor exception handling
		throw new ControlException("Specified URL is not a https-URL.");
	    }

	    TlsAuthenticationCertSave tlsAuth = new TlsAuthenticationCertSave();
	    tlsAuth.setHostname(hostname);
	    // FIXME: verify certificate chain as soon as a usable solution exists for the trust problem
	    // tlsAuth.setCertificateVerifier(new JavaSecVerifier());
	    ClientCertTlsClient tlsClient = new ClientCertDefaultTlsClient(hostname);
	    tlsClient.setAuthentication(tlsAuth);
	    TlsProtocolHandler h;

	    try {
		tlsClient.setClientVersion(ProtocolVersion.TLSv11);
		Socket socket = ProxySettings.getDefault().getSocket(hostname, port);
		h = new TlsProtocolHandler(socket.getInputStream(), socket.getOutputStream());
		h.connect(tlsClient);
		cert = tlsAuth.getCertificateChain();
	    } catch (IOException e) {
		logger.error("Connecting to the TCToken-URL with TLSv1.1 failed. Falling back to TLSv1.0.");
		tlsClient.setClientVersion(ProtocolVersion.TLSv10);
		Socket socket = ProxySettings.getDefault().getSocket(hostname, port);
		h = new TlsProtocolHandler(socket.getInputStream(), socket.getOutputStream());
		h.connect(tlsClient);
	    }

	    StreamHttpClientConnection conn = new StreamHttpClientConnection(h.getInputStream(), h.getOutputStream());

	    HttpContext ctx = new BasicHttpContext();
	    HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

	    BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("GET", resource);
	    req.setParams(conn.getParams());
	    HttpRequestHelper.setDefaultHeader(req, url);
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
			String uri = headers[0].getValue();
			url = new URL(uri);
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
	}

	LimitedInputStream is = new LimitedInputStream(entity.getContent());
	return new Pair<InputStream, Certificate>(is, cert);
    }

    /**
     * Fetch the data from the URL.
     *
     * @param url URL of the resource.
     * @return Resource fetched from the URL and the certificate of the TCToken service.
     * @throws TCTokenException Thrown in case there was a problem reading the data from the given location.
     */
    public static Pair<String, Certificate> getResource(@Nonnull URL url) throws IOException {
	LimitedInputStream is = null;

	try {
	    Pair<InputStream, Certificate> data = TCTokenGrabber.getStream(url);
	    is = new LimitedInputStream(data.p1);
	    String stringData = FileUtils.toString(is);
	    return new Pair<String, Certificate>(stringData, data.p2);
	} catch (IOException ex) {
	    throw ex;
	} catch (Exception e) {
	    throw new IOException(e.getMessage(), e);
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (Exception ignore) {
		}
	    }
	}
    }

}
