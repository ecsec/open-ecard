/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.binding.tctoken.ex.InvalidAddressException;
import org.openecard.bouncycastle.tls.ProtocolVersion;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.bouncycastle.tls.crypto.TlsCrypto;
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.openecard.common.DynamicContext;
import org.openecard.common.I18n;
import org.openecard.common.io.LimitedInputStream;
import org.openecard.crypto.tls.proxy.ProxySettings;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.Pair;
import org.openecard.common.util.Promise;
import org.openecard.common.util.TR03112Utils;
import org.openecard.crypto.common.ReusableSecureRandom;
import org.openecard.transport.httpcore.cookies.CookieException;
import org.openecard.transport.httpcore.cookies.CookieManager;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.crypto.tls.verify.JavaSecVerifier;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.HttpUtils;
import org.openecard.transport.httpcore.InvalidResultStatus;
import org.openecard.transport.httpcore.StreamHttpClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a grabber to fetch TCTokens from an URL.
 *
 * @author Moritz Horsch
 * @author Johannes Schmölz
 * @author Tobias Wich
 */
public class ResourceContext {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceContext.class);


    private static final I18n LANG = I18n.getTranslation("tr03112");

    private final ClientCertTlsClient tlsClient;
    private final TlsClientProtocol tlsClientProto;
    private final List<Pair<URL, TlsServerCertificate>> certs;

    private InputStream stream;
    private String data;

    protected ResourceContext(@Nullable ClientCertTlsClient tlsClient, @Nullable TlsClientProtocol tlsClientProto,
	    @Nonnull List<Pair<URL, TlsServerCertificate>> certs) {
	this.tlsClient = tlsClient;
	this.tlsClientProto = tlsClientProto;
	this.certs = certs;
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

    public void closeStream() {
	if (stream != null) {
	    try {
		stream.close();
	    } catch (IOException ex) {
		LOG.debug("Failed to close stream.", ex);
	    }
	}
	if (tlsClientProto != null) {
	    try {
		tlsClientProto.close();
	    } catch (IOException ex) {
		LOG.debug("Failed to close connection.", ex);
	    }
	}
    }

    public List<Pair<URL, TlsServerCertificate>> getCerts() {
	return certs;
    }

    public synchronized String getData() throws IOException {
	// load data from stream first
	if (data == null) {
	    try {
		data = FileUtils.toString(stream);
	    } catch (IOException ex) {
		throw ex;
	    } finally {
		if (stream != null) {
		    try {
			stream.close();
		    } catch (IOException ex) {
			LOG.debug("Failed to close stream.", ex);
		    }
		}
	    }
	}
	return data;
    }

    /**
     * Opens a stream to the given URL.
     * This function function uses {@link #getStream(URL, CertificateValidator)} to get the stream. A verifier which is
     * always true is used in the invocation.
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
	    public CertificateValidator.VerifierResult validate(URL url, TlsServerCertificate cert) throws ValidationError {
		return CertificateValidator.VerifierResult.DONTCARE;
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
	ArrayList<Pair<URL, TlsServerCertificate>> serverCerts = new ArrayList<>();
	return getStreamInt(url, v, serverCerts, 10);
    }

    private static ResourceContext getStreamInt(URL url, CertificateValidator v, List<Pair<URL,
	    TlsServerCertificate>> serverCerts, int maxRedirects) throws IOException, ResourceException, ValidationError,
	    InvalidAddressException {
	try {
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    CookieManager cManager = (CookieManager) dynCtx.get(TR03112Keys.COOKIE_MANAGER);
	    LOG.info("Trying to load resource from: {}", url);

	    if (maxRedirects == 0) {
		throw new ResourceException(MAX_REDIRECTS);
	    }
	    maxRedirects--;

	    String protocol = url.getProtocol();
	    String hostname = url.getHost();
	    int port = url.getPort();
	    if (port == -1) {
		port = url.getDefaultPort();
	    }
	    String resource = url.getFile();
	    resource = resource.isEmpty() ? "/" : resource;

	    if (! "https".equals(protocol)) {
		throw new InvalidAddressException(INVALID_ADDRESS);
	    }

	    // open a TLS connection, retrieve the server certificate and save it
	    TlsClientProtocol h;
	    DynamicAuthentication tlsAuth = new DynamicAuthentication(hostname);
	    // add PKIX validator if not doin nPA auth
	    if (isPKIXVerify()) {
		tlsAuth.addCertificateVerifier(new JavaSecVerifier());
	    }
	    // FIXME: validate certificate chain as soon as a usable solution exists for the trust problem
	    // tlsAuth.setCertificateVerifier(new JavaSecVerifier());
	    TlsCrypto crypto = new BcTlsCrypto(ReusableSecureRandom.getInstance());
	    ClientCertTlsClient tlsClient = new ClientCertDefaultTlsClient(crypto, hostname, true);
	    tlsClient.setAuthentication(tlsAuth);

	    // connect tls client
	    tlsClient.setClientVersion(ProtocolVersion.TLSv12);
	    Socket socket = ProxySettings.getDefault().getSocket(protocol, hostname, port);
	    h = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream());
	    LOG.debug("Performing TLS handshake.");
	    h.connect(tlsClient);
	    LOG.debug("TLS handshake performed.");

	    serverCerts.add(new Pair<>(url, tlsAuth.getServerCertificate()));
	    // check result
	    CertificateValidator.VerifierResult verifyResult = v.validate(url, tlsAuth.getServerCertificate());
	    if (verifyResult == CertificateValidator.VerifierResult.FINISH) {
		List<Pair<URL, TlsServerCertificate>> pairs = Collections.unmodifiableList(serverCerts);
		return new ResourceContext(tlsClient, h, pairs);
	    }

	    StreamHttpClientConnection conn = new StreamHttpClientConnection(h.getInputStream(), h.getOutputStream());

	    HttpContext ctx = new BasicHttpContext();
	    HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

	    BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("GET", resource);
	    HttpRequestHelper.setDefaultHeader(req, url);
	    req.setHeader("Accept", "text/xml, */*;q=0.8");
	    req.setHeader("Accept-Charset", "utf-8, *;q=0.8");
	    setCookieHeader(req, cManager, url);
	    HttpUtils.dumpHttpRequest(LOG, req);
	    LOG.debug("Sending HTTP request.");
	    HttpResponse response = httpexecutor.execute(req, conn, ctx);
	    storeCookies(response, cManager, url);
	    LOG.debug("HTTP response received.");
	    StatusLine status = response.getStatusLine();
	    int statusCode = status.getStatusCode();
	    String reason = status.getReasonPhrase();
	    HttpUtils.dumpHttpResponse(LOG, response, null);

	    HttpEntity entity = null;
	    boolean finished = false;
	    if (TR03112Utils.isRedirectStatusCode(statusCode)) {
		Header[] headers = response.getHeaders("Location");
		if (headers.length > 0) {
		    String uri = headers[0].getValue();
		    url = new URL(uri);
		} else {
		    // FIXME: refactor exception handling
		    throw new ResourceException(MISSING_LOCATION_HEADER);
		}
	    } else if (statusCode >= 400) {
		// according to the HTTP RFC, codes greater than 400 signal errors
		LOG.debug("Received a result code {} '{}' from server.", statusCode, reason);
		throw new InvalidResultStatus(LANG.translationForKey(INVALID_RESULT_STATUS, statusCode, reason));
	    } else {
		if (verifyResult == CertificateValidator.VerifierResult.CONTINUE) {
		    throw new InvalidAddressException(INVALID_REFRESH_ADDRESS_NOSOP);
		} else {
		    conn.receiveResponseEntity(response);
		    entity = response.getEntity();
		    finished = true;
		}
	    }

	    // follow next redirect or finish?
	    if (finished) {
		assert(entity != null);
		ResourceContext result = new ResourceContext(tlsClient, h, serverCerts);
		LimitedInputStream is = new LimitedInputStream(entity.getContent());
		result.setStream(is);
		return result;
	    } else {
		h.close();
		return getStreamInt(url, v, serverCerts, maxRedirects);
	    }
	} catch (URISyntaxException ex) {
	    throw new IOException(LANG.translationForKey(FAILED_PROXY), ex);
	} catch (HttpException ex) {
	    // don't translate this, it is handled in the ActivationAction
	    throw new IOException("Invalid HTTP message received.", ex);
	}
    }

    /**
     * Get the {@code Cookie} header for the given {@link URL} and from the given {@link CookieManager}.
     *
     * @param manager {@link CookieManager} used to manage the cookies and getting the value of the {@code Cookie} header.
     * @param url {@link URL} for which the {@code Cookie} header shall be set.
     * @return The value of the {@code Cookie} header or {@code NULL} if there exist not cookies for the given {@link URL}.
     */
    @Nullable
    private static String setCookieHeader(@Nonnull BasicHttpRequest req, CookieManager manager, @Nonnull URL url) {
	String cookieHeader = null;
	try {
	    if (manager != null) {
		cookieHeader = manager.getCookieHeaderValue(url.toString());
		if (cookieHeader != null && !cookieHeader.isEmpty()) {
		    req.setHeader("Cookie", cookieHeader);
		}
	    }
	} catch (CookieException ex) {
	    // ignore because the input parameter is created from a valid URL.
	}

	return cookieHeader;
    }

    /**
     * Stores the cookies contained in the given HttpResponse.
     * If there are no {@code Set-Cookie} headers in the request nothing will be stored.
     *
     * @param response Http Response containing possible {@code Set-Cookie} headers.
     * @param cManager {@link CookieManager} to use for managing the cookies.
     * @param url URL which was called.
     */
    private static void storeCookies(@Nonnull HttpResponse response, CookieManager cManager, @Nonnull URL url) {
	Header[] headers = response.getAllHeaders();
	for (Header header : headers) {
	    if (header.getName().toLowerCase().equals("set-cookie")) {
		try {
		    if (cManager != null) {
			cManager.addCookie(url.toString(), "set-cookie: " + header.getValue());
		    }
		} catch(CookieException ex) {
		    String msg = "Received invalid cookie from: %s. The cookie is not stored.";
		    msg = String.format(msg, url.toString());
		    LOG.warn(msg, ex);
		}
	    }
	}
    }

    private static boolean isPKIXVerify() {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	Promise<Object> cardTypeP = dynCtx.getPromise(TR03112Keys.ACTIVATION_CARD_TYPE);
	Object cardType = cardTypeP.derefNonblocking();
	// verify when the value is not set or when no nPA is requested
	if (cardType != null && ! "http://bsi.bund.de/cif/npa.xml".equals(cardType)) {
	    return true;
	} else {
	    return false;
	}
    }

}
