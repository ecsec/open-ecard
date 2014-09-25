/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.apache.http.Header;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.StatusLine;
import org.openecard.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openecard.apache.http.protocol.BasicHttpContext;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.apache.http.protocol.HttpRequestExecutor;
import org.openecard.binding.tctoken.ex.InvalidAddressException;
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
public class ResourceContext {

    private static final Logger logger = LoggerFactory.getLogger(ResourceContext.class);

    private final ClientCertTlsClient tlsClient;
    private final TlsClientProtocol tlsClientProto;
    private final List<Pair<URL, Certificate>> certs;

    private InputStream stream;
    private String data;
    private URL finalResourceAddress;

    protected ResourceContext(@Nullable ClientCertTlsClient tlsClient, @Nullable TlsClientProtocol tlsClientProto,
	    @Nonnull List<Pair<URL, Certificate>> certs, URL url) {
	this.tlsClient = tlsClient;
	this.tlsClientProto = tlsClientProto;
	this.certs = certs;
	finalResourceAddress = url;
    }

    public ClientCertTlsClient getTlsClient() {
	return tlsClient;
    }

    public TlsClientProtocol getTlsClientProto() {
	return tlsClientProto;
    }

    private void setStream(InputStream stream) {
	this.stream = stream;
    }

    public InputStream getStream() {
	return stream;
    }

    public List<Pair<URL, Certificate>> getCerts() {
	return certs;
    }

    public URL getFinalResourceAddress() {
	return finalResourceAddress;
    }

    public synchronized String getData() throws IOException {
	// load data from stream first
	if (data == null) {
	    LimitedInputStream is = null;
	    try {
		is = new LimitedInputStream(stream);
		data = FileUtils.toString(is);
	    } catch (IOException ex) {
		throw ex;
	    } finally {
		if (is != null) {
		    try {
			is.close();
		    } catch (IOException ignore) {
		    }
		}
	    }
	}
	return data;
    }

    /**
     * Opens a stream to the given URL.
     * This function function uses {@link #getStream(java.net.URL, org.openecard.binding.tctoken.CertificateVerifier)}
     * to get the stream. A verifier which is always true is used in the invocation.
     *
     * @param url URL pointing to the TCToken.
     * @return Resource as a stream and the server certificates plus chain received while retrieving the TC Token.
     * @throws IOException Thrown in case something went wrong in the connection layer.
     * @throws ResourceException Thrown when an unexpected condition (not TR-03112 conforming) occured.
     * @throws ValidationError The validator could not validate at least one host.
     * @throws InvalidAddressException
     */
    public static ResourceContext getStream(URL url) throws IOException, ResourceException, ValidationError,
	    InvalidAddressException {
	// use verifier which always returns
	return getStream(url, new CertificateValidator() {
	    @Override
	    public CertificateValidator.VerifierResult validate(URL url, Certificate cert) throws ValidationError {
		return CertificateValidator.VerifierResult.CONTINE;
	    }
	});
    }

    /**
     * Opens a stream to the given URL.
     * This implementation follows redirects and records where it has been.
     *
     * @param url URL pointing to the TCToken.
     * @param v Certificate verifier instance.
     * @return Resource as a stream and the server certificates plus chain received while retrieving the TC Token.
     * @throws IOException Thrown in case something went wrong in the connection layer.
     * @throws ResourceException Thrown when an unexpected condition (not TR-03112 conforming) occured.
     * @throws ValidationError The validator could not validate at least one host.
     * @throws InvalidAddressException
     */
    public static ResourceContext getStream(URL url, CertificateValidator v) throws IOException, ResourceException,
	    ValidationError, InvalidAddressException {
	ArrayList<Pair<URL, Certificate>> serverCerts = new ArrayList<>();
	return getStreamInt(url, v, serverCerts, 10);
    }

    private static ResourceContext getStreamInt(URL url, CertificateValidator v, List<Pair<URL,
	    Certificate>> serverCerts, int maxRedirects) throws IOException, ResourceException, ValidationError,
	    InvalidAddressException {
	try {
	    logger.info("Trying to load resource from: {}", url);

	    if (maxRedirects == 0) {
		throw new ResourceException("Maximum number of redirects exceeded.");
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
		throw new InvalidAddressException("Specified URL is not a https-URL.");
	    }

	    // open a TLS connection, retrieve the server certificate and save it
	    TlsClientProtocol h;
	    DynamicAuthentication tlsAuth = new DynamicAuthentication(hostname);
	    // FIXME: validate certificate chain as soon as a usable solution exists for the trust problem
	    // tlsAuth.setCertificateVerifier(new JavaSecVerifier());
	    ClientCertTlsClient tlsClient = new ClientCertDefaultTlsClient(hostname, true);
	    tlsClient.setAuthentication(tlsAuth);

	    // connect tls client
	    tlsClient.setClientVersion(ProtocolVersion.TLSv12);
	    Socket socket = ProxySettings.getDefault().getSocket(hostname, port);
	    h = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream(), new SecureRandom());
	    h.connect(tlsClient);

	    serverCerts.add(new Pair<>(url, tlsAuth.getServerCertificate()));
	    // check result
	    CertificateValidator.VerifierResult verifyResult = v.validate(url, tlsAuth.getServerCertificate());
	    if (verifyResult == CertificateValidator.VerifierResult.FINISH) {
		List<Pair<URL, Certificate>> pairs = Collections.unmodifiableList(serverCerts);
		return new ResourceContext(tlsClient, h, pairs, url);
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

	    HttpEntity entity = null;
	    boolean finished = false;
	    if (TR03112Utils.isRedirectStatusCode(statusCode)) {
		Header[] headers = response.getHeaders("Location");
		if (headers.length > 0) {
		    String uri = headers[0].getValue();
		    url = new URL(uri);
		} else {
		    // FIXME: refactor exception handling
		    String msg = "Resource could not be retrieved. Missing Location header in HTTP response.";
		    throw new ResourceException(msg);
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

	    // follow next redirect or finish?
	    if (finished) {
		ResourceContext result = new ResourceContext(tlsClient, h, serverCerts, url);
		LimitedInputStream is = new LimitedInputStream(entity.getContent());
		result.setStream(is);
		return result;
	    } else {
		return getStreamInt(url, v, serverCerts, maxRedirects);
	    }
	} catch (URISyntaxException ex) {
	    throw new IOException("Failed to initialize proxy.", ex);
	} catch (HttpException ex) {
	    throw new IOException("Invalid HTTP message received.", ex);
	}
    }

}
