/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.httpcore

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.http.HttpEntity
import org.apache.http.HttpException
import org.apache.http.HttpResponse
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.message.BasicHttpRequest
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestExecutor
import org.openecard.bouncycastle.tls.ProtocolVersion
import org.openecard.bouncycastle.tls.TlsClientProtocol
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto
import org.openecard.common.io.LimitedInputStream
import org.openecard.common.util.Pair
import org.openecard.common.util.TR03112Utils
import org.openecard.crypto.common.ReusableSecureRandom
import org.openecard.crypto.tls.ClientCertDefaultTlsClient
import org.openecard.crypto.tls.ClientCertTlsClient
import org.openecard.crypto.tls.auth.DynamicAuthentication
import org.openecard.crypto.tls.proxy.ProxySettings
import org.openecard.crypto.tls.verify.JavaSecVerifier
import org.openecard.httpcore.cookies.CookieException
import org.openecard.httpcore.cookies.CookieManager
import java.io.IOException
import java.net.Socket
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*

private val LOG = KotlinLogging.logger {  }

/**
 * Http client returning a ResourceContext after finding a valid endpoint.
 *
 * @author Tobias Wich
 */
open class ResourceContextLoader {
    protected open var cookieManager: CookieManager? = null
        get() {
            if (field == null) {
                field = CookieManager()
            }
            return field
        }

    /**
     * Opens a stream to the given URL.This function uses [.getStream] to get the stream.A verifier which is
     * always true is used in the invocation.
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
    @Throws(
        IOException::class,
        GeneralHttpResourceException::class,
        ValidationError::class,
        InsecureUrlException::class,
        InvalidRedirectChain::class,
        InvalidRedirectResponseSyntax::class,
        InvalidProxyException::class,
        RedirectionDepthException::class
    )
    fun getStream(url: URL): ResourceContext? {
        // use verifier which always returns
        return getStream(url, object : CertificateValidator {
            @Throws(ValidationError::class)
            override fun validate(url: URL, cert: TlsServerCertificate): CertificateValidator.VerifierResult {
                return CertificateValidator.VerifierResult.DONTCARE
            }
        })
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
    @Throws(
        IOException::class,
        GeneralHttpResourceException::class,
        ValidationError::class,
        InsecureUrlException::class,
        InvalidRedirectChain::class,
        InvalidRedirectResponseSyntax::class,
        InvalidProxyException::class,
        RedirectionDepthException::class
    )
    fun getStream(url: URL, v: CertificateValidator): ResourceContext? {
        val serverCerts = mutableListOf<Pair<URL, TlsServerCertificate>>()
        return getStreamInt(url, v, serverCerts, 10)
    }

    @Throws(
        IOException::class,
        GeneralHttpResourceException::class,
        ValidationError::class,
        InsecureUrlException::class,
        InvalidRedirectChain::class,
        InvalidProxyException::class,
        InvalidRedirectResponseSyntax::class,
        RedirectionDepthException::class
    )
    protected fun getStreamInt(
        url: URL,
        v: CertificateValidator,
        serverCerts: MutableList<Pair<URL, TlsServerCertificate>>,
        maxRedirects: Int
    ): ResourceContext? {
        var url = url
        var maxRedirects = maxRedirects
        try {
            val cManager = this.cookieManager
			LOG.info { "Trying to load resource from: $url" }

            if (maxRedirects == 0) {
                throw RedirectionDepthException("The maximum number of successive redirects has been reached.")
            }
            maxRedirects--

            val protocol = url.protocol
            val hostname = url.host
            var port = url.port
            if (port == -1) {
                port = url.defaultPort
            }
            var resource = url.file
            resource = if (resource.isEmpty()) "/" else resource

            if ("https" != protocol) {
                throw InsecureUrlException("Non HTTPS based protocol requested.")
            }

            // open a TLS connection, retrieve the server certificate and save it
            val h: TlsClientProtocol?
            val tlsAuth = DynamicAuthentication(hostname)
            // add PKIX validator if not doin nPA auth
            if (this.isPKIXVerify) {
                tlsAuth.addCertificateVerifier(JavaSecVerifier())
            }
            // FIXME: validate certificate chain as soon as a usable solution exists for the trust problem
            // tlsAuth.setCertificateVerifier(new JavaSecVerifier());
            val crypto: TlsCrypto = BcTlsCrypto(ReusableSecureRandom.instance)
            val tlsClient: ClientCertTlsClient = ClientCertDefaultTlsClient(crypto, hostname, true)
            tlsClient.setAuthentication(tlsAuth)

            // connect tls client
            tlsClient.clientVersion = ProtocolVersion.TLSv12
            val socket: Socket = ProxySettings.Companion.default.getSocket(protocol, hostname, port)
            h = TlsClientProtocol(socket.getInputStream(), socket.getOutputStream())
			LOG.debug { "Performing TLS handshake." }
            h.connect(tlsClient)
			LOG.debug { "TLS handshake performed." }

            serverCerts.add(Pair(url, tlsAuth.serverCertificate!!))
            // check result
            val verifyResult = v.validate(url, tlsAuth.serverCertificate!!)
            if (verifyResult == CertificateValidator.VerifierResult.FINISH) {
                return ResourceContext(tlsClient, h, serverCerts.toList())
            }

            val conn = StreamHttpClientConnection(h.inputStream, h.outputStream)

            val ctx: HttpContext = BasicHttpContext()
            val httpexecutor = HttpRequestExecutor()

            val req = BasicHttpEntityEnclosingRequest("GET", resource)
            HttpRequestHelper.setDefaultHeader(req, url)
            req.setHeader("Accept", this.acceptsHeader)
            req.setHeader("Accept-Charset", "utf-8, *;q=0.8")
            setCookieHeader(req, cManager, url)
            KHttpUtils.dumpHttpRequest(LOG, req)
			LOG.debug { "Sending HTTP request." }
            val response = httpexecutor.execute(req, conn, ctx)
            storeCookies(response, cManager, url)
			LOG.debug { "HTTP response received." }
            val status = response.statusLine
            val statusCode = status.statusCode
            val reason = status.
			reasonPhrase
            KHttpUtils.dumpHttpResponse(LOG, response)

            var entity: HttpEntity? = null
            var finished = false
            if (TR03112Utils.isRedirectStatusCode(statusCode)) {
                val headers = response.getHeaders("Location")
                if (headers.size > 0) {
                    val uri = headers[0]!!.value
                    url = URI(uri).toURL()
                } else {
                    // FIXME: refactor exception handling
                    throw InvalidRedirectResponseSyntax("Location header is missing in redirect response.")
                }
            } else if (statusCode >= 400) {
                // according to the HTTP RFC, codes greater than 400 signal errors
                val msg = String.format("Received a result code %d '%s' from server.", statusCode, reason)
				LOG.debug { msg }
                throw InvalidResultStatus(statusCode, reason, msg)
            } else {
                if (verifyResult == CertificateValidator.VerifierResult.CONTINUE) {
                    throw InvalidRedirectChain("Redirect URL is not a valid redirection target.")
                } else {
                    conn.receiveResponseEntity(response)
                    entity = response.entity
                    finished = true
                }
            }

            // follow next redirect or finish?
            if (finished) {
                checkNotNull(entity)
                val `is` = LimitedInputStream(entity.content)
                val result = ResourceContext(tlsClient, h, serverCerts, `is`)
                return result
            } else {
                h.close()
                return getStreamInt(url, v, serverCerts, maxRedirects)
            }
        } catch (ex: URISyntaxException) {
            throw InvalidProxyException("Proxy URL is invalid.", ex)
        } catch (ex: HttpException) {
            // don't translate this, it is handled in the ActivationAction
            throw GeneralHttpResourceException("Invalid HTTP message received.", ex)
        }
    }

    val acceptsHeader: String
        get() = "text/xml, */*;q=0.8"

    /**
     * Get the `Cookie` header for the given [URL] and from the given [CookieManager].
     *
     * @param req Request for which to set the cookie header.
     * @param manager [CookieManager] used to manage the cookies and getting the value of the `Cookie` header.
     * @param url [URL] for which the `Cookie` header shall be set.
     * @return The value of the `Cookie` header or `NULL` if there exist not cookies for the given [URL].
     */
    protected fun setCookieHeader(
        req: BasicHttpRequest,
        manager: CookieManager?,
        url: URL
    ): String? {
        var cookieHeader: String? = null
        try {
            if (manager != null) {
                cookieHeader = manager.getCookieHeaderValue(url.toString())
                if (cookieHeader != null && !cookieHeader.isEmpty()) {
                    req.setHeader("Cookie", cookieHeader)
                }
            }
        } catch (ex: CookieException) {
            // ignore because the input parameter is created from a valid URL.
        }

        return cookieHeader
    }

    /**
     * Stores the cookies contained in the given HttpResponse.
     * If there are no `Set-Cookie` headers in the request nothing will be stored.
     *
     * @param response Http Response containing possible `Set-Cookie` headers.
     * @param cManager [CookieManager] to use for managing the cookies.
     * @param url URL which was called.
     */
    protected fun storeCookies(response: HttpResponse, cManager: CookieManager?, url: URL) {
        val headers = response.allHeaders
        for (header in headers) {
            if (header.name.lowercase(Locale.getDefault()) == "set-cookie") {
                try {
					cManager?.addCookie(url.toString(), "set-cookie: " + header.value)
                } catch (ex: CookieException) {
                    var msg = "Received invalid cookie from: %s. The cookie is not stored."
                    msg = String.format(msg, url.toString())
					LOG.warn(ex) { msg }
                }
            }
        }
    }

    protected open val isPKIXVerify: Boolean
        get() = true

}
