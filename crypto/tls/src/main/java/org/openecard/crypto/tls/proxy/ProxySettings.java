/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.crypto.tls.proxy;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.selector.fixed.FixedSocksSelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.OpenecardProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to set up sockets with a specified or the system proxy.
 * The default is to use the system wide proxy settings, but it can also be overloaded with specific settings.
 * In order to overload the proxy settings, set the following values in {@link OpenecardProperties}:
 * <ul>
 *   <li>proxy.host</li>
 *   <li>proxy.port</li>
 * </ul>
 *
 * @author Tobias Wich
 */
public class ProxySettings {

    private static final Logger LOG = LoggerFactory.getLogger(ProxySettings.class);

    private final ProxySelector selector;

    static {
	com.github.markusbernhardt.proxy.util.Logger.setBackend(new LogBackEnd() {
	    private final HashMap<Class<?>, Logger> loggerCache = new HashMap<>();

	    private synchronized Logger getLogger(Class<?> clazz) {
		Logger l = loggerCache.get(clazz);
		if (l == null) {
		    l = LoggerFactory.getLogger(clazz);
		    loggerCache.put(clazz, l);
		}
		return l;
	    }

	    @Override
	    public void log(Class<?> clazz, LogLevel logLevel, String msg, Object... params) {
		Logger l = getLogger(clazz);
		switch (logLevel) {
		    case TRACE:
			l.trace(msg, params);
			break;
		    case DEBUG:
			l.debug(msg, params);
			break;
		    case INFO:
			l.info(msg, params);
			break;
		    case WARNING:
			l.warn(msg, params);
			break;
		    case ERROR:
			l.error(msg, params);
			break;
		}
	    }

	    @Override
	    public boolean isLogginEnabled(LogLevel logLevel) {
		// can not determine without knowing the class
		return true;
	    }
	});

	load();
    }

    /**
     * Preload proxy settings according to the global options.
     * The load must be performed when the settings change while running.
     */
    public static synchronized void load() {
	// load system proxy selector
	ProxySelector selector = new NoProxySelector();

	// get config values
	String scheme = OpenecardProperties.getProperty("proxy.scheme");
	// the empty string is no defined value, thus it means scheme not defined
	scheme = scheme != null ? scheme.toUpperCase() : "";
	String validate = OpenecardProperties.getProperty("proxy.validate_tls");
	String host = OpenecardProperties.getProperty("proxy.host");
	String port = OpenecardProperties.getProperty("proxy.port");
	String user = OpenecardProperties.getProperty("proxy.user");
	String pass = OpenecardProperties.getProperty("proxy.pass");
	String excl = OpenecardProperties.getProperty("proxy.excludes");
	// remove block of basic auth for http proxies
	setSystemProperty("jdk.http.auth.tunneling.disabledSchemes", "");

	clearSystemProperties();

	switch (scheme) {
	    case "SOCKS":
		// try to load SOCKS proxy
		if (host != null && port != null) {
		    setSystemProperty("socksProxyHost", host);
		    setSystemProperty("socksProxyPort", port);
		    setSystemProperty("socksProxyVersion", "5");
		    setSystemProperty("java.net.socks.username", user);
		    setSystemProperty("java.net.socks.password", pass);

		    // search strategy fails for this case, see https://github.com/MarkusBernhardt/proxy-vole/issues/5
		    // furthermore there are issues with the protocol selection
//		    ProxySearch ps = new ProxySearch();
//		    ps.addStrategy(ProxySearch.Strategy.JAVA);
//		    selector = ps.getProxySelector();
		    ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		    ps.setFallbackSelector(new FixedSocksSelector(host, Integer.parseInt(port)));
		    selector = ps;

		    List<Pattern> exclusions = parseExclusionHosts(excl);
		    selector = new RegexProxySelector(selector, exclusions);
		}
		break;
	    case "HTTP":
	    case "HTTPS":
		// try to load HTTP proxy
		if (host != null && port != null) {
		    try {
			if ("HTTP".equals(scheme)) {
			    setSystemProperty("http.proxyHost", host);
			    setSystemProperty("http.proxyPort", port);
			    setSystemProperty("https.proxyHost", host);
			    setSystemProperty("https.proxyPort", port);

			    if (user != null && pass != null) {
				try {
				    Authenticator.setDefault(createAuthenticator(user, pass));
				} catch (SecurityException ignore) {
				    LOG.error("Failed to set new Proxy Authenticator.");
				}
			    }

			    ProxySearch ps = new ProxySearch();
			    ps.addStrategy(ProxySearch.Strategy.JAVA);
			    selector = ps.getProxySelector();
			} else {
			    // use our own HTTP CONNECT Proxy
			    // the default is always validate
			    boolean valid = true;
			    if (validate != null) {
				valid = Boolean.parseBoolean(validate);
			    }

			    // use our connect proxy with an empty parent selector
			    Proxy proxy = new HttpConnectProxy(scheme, valid, host, Integer.parseInt(port), user, pass);
			    selector = new SingleProxySelector(proxy);
			}

			List<Pattern> exclusions = parseExclusionHosts(excl);
			selector = new RegexProxySelector(selector, exclusions);
		    } catch (NumberFormatException ex) {
			LOG.error("Invalid port specified for HTTPS proxy, using system defaults.");
		    }
		}
		break;
	    case "NO PROXY":
		selector = new NoProxySelector();
		break;
	    default: // including "SYSTEM PROXY"
		if (! "SYSTEM PROXY".equals(scheme)) {
		    LOG.warn("Unsupported proxy scheme {} used.", scheme);
		}

		// get proxy for a common host and set system properties
		ProxySelector ps = ProxySearch.getDefaultProxySearch()
			.getProxySelector();
		List<Proxy> proxies = ps != null ? ps.select(URI.create("https://google.com/")) : Collections.EMPTY_LIST;
		setSocksProperties(proxies);
		setHttpProperties(proxies);

		selector = new UpdatingProxySelector(new SelectorSupplier() {
		    @Override
		    public ProxySelector find() {
			ProxySearch ps = ProxySearch.getDefaultProxySearch();
			ProxySelector selector = ps.getProxySelector();
			return selector;
		    }
		});
		break;
	}

	ProxySelector.setDefault(selector);
    }

    /**
     * Converts a list of host exclusion entries to regexes.
     * The list has the form {@code *.example.com;localhost:8080;}.
     *
     * @param excl Exclusion entries as defined above.
     * @return Possibly empty list of patterns matching the given host patterns in the exclusion list.
     */
    public static List<Pattern> parseExclusionHosts(@Nullable String excl) {
	List<String> exclStrs = tokenizeExclusionHosts(excl);
	ArrayList<Pattern> result = new ArrayList<>(exclStrs.size());
	for (String next : exclStrs) {
	    try {
		Pattern p = createPattern(next);
		result.add(p);
	    } catch (PatternSyntaxException ex) {
		LOG.error("Failed to parse proxy exclusion pattern '{}'.", next);
	    }
	}

	return result;
    }

    private static List<String> tokenizeExclusionHosts(@Nullable String excl) {
	if (excl == null) {
	    return Collections.emptyList();
	} else {
	    try (Scanner s = new Scanner(excl).useDelimiter(";")) {
		ArrayList<String> exclStrs = new ArrayList<>();

		// read all items
		while (s.hasNext()) {
		    String next = s.next().trim();
		    if (! next.isEmpty()) {
			exclStrs.add(next);
		    }
		}

		return exclStrs;
	    }
	}
    }

    private static void clearSystemProperties() {
	clearSystemProperty("http.proxyHost");
	clearSystemProperty("http.proxyPort");
	clearSystemProperty("https.proxyHost");
	clearSystemProperty("https.proxyPort");
	clearSystemProperty("http.nonProxyHosts");

	clearSystemProperty("ftp.proxyHost");
	clearSystemProperty("ftp.proxyPort");
	clearSystemProperty("ftp.nonProxyHosts");

	clearSystemProperty("socksProxyHost");
	clearSystemProperty("socksProxyPort");
	clearSystemProperty("socksProxyVersion");
	clearSystemProperty("java.net.socks.username");
	clearSystemProperty("java.net.socks.password");

	clearSystemProperty("java.net.useSystemProxies");

	try {
	    Authenticator.setDefault(null);
	} catch (SecurityException ignore) {
	}
    }

    private static void clearSystemProperty(@Nonnull String key) throws IllegalArgumentException {
	try {
	    System.clearProperty(key);
	} catch (SecurityException ex) {
	    LOG.warn("Failed to clear system property '{}'.", key);
	}
    }

    private static void setSystemProperty(@Nonnull String key, @Nullable String value) {
	if (value != null) {
	    try {
		System.setProperty(key, value);
	    } catch (SecurityException ex) {
		LOG.warn("Failed to set system property '{}'.", key);
	    }
	}
    }

    private static void setSocksProperties(List<Proxy> proxies) {
	InetSocketAddress addr = getProxyAddress(Proxy.Type.SOCKS, proxies);

	if (addr != null) {
	    LOG.debug("Setting proxy properties to SOCKS@{}", addr);
	    setSystemProperty("socksProxyHost", addr.getHostString());
	    if (addr.getPort() > 0) {
		setSystemProperty("socksProxyPort", Integer.toString(addr.getPort()));
	    }
	    setSystemProperty("socksProxyVersion", "5");
	}
    }

    private static void setHttpProperties(List<Proxy> proxies) {
	InetSocketAddress addr = getProxyAddress(Proxy.Type.SOCKS, proxies);

	if (addr != null) {
	    LOG.debug("Setting proxy properties to HTTP@{}", addr);
	    setSystemProperty("http.proxyHost", addr.getHostString());
	    if (addr.getPort() > 0) {
		setSystemProperty("http.proxyPort", Integer.toString(addr.getPort()));
	    }
	}
    }

    @Nullable
    private static InetSocketAddress getProxyAddress(Proxy.Type type, List<Proxy> proxies) {
	InetSocketAddress addr = null;
	for (Proxy next : proxies) {
	    if (next.type() == Proxy.Type.SOCKS) {
		SocketAddress sa = next.address();
		if (sa instanceof InetSocketAddress) {
		    return (InetSocketAddress) sa;
		}
	    }
	}
	return null;
    }

    private static Authenticator createAuthenticator(final @Nonnull String user, final @Nonnull String pass) {
	return new Authenticator() {
	    @Override
	    protected PasswordAuthentication getPasswordAuthentication() {
		if (getRequestorType() == RequestorType.PROXY) {
		    String prot = getRequestingProtocol().toLowerCase();
		    String host = System.getProperty(prot + ".proxyHost", "");
		    String port = System.getProperty(prot + ".proxyPort", "80");
//		    String user = System.getProperty(prot + ".proxyUser", "");
//		    String password = System.getProperty(prot + ".proxyPassword", "");

		    if (getRequestingHost().equalsIgnoreCase(host)) {
			if (Integer.parseInt(port) == getRequestingPort()) {
			    // Seems to be OK.
			    return new PasswordAuthentication(user, pass.toCharArray());
			}
		    }
		}
		return null;
	    }
	};
    }

    private static Pattern createPattern(String expr) throws PatternSyntaxException {
	String[] hostPort = expr.split(":");
	String host, port;
	if (hostPort.length == 1) {
	    host = hostPort[0];
	    port = "(:*)?";
	} else {
	    // other combinations ignored, this is the users fault
	    host = hostPort[0];
	    port = ":" + hostPort[1];
	}
	return Pattern.compile(replaceMetaChars("^" + host + port + "$"));
    }

    private static String replaceMetaChars(String expr) {
	String result = expr.replace(".", "\\.");
	result = result.replace("*", ".*?");
	return result;
    }

    /**
     * Create instance with default settings read from the system.
     *
     * @param selector Currently active proxy selector.
     */
    public ProxySettings(@Nonnull ProxySelector selector) {
	this.selector = selector;
    }


    /**
     * Gets default ProxySettings instance.
     * The configuration for the default instance is loaded from the config file.
     *
     * @see OpenecardProperties
     * @return Default ProxySettings instance.
     */
    public static ProxySettings getDefault() {
	return new ProxySettings(ProxySelector.getDefault());
    }


    /**
     * Gets proxy instance for the chosen proxy configuration.
     * This may either be a proxy specified when creating the instance, the proxy set via the
     * {@link OpenecardProperties}, or the proxy selected by Java's {@link ProxySelector}.<br>
     * In case the ProxySelector is used, the host and port are needed in order to select the correct proxy
     * (see {@link ProxySelector#select(java.net.URI)}).
     *
     * @param protocol Application protocol spoken over the connection. Possible values are {@code http}, {@code https},
     *   {@code ftp}, {@code gopher} and {@code socket}.
     * @param hostname Hostname for the proxy determination.
     * @param port Port for the proxy determination.
     * @return Proxy object according to the configuration of the ProxySettings instance.
     * @throws URISyntaxException If host and/or port are invalid.
     */
    private Proxy getProxy(String protocol, String hostname, int port) throws URISyntaxException {
	Proxy p = Proxy.NO_PROXY;
	URI uri = new URI(protocol + "://" + hostname + ":" + port);
	// ask Java for the proxy
	List<Proxy> proxies = selector.select(uri);

	// find the first which is not DIRECT
	for (Proxy next : proxies) {
	    if (next.type() != Proxy.Type.DIRECT) {
		p = proxies.get(0);
		break;
	    }
	}

	LOG.debug("Selecting proxy: {}", p);
	return p;
    }

    /**
     * Gets connected socket using the proxy configured in this ProxySettings instance.
     *
     * @param protocol Application protocol spoken over the connection. Possible values are {@code http}, {@code https},
     *   {@code ftp}, {@code gopher} and {@code socket}.
     * @param hostname Host to connect the socket to.
     * @param port Port to connect the socket to.
     * @return Connected socket
     * @throws IOException If socket could not be connected.
     * @throws URISyntaxException If proxy could not be determined for the host-port combination.
     */
    public Socket getSocket(String protocol, String hostname, int port) throws IOException, URISyntaxException {
	Proxy p = getProxy(protocol, hostname, port);
	Socket sock = new Socket(p);
	SocketAddress addr;
	if (p.type() == Proxy.Type.DIRECT) {
	    addr = new InetSocketAddress(hostname, port);
	} else {
	    addr = InetSocketAddress.createUnresolved(hostname, port);
	}
	sock.setKeepAlive(true);
	 // this is pretty much, but not a problem, as this only shifts the responsibility to the server
	sock.setSoTimeout(5 * 60 * 1000);
	sock.connect(addr, 60 * 1000);
	return sock;
    }

}
