/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.httpcore;

import java.io.IOException;
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
import org.openecard.bouncycastle.tls.ProtocolVersion;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.bouncycastle.tls.crypto.TlsCrypto;
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.openecard.common.io.LimitedInputStream;
import org.openecard.common.util.Pair;
import org.openecard.common.util.TR03112Utils;
import org.openecard.crypto.common.ReusableSecureRandom;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.crypto.tls.proxy.ProxySettings;
import org.openecard.crypto.tls.verify.JavaSecVerifier;
import org.openecard.httpcore.cookies.CookieException;
import org.openecard.httpcore.cookies.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Http client returning a ResourceContext after finding a valid endpoint.
 *
 * @author Tobias Wich
 */
public class ResourceContextLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceContextLoader.class);

    private CookieManager cookieManager;

    /**
     * Opens a stream to the given URL.This function function uses {@link #getStream(URL, CertificateValidator)} to get the stream.A verifier which is
 always true is used in the invocation.
     *
     * @param url URL pointing to the TCToken.
     * @return Resource as a stream and the server certificates plus chain received while retrieving the TC Token.
     * @throws IOException Thrown in case something went wrong in the connection layer.
     * @throws GeneralHttpResourceException Thrown when an unexpected condition (not TR-03112 conforming) occured.
     * @throws ValidationError The validator could not validate at least one host.
     * @throws InsecureUrlException
     * @throws InvalidRedirectChain
     * @throws InvalidRedirectResponseSyntax
     * @throws InvalidProxyException
     * @throws RedirectionDepthException
     */
    public ResourceContext getStream(URL url) throws IOException, GeneralHttpResourceException, ValidationError,
	    InsecureUrlException, InvalidRedirectChain, InvalidRedirectResponseSyntax, InvalidProxyException, RedirectionDepthException {
	// use verifier which always returns
	return getStream(url, new CertificateValidator() {
	    @Override
	    public CertificateValidator.VerifierResult validate(URL url, TlsServerCertificate cert) throws ValidationError {
		return CertificateValidator.VerifierResult.DONTCARE;
	    }
	});
    }

    /**
     * Opens a stream to the given URL.This implementation follows redirects and records where it has been.
     *
     * @param url URL pointing to the TCToken.
     * @param v Certificate verifier instance.
     * @return Resource as a stream and the server certificates plus chain received while retrieving the TC Token.
     * @throws IOException Thrown in case something went wrong in the connection layer.
     * @throws GeneralHttpResourceException Thrown when an unexpected condition (not TR-03112 conforming) occured.
     * @throws ValidationError The validator could not validate at least one host.
     * @throws InsecureUrlException
     * @throws InvalidRedirectChain
     * @throws InvalidRedirectResponseSyntax
     * @throws InvalidProxyException
     * @throws RedirectionDepthException
     */
    public ResourceContext getStream(URL url, CertificateValidator v) throws IOException, GeneralHttpResourceException,
	    ValidationError, InsecureUrlException, InvalidRedirectChain, InvalidRedirectResponseSyntax, InvalidProxyException, RedirectionDepthException {
	ArrayList<Pair<URL, TlsServerCertificate>> serverCerts = new ArrayList<>();
	return getStreamInt(url, v, serverCerts, 10);
    }

    protected ResourceContext getStreamInt(URL url, CertificateValidator v, List<Pair<URL,
	    TlsServerCertificate>> serverCerts, int maxRedirects) throws IOException, GeneralHttpResourceException, ValidationError,
	    InsecureUrlException, InvalidRedirectChain, InvalidProxyException, InvalidRedirectResponseSyntax, RedirectionDepthException {
	try {
	    CookieManager cManager = getCookieManager();
	    LOG.info("Trying to load resource from: {}", url);

	    if (maxRedirects == 0) {
		throw new RedirectionDepthException("The maximum number of successive redirects has been reached.");
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
		throw new InsecureUrlException("Non HTTPS based protocol requested.");
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
	    TlsCrypto crypto = new BcTlsCrypto(ReusableSecureRandom.instance);
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
	    req.setHeader("Accept", getAcceptsHeader());
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
	    HttpUtils.dumpHttpResponse(LOG, response);

	    HttpEntity entity = null;
	    boolean finished = false;
	    if (TR03112Utils.isRedirectStatusCode(statusCode)) {
		Header[] headers = response.getHeaders("Location");
		if (headers.length > 0) {
		    String uri = headers[0].getValue();
		    url = new URL(uri);
		} else {
		    // FIXME: refactor exception handling
		    throw new InvalidRedirectResponseSyntax("Location header is missing in redirect response.");
		}
	    } else if (statusCode >= 400) {
		// according to the HTTP RFC, codes greater than 400 signal errors
		String msg = String.format("Received a result code %d '%s' from server.", statusCode, reason);
		LOG.debug(msg);
		throw new InvalidResultStatus(statusCode, reason, msg);
	    } else {
		if (verifyResult == CertificateValidator.VerifierResult.CONTINUE) {
		    throw new InvalidRedirectChain("Redirect URL is not a valid redirection target.");
		} else {
		    conn.receiveResponseEntity(response);
		    entity = response.getEntity();
		    finished = true;
		}
	    }

	    // follow next redirect or finish?
	    if (finished) {
		assert(entity != null);
		LimitedInputStream is = new LimitedInputStream(entity.getContent());
		ResourceContext result = new ResourceContext(tlsClient, h, serverCerts, is);
		return result;
	    } else {
		h.close();
		return getStreamInt(url, v, serverCerts, maxRedirects);
	    }
	} catch (URISyntaxException ex) {
	    throw new InvalidProxyException("Proxy URL is invalid.", ex);
	} catch (HttpException ex) {
	    // don't translate this, it is handled in the ActivationAction
	    throw new GeneralHttpResourceException("Invalid HTTP message received.", ex);
	}
    }

    public String getAcceptsHeader() {
	return "text/xml, */*;q=0.8";
    }

    /**
     * Get the {@code Cookie} header for the given {@link URL} and from the given {@link CookieManager}.
     *
     * @param req Request for which to set the cookie header.
     * @param manager {@link CookieManager} used to manage the cookies and getting the value of the {@code Cookie} header.
     * @param url {@link URL} for which the {@code Cookie} header shall be set.
     * @return The value of the {@code Cookie} header or {@code NULL} if there exist not cookies for the given {@link URL}.
     */
    @Nullable
    protected String setCookieHeader(@Nonnull BasicHttpRequest req, CookieManager manager, @Nonnull URL url) {
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
    protected void storeCookies(@Nonnull HttpResponse response, CookieManager cManager, @Nonnull URL url) {
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

    protected CookieManager getCookieManager() {
	if (cookieManager == null) {
	    cookieManager = new CookieManager();
	}
	return cookieManager;
    }

    protected boolean isPKIXVerify() {
	return true;
    }

}
