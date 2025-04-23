/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

import com.github.markusbernhardt.proxy.ProxySearch
import com.github.markusbernhardt.proxy.selector.fixed.FixedSocksSelector
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.OpenecardProperties
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI
import java.util.Locale
import java.util.Scanner
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class ProxySettingsLoader {
	/**
	 * Preload proxy settings according to the global options.
	 * The load must be performed when the settings change while running.
	 */
	fun load() {
		synchronized(ProxySettingsLoader::class.java) {
			// load system proxy selector
			var selector: ProxySelector = NoProxySelector()

			// get config values
			var scheme = OpenecardProperties.getProperty("proxy.scheme")
			// the empty string is no defined value, thus it means scheme not defined
			scheme = scheme?.uppercase(Locale.getDefault()) ?: ""
			val validate = OpenecardProperties.getProperty("proxy.validate_tls")
			val host = OpenecardProperties.getProperty("proxy.host")
			val port = OpenecardProperties.getProperty("proxy.port")
			val user = OpenecardProperties.getProperty("proxy.user")
			val pass = OpenecardProperties.getProperty("proxy.pass")
			val excl = OpenecardProperties.getProperty("proxy.excludes")
			// remove block of basic auth for http proxies
			setSystemProperty("jdk.http.auth.tunneling.disabledSchemes", "")

			clearSystemProperties()

			// convert port to an integer
			var portInt: Int? = null
			try {
				if (port != null) {
					portInt = port.toInt()
				}
			} catch (ex: NumberFormatException) {
				LOG.warn { "Failed to convert port string '$port' to a number. Using to proxy." }
			}

			when (scheme) {
				"SOCKS" -> // try to load SOCKS proxy
					if (host != null && portInt != null) {
						setSystemProperty("socksProxyHost", host)
						setSystemProperty("socksProxyPort", port)
						setSystemProperty("socksProxyVersion", "5")
						setSystemProperty("java.net.socks.username", user)
						setSystemProperty("java.net.socks.password", pass)

						// search strategy fails for this case, see https://github.com/MarkusBernhardt/proxy-vole/issues/5
						// furthermore there are issues with the protocol selection
						// 		    ProxySearch ps = new ProxySearch();
						// 		    ps.addStrategy(ProxySearch.Strategy.JAVA);
						// 		    selector = ps.getProxySelector();
						val ps = ProtocolDispatchSelector()
						ps.setFallbackSelector(FixedSocksSelector(host, portInt))
						selector = ps

						val exclusions = parseExclusionHosts(excl)
						selector = RegexProxySelector(selector, exclusions)
					}

				"HTTP", "HTTPS" -> // try to load HTTP proxy
					if (host != null && portInt != null) {
						if ("HTTP" == scheme) {
							setSystemProperty("http.proxyHost", host)
							setSystemProperty("http.proxyPort", port)
							setSystemProperty("https.proxyHost", host)
							setSystemProperty("https.proxyPort", port)

							if (user != null && pass != null) {
								try {
									Authenticator.setDefault(createAuthenticator(user, pass))
								} catch (ignore: SecurityException) {
									error { "Failed to set new Proxy Authenticator." }
								}
							}

							val ps = ProxySearch()
							ps.addStrategy(ProxySearch.Strategy.JAVA)
							selector = ps.getProxySelector()
						} else {
							// use our own HTTP CONNECT Proxy
							// the default is always validate
							var valid = true
							if (validate != null) {
								valid = validate.toBoolean()
							}

							// use our connect proxy with an empty parent selector
							val proxy: Proxy = HttpConnectProxy(scheme, valid, host, portInt, user, pass)
							selector = SingleProxySelector(proxy)
						}

						val exclusions = parseExclusionHosts(excl)
						selector = RegexProxySelector(selector, exclusions)
					}

				"NO PROXY" -> selector = NoProxySelector()
				else -> {
					if ("SYSTEM PROXY" != scheme) {
						LOG.warn { "Unsupported proxy scheme $scheme used." }
					}

					// get proxy for a common host and set system properties
					val ps = ProxySearch.getDefaultProxySearch().getProxySelector()
					val proxies: List<Proxy> =
						if (ps !=
							null
						) {
							ps.select(URI.create("https://google.com/"))
						} else {
							emptyList()
						}
					setSocksProperties(proxies)
					setHttpProperties(proxies)

					selector =
						UpdatingProxySelector(
							object : SelectorSupplier {
								override fun find(): ProxySelector? {
									val ps = ProxySearch.getDefaultProxySearch()
									val selector = ps.getProxySelector()
									return selector
								}
							},
						)
				}
			}
			ProxySelector.setDefault(selector)
		}
	}

	private fun clearSystemProperties() {
		clearSystemProperty("http.proxyHost")
		clearSystemProperty("http.proxyPort")
		clearSystemProperty("https.proxyHost")
		clearSystemProperty("https.proxyPort")
		clearSystemProperty("http.nonProxyHosts")

		clearSystemProperty("ftp.proxyHost")
		clearSystemProperty("ftp.proxyPort")
		clearSystemProperty("ftp.nonProxyHosts")

		clearSystemProperty("socksProxyHost")
		clearSystemProperty("socksProxyPort")
		clearSystemProperty("socksProxyVersion")
		clearSystemProperty("java.net.socks.username")
		clearSystemProperty("java.net.socks.password")

		clearSystemProperty("java.net.useSystemProxies")

		try {
			Authenticator.setDefault(null)
		} catch (ignore: SecurityException) {
		}
	}

	@Throws(IllegalArgumentException::class)
	private fun clearSystemProperty(key: String) {
		try {
			System.clearProperty(key)
		} catch (ex: SecurityException) {
			LOG.warn { "Failed to clear system property '$key'." }
		}
	}

	private fun setSystemProperty(
		key: String,
		value: String?,
	) {
		if (value != null) {
			try {
				System.setProperty(key, value)
			} catch (ex: SecurityException) {
				LOG.warn { "Failed to set system property '$key'." }
			}
		}
	}

	private fun setSocksProperties(proxies: List<Proxy>) {
		val addr = getProxyAddress(Proxy.Type.SOCKS, proxies)

		if (addr != null) {
			LOG.debug { "Setting proxy properties to SOCKS@$addr" }
			setSystemProperty("socksProxyHost", addr.hostString)
			if (addr.port > 0) {
				setSystemProperty("socksProxyPort", addr.port.toString())
			}
			setSystemProperty("socksProxyVersion", "5")
		}
	}

	private fun setHttpProperties(proxies: List<Proxy>) {
		val addr = getProxyAddress(Proxy.Type.SOCKS, proxies)

		if (addr != null) {
			LOG.debug { "Setting proxy properties to HTTP@$addr" }
			setSystemProperty("http.proxyHost", addr.hostString)
			if (addr.port > 0) {
				setSystemProperty("http.proxyPort", addr.port.toString())
			}
		}
	}

	private fun getProxyAddress(
		type: Proxy.Type,
		proxies: List<Proxy>,
	): InetSocketAddress? {
		val addr: InetSocketAddress? = null
		for (next in proxies) {
			if (next.type() == Proxy.Type.SOCKS) {
				val sa = next.address()
				if (sa is InetSocketAddress) {
					return sa
				}
			}
		}
		return null
	}

	private fun createAuthenticator(
		user: String,
		pass: String,
	): Authenticator {
		return object : Authenticator() {
			override fun getPasswordAuthentication(): PasswordAuthentication? {
				if (requestorType == RequestorType.PROXY) {
					val prot = requestingProtocol.lowercase(Locale.getDefault())
					val host = System.getProperty("$prot.proxyHost", "")
					val port = System.getProperty("$prot.proxyPort", "80")

					// 		    String user = System.getProperty(prot + ".proxyUser", "");
// 		    String password = System.getProperty(prot + ".proxyPassword", "");
					if (requestingHost.equals(host, ignoreCase = true)) {
						if (port.toInt() == requestingPort) {
							// Seems to be OK.
							return PasswordAuthentication(user, pass.toCharArray())
						}
					}
				}
				return null
			}
		}
	}

	companion object {
		init {
			com.github.markusbernhardt.proxy.util.Logger.setBackend(
				object :
					com.github.markusbernhardt.proxy.util.Logger.Slf4jLogBackEnd() {
					override fun log(
						clazz: Class<*>?,
						loglevel: com.github.markusbernhardt.proxy.util.Logger.LogLevel?,
						msg: String,
						vararg params: Any?,
					) {
						// rewrite {0}, {1}, ... textmarkers to use the SLF4J syntax without numbers
						var msg = msg
						msg = msg.replace("\\{[0-9]+\\}".toRegex(), "{}")
						super.log(clazz, loglevel, msg, *params)
					}
				},
			)
		}

		/**
		 * Converts a list of host exclusion entries to regexes.
		 * The list has the form `*.example.com;localhost:8080;`.
		 *
		 * @param excl Exclusion entries as defined above.
		 * @return Possibly empty list of patterns matching the given host patterns in the exclusion list.
		 */
		@JvmStatic
		fun parseExclusionHosts(excl: String?): List<Pattern> {
			val exclStrs = tokenizeExclusionHosts(excl)
			val result = mutableListOf<Pattern>()
			for (next in exclStrs) {
				try {
					val p: Pattern = createPattern(next)
					result.add(p)
				} catch (ex: PatternSyntaxException) {
					LOG.error { "Failed to parse proxy exclusion pattern '$next'." }
				}
			}

			return result
		}

		private fun tokenizeExclusionHosts(excl: String?): List<String> {
			if (excl == null) {
				return listOf()
			} else {
				Scanner(excl).useDelimiter(";").use { s ->
					val exclStrs = mutableListOf<String>()
					// read all items
					while (s.hasNext()) {
						val next = s.next().trim { it <= ' ' }
						if (!next.isEmpty()) {
							exclStrs.add(next)
						}
					}
					return exclStrs
				}
			}
		}

		@Throws(PatternSyntaxException::class)
		private fun createPattern(expr: String): Pattern {
			val hostPort: Array<String?> = expr.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			val host: String?
			val port: String?
			if (hostPort.size == 1) {
				host = hostPort[0]
				port = "(:*)?"
			} else {
				// other combinations ignored, this is the users fault
				host = hostPort[0]
				port = ":" + hostPort[1]
			}
			return Pattern.compile(replaceMetaChars("^$host$port$"))
		}

		private fun replaceMetaChars(expr: String): String {
			var result = expr.replace(".", "\\.")
			result = result.replace("*", ".*?")
			return result
		}
	}
}
