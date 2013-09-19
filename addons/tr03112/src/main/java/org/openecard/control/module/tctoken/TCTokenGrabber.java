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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.openecard.bouncycastle.crypto.tls.TlsClientProtocol;
import org.openecard.common.io.LimitedInputStream;
import org.openecard.crypto.tls.proxy.ProxySettings;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.Pair;
import org.openecard.common.util.TR03112Utils;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.HttpUtils;
import org.openecard.transport.httpcore.InvalidResultStatus;
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
     * This implementation follows redirects and records where it has been.
     *
     * @param url URL pointing to the TCToken.
     * @return Resource as a stream and the server certificates plus chain received while retrieving the TC Token.
     * @throws ControlException
     */
    public static Pair<InputStream, List<Pair<URL, Certificate>>> getStream(URL url, CertificateVerifier v)
	    throws MalformedURLException, IOException, HttpException, URISyntaxException, ControlException {
	HttpEntity entity = null;
	int maxRedirects = 10;
	boolean finished = false;
	ArrayList<Pair<URL, Certificate>> serverCerts = new ArrayList<Pair<URL, Certificate>>();

	while (! finished) {
	    logger.info("Trying to load resource from: {}", url);

	    if (maxRedirects == 0) {
		throw new ControlException("Maximum number of redirects exceeded..");
	    }
	    maxRedirects--;

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

	    // open a TLS connection, retrieve the server certificate and save it
	    TlsClientProtocol h;
	    DynamicAuthentication tlsAuth = new DynamicAuthentication();
	    // FIXME: verify certificate chain as soon as a usable solution exists for the trust problem
	    // tlsAuth.setCertificateVerifier(new JavaSecVerifier());
	    ClientCertTlsClient tlsClient = new ClientCertDefaultTlsClient(hostname);
	    tlsClient.setAuthentication(tlsAuth);

	    // connect tls client
	    tlsClient.setClientVersion(ProtocolVersion.TLSv12);
	    Socket socket = ProxySettings.getDefault().getSocket(hostname, port);
	    h = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream());
	    h.connect(tlsClient);

	    serverCerts.add(new Pair<URL, Certificate>(url, tlsAuth.getServerCertificate()));
	    // check result
	    CertificateVerifier.VerifierResult verifyResult = v.verify(url, tlsAuth.getServerCertificate());
	    if (verifyResult == CertificateVerifier.VerifierResult.FINISH) {
		List<Pair<URL, Certificate>> pairs = Collections.unmodifiableList(serverCerts);
		return new Pair<InputStream, List<Pair<URL, Certificate>>>(null, pairs);
	    }

	    StreamHttpClientConnection conn = new StreamHttpClientConnection(h.getInputStream(), h.getOutputStream());

	    HttpContext ctx = new BasicHttpContext();
	    HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

	    BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("GET", resource);
	    req.setParams(conn.getParams());
	    HttpRequestHelper.setDefaultHeader(req, url);
	    req.setHeader("Accept", "text/xml, */*;q=0.8");
	    req.setHeader("Accept-Charset", "utf-8, *;q=0.8");
	    HttpUtils.dumpHttpRequest(logger, req);
	    HttpResponse response = httpexecutor.execute(req, conn, ctx);
	    StatusLine status = response.getStatusLine();
	    int statusCode = status.getStatusCode();
	    String reason = status.getReasonPhrase();
	    HttpUtils.dumpHttpResponse(logger, response, null);

	    if (TR03112Utils.isRedirectStatusCode(statusCode)) {
		Header[] headers = response.getHeaders("Location");
		if (headers.length > 0) {
		    String uri = headers[0].getValue();
		    url = new URL(uri);
		} else {
		    // FIXME: refactor exception handling
		    String msg = "Resource could not be retrieved. Missing Location header in HTTP response.";
		    throw new ControlException(msg);
		}
	    } else if (statusCode >= 400) {
		// according to the HTTP RFC, codes greater than 400 signal errors
		String msg = String.format("Received a result code %d '%s' from server.", statusCode, reason);
		throw new InvalidResultStatus(msg);
	    } else {
		conn.receiveResponseEntity(response);
		entity = response.getEntity();
		finished = true;
	    }
	}

	LimitedInputStream is = new LimitedInputStream(entity.getContent());
	return new Pair<InputStream, List<Pair<URL, Certificate>>>(is, Collections.unmodifiableList(serverCerts));
    }

    public static Pair<String, List<Pair<URL, Certificate>>> getResource(@Nonnull URL url) throws IOException {
	// use verifier which always returns
	return getResource(url, new CertificateVerifier() {
	    @Override
	    public CertificateVerifier.VerifierResult verify(URL url, Certificate cert) throws ControlException {
		return VerifierResult.CONTINE;
	    }
	});
    }

    /**
     * Fetch the data from the URL.
     *
     * @param url URL of the resource.
     * @return Resource fetched from the possibly redirected URL and the certificates plus chain of the endpoints.
     * @throws TCTokenException Thrown in case there was a problem reading the data from the given location.
     */
    public static Pair<String, List<Pair<URL, Certificate>>> getResource(@Nonnull URL url, CertificateVerifier v)
	    throws IOException {
	LimitedInputStream is = null;

	try {
	    Pair<InputStream, List<Pair<URL, Certificate>>> data = TCTokenGrabber.getStream(url, v);
	    is = new LimitedInputStream(data.p1);
	    String stringData = FileUtils.toString(is);
	    return new Pair<String, List<Pair<URL, Certificate>>>(stringData, data.p2);
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
