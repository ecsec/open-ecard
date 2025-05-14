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
package org.openecard.crypto.tls.proxy

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.SysUtils
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.Socket
import java.net.SocketAddress
import java.net.URI
import java.net.URISyntaxException

private val LOG = KotlinLogging.logger { }

/**
 * Helper class to set up sockets with a specified or the system proxy.
 * The default is to use the system wide proxy settings, but it can also be overloaded with specific settings.
 * In order to overload the proxy settings, set the following values in [OpenecardProperties]:
 *
 *  * proxy.host
 *  * proxy.port
 *
 * @param selector Currently active proxy selector.
 *
 * @author Tobias Wich
 */
class ProxySettings(
	private val selector: ProxySelector,
) {
	/**
	 * Gets proxy instance for the chosen proxy configuration.
	 * This may either be a proxy specified when creating the instance, the proxy set via the
	 * [OpenecardProperties], or the proxy selected by Java's [ProxySelector].<br></br>
	 * In case the ProxySelector is used, the host and port are needed in order to select the correct proxy
	 * (see [ProxySelector.select]).
	 *
	 * @param protocol Application protocol spoken over the connection. Possible values are `http`, `https`,
	 * `ftp`, `gopher` and `socket`.
	 * @param hostname Hostname for the proxy determination.
	 * @param port Port for the proxy determination.
	 * @return Proxy object according to the configuration of the ProxySettings instance.
	 * @throws URISyntaxException If host and/or port are invalid.
	 */
	@Throws(URISyntaxException::class)
	private fun getProxy(
		protocol: String,
		hostname: String,
		port: Int,
	): Proxy {
		var p = Proxy.NO_PROXY
		val uri = URI("$protocol://$hostname:$port")
		// ask Java for the proxy
		val proxies = selector.select(uri)

		// find the first which is not DIRECT
		for (next in proxies) {
			if (next.type() != Proxy.Type.DIRECT) {
				p = proxies[0]
				break
			}
		}

		LOG.debug { "Selecting proxy: $p" }
		return p
	}

	/**
	 * Gets connected socket using the proxy configured in this ProxySettings instance.
	 *
	 * @param protocol Application protocol spoken over the connection. Possible values are `http`, `https`,
	 * `ftp`, `gopher` and `socket`.
	 * @param hostname Host to connect the socket to.
	 * @param port Port to connect the socket to.
	 * @return Connected socket
	 * @throws IOException If socket could not be connected.
	 * @throws URISyntaxException If proxy could not be determined for the host-port combination.
	 */
	@Throws(IOException::class, URISyntaxException::class)
	fun getSocket(
		protocol: String,
		hostname: String,
		port: Int,
	): Socket {
		var p = getProxy(protocol, hostname, port)

		if (SysUtils.isAndroid && p.type() == Proxy.Type.HTTP) {
			LOG.debug { "Replacing proxy implementation for Android system." }
			val sa = p.address()
			if (sa is InetSocketAddress) {
				val isa = sa
				val phost = isa.hostString
				val pport = isa.port
				val hcp = HttpConnectProxy("HTTP", false, phost, pport, null, null)
				p = hcp
			}
		}

		if (p is HttpConnectProxy) {
			LOG.debug { "Using custom HttpConnectProxy to obtain socket." }
			val hcp = p
			return hcp.getSocket(hostname, port)
		} else {
			LOG.debug { "Using proxy (${p.type()}) to obtain socket." }
			val sock = Socket(p)
			val addr: SocketAddress?
			if (p.type() == Proxy.Type.DIRECT) {
				addr = InetSocketAddress(hostname, port)
			} else {
				addr = InetSocketAddress.createUnresolved(hostname, port)
			}
			sock.setKeepAlive(true)
			// this is pretty much, but not a problem, as this only shifts the responsibility to the server
			sock.setSoTimeout(5 * 60 * 1000)
			sock.connect(addr, 60 * 1000)
			return sock
		}
	}

	companion object {
		init {
			if (!SysUtils.isAndroid && !SysUtils.isIOS) {
				val psl = ProxySettingsLoader()
				psl.load()
			}
		}

		/**
		 * Preload proxy settings according to the global options.
		 * The load must be performed when the settings change while running.
		 */
		@JvmStatic
		@Synchronized
		fun load() {
			if (!SysUtils.isAndroid) {
				ProxySettingsLoader().load()
			}
		}

		@JvmStatic
		val default: ProxySettings
			/**
			 * Gets default ProxySettings instance.
			 * The configuration for the default instance is loaded from the config file.
			 *
			 * @see OpenecardProperties
			 *
			 * @return Default ProxySettings instance.
			 */
			get() = ProxySettings(ProxySelector.getDefault())
	}
}
