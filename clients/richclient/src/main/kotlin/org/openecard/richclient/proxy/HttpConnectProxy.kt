/****************************************************************************
 * Copyright (C) 2013-2025 ecsec GmbH.
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

package org.openecard.richclient.proxy

import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import kotlin.io.encoding.Base64

/**
 * HTTP proxy supporting only CONNECT tunnel.
 * This class is initialised with the proxy parameters and can then establish a tunnel with the getSocket method.
 * The authentication parameters are optional. Scheme must be one of http and https.
 *
 * @param proxyScheme HTTP or HTTPS
 * @param proxyValidate Flag indicating whether to perform a certificate validation of the proxy server connection.
 * @param proxyHost Hostname of the proxy
 * @param proxyPort Port of the proxy.
 * @param proxyUser Optional username for authentication against the proxy.
 * @param proxyPass Optional pass for authentication against the proxy.
 *
 * @author Tobias Wich
 */
class HttpConnectProxy(
	private val proxyScheme: String,
	private val proxyValidate: Boolean,
	private val proxyHost: String,
	private val proxyPort: Int,
	private val proxyUser: String?,
	private val proxyPass: String?,
) : Proxy(
		Type.HTTP,
		InetSocketAddress(
			proxyHost,
			proxyPort,
		),
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
	fun getSocket(
		host: String,
		port: Int,
	): Socket {
		val sock = connectSocket()
		val requestStr = makeRequestStr(host, port)

		// deliver request
		sock.getOutputStream().write(requestStr.toByteArray())

		// get HTTP response and validate it
		val inStream = sock.getInputStream()
		val proxyResponse = getResponse(inStream)
		validateResponse(proxyResponse)

		// validation passed, slurp in the rest of the data and return
		if (inStream.available() > 0) {
			inStream.skip(inStream.available().toLong())
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
			val ctx = SSLContext.getInstance("TLS")
			val tlsSock = ctx.socketFactory.createSocket(sock, proxyHost, proxyPort, true)
			return tlsSock
		} else {
			return sock
		}
	}

	private fun makeRequestStr(
		host: String,
		port: Int,
	): String {
		// HTTP CONNECT protocol RFC 2616
		val requestStr = StringBuilder(1024)
		requestStr
			.append("CONNECT ")
			.append(host)
			.append(":")
			.append(port)
			.append(" HTTP/1.0\r\n")
		// Add Proxy Authorization if proxyUser and proxyPass is set
		if (proxyUser != null && !proxyUser.isEmpty() && proxyPass != null && !proxyPass.isEmpty()) {
			var proxyUserPass = String.format("%s:%s", proxyUser, proxyPass)
			proxyUserPass = Base64.encode(proxyUserPass.toByteArray())
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
		val p = Pattern.compile("HTTP/1\\.([01]) (\\d{3}) (.*)\\r\\n(?s).*")
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
