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
package org.openecard.crypto.tls.proxy

import org.openecard.bouncycastle.tls.TlsClientProtocol
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto
import org.openecard.bouncycastle.util.encoders.Base64
import org.openecard.crypto.common.ReusableSecureRandom
import org.openecard.crypto.tls.ClientCertDefaultTlsClient
import org.openecard.crypto.tls.SocketWrapper
import org.openecard.crypto.tls.auth.DynamicAuthentication
import org.openecard.crypto.tls.verify.CertificateVerifierBuilder
import org.openecard.crypto.tls.verify.HostnameVerifier
import org.openecard.crypto.tls.verify.JavaSecVerifier
import org.openecard.crypto.tls.verify.KeyLengthVerifier
import java.io.IOException
import java.io.InputStream
import java.net.*
import java.util.regex.Pattern


/**
 * HTTP proxy supporting only CONNECT tunnel.
 * This class is initialised with the proxy parameters and can then establish a tunnel with the getSocket method.
 * The authentication parameters are optional. Scheme must be one of http and https.
 *
 * @author Tobias Wich
 */
class HttpConnectProxy
/**
 * Create a HTTP proxy instance configured with the given values.
 * This method does not perform any reachability checks, it only saves the values for later use.
 *
 * @param proxyScheme HTTP or HTTPS
 * @param proxyValidate Flag indicating whether to perform a certificate validation of the proxy server connection.
 * @param proxyHost Hostname of the proxy
 * @param proxyPort Port of the proxy.
 * @param proxyUser Optional username for authentication against the proxy.
 * @param proxyPass Optional pass for authentication against the proxy.
 */(
    private val proxyScheme: String,
    private val proxyValidate: Boolean,
    private val proxyHost: String,
    private val proxyPort: Int,
    private val proxyUser: String?,
    private val proxyPass: String?,
) : Proxy(
    Type.HTTP, InetSocketAddress(
        proxyHost,
        proxyPort
    )
) {
    /**
     * Gets a freshly allocated socket representing a tunnel to the given host through the configured proxy.
     *
     * @param host Hostname of the target. IP addresses are allowed.
     * @param port Port number of the target.
     * @return Connected socket to the target.
     * @throws IOException Thrown in case the proxy or the target are not functioning correctly.
     */
    @Throws(IOException::class)
    fun getSocket(host: String, port: Int): Socket {
        val sock = connectSocket()
        val requestStr = makeRequestStr(host, port)

        // deliver request
        sock.getOutputStream().write(requestStr.toByteArray())

        // get HTTP response and validate it
        val `in` = sock.getInputStream()
        val proxyResponse = getResponse(`in`)
        validateResponse(proxyResponse)

        // validation passed, slurp in the rest of the data and return
        if (`in`.available() > 0) {
            `in`.skip(`in`.available().toLong())
        }
        return sock
    }

    @Throws(IOException::class)
    private fun connectSocket(): Socket {
        // Socket object connecting to proxy
        val sock = Socket()
        val addr: SocketAddress = InetSocketAddress(proxyHost, proxyPort)
        sock.setKeepAlive(true)
        // this is pretty much, but not a problem, as this only shifts the responsibility to the server
        sock.setSoTimeout(5 * 60 * 1000)
        sock.connect(addr, 60 * 1000)

        // evaluate scheme
        if ("HTTPS" == proxyScheme) {
            val crypto: TlsCrypto = BcTlsCrypto(ReusableSecureRandom.instance)
            val tlsClient = ClientCertDefaultTlsClient(crypto, proxyHost, true)
            val tlsAuth = DynamicAuthentication(proxyHost)
            if (proxyValidate) {
                val cv = CertificateVerifierBuilder()
                    .and(HostnameVerifier())
                    .and(KeyLengthVerifier())
                    .and(JavaSecVerifier())
                    .build()
                tlsAuth.setCertificateVerifier(cv)
            }
            tlsClient.setAuthentication(tlsAuth)
            val proto = TlsClientProtocol(sock.getInputStream(), sock.getOutputStream())
            proto.connect(tlsClient)
            // wrap socket
            val tlsSock: Socket = SocketWrapper(sock, proto.inputStream, proto.outputStream)
            return tlsSock
        } else {
            return sock
        }
    }

    private fun makeRequestStr(host: String, port: Int): String {
        // HTTP CONNECT protocol RFC 2616
        val requestStr = StringBuilder(1024)
        requestStr.append("CONNECT ").append(host).append(":").append(port).append(" HTTP/1.0\r\n")
        // Add Proxy Authorization if proxyUser and proxyPass is set
        if (proxyUser != null && !proxyUser.isEmpty() && proxyPass != null && !proxyPass.isEmpty()) {
            var proxyUserPass: String? = String.format("%s:%s", proxyUser, proxyPass)
            proxyUserPass = Base64.toBase64String(proxyUserPass!!.toByteArray())
            requestStr.append("Proxy-Authorization: Basic ").append(proxyUserPass).append("\r\n")
        }
        // finalize request
        requestStr.append("\r\n")

        return requestStr.toString()
    }

    @Throws(IOException::class)
    private fun getResponse(`in`: InputStream): String {
        val tmpBuffer = ByteArray(512)
        val len = `in`.read(tmpBuffer, 0, tmpBuffer.size)
        if (len == 0) {
            throw SocketException("Invalid response from proxy.")
        }

        val proxyResponse = String(tmpBuffer, 0, len, charset("UTF-8"))
        return proxyResponse
    }

    @Throws(IOException::class)
    private fun validateResponse(response: String) {
        // destruct response status line
        // upgrade to HTTP/1.1 is ok, as nobody evaluates that stuff further
        // the last . catches everything including newlines, which is important because the match fails then
        val p = Pattern.compile("HTTP/1\\.(1|0) (\\d{3}) (.*)\\r\\n(?s).*")
        val m = p.matcher(response)
        if (m.matches()) {
            val code = m.group(2)
            val desc = m.group(3)
            val codeNum = code.toInt()

            // expecting 200 in order to continue
            if (codeNum != 200) {
                throw HttpConnectProxyException("Failed to create proxy socket.", codeNum, desc)
            }
        } else {
            throw HttpConnectProxyException("Invalid HTTP response from proxy.", 500, "Response malformed.")
        }
        // if we passed the exceptions the response is fine
    }
}
