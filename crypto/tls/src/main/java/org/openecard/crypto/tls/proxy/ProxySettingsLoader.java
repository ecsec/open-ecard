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

package org.openecard.crypto.tls.proxy;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.selector.fixed.FixedSocksSelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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
 *
 * @author Tobias Wich
 */
public class ProxySettingsLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ProxySettingsLoader.class);

    static {
	com.github.markusbernhardt.proxy.util.Logger l = new com.github.markusbernhardt.proxy.util.Logger();
	com.github.markusbernhardt.proxy.util.Logger.setBackend(l.new Slf4jLogBackEnd() {
	    @Override
	    public void log(Class<?> clazz, com.github.markusbernhardt.proxy.util.Logger.LogLevel loglevel, String msg, Object... params) {
		// rewrite {0}, {1}, ... textmarkers to use the SLF4J syntax without numbers
		msg = msg.replaceAll("\\{[0-9]+\\}", "{}");
		super.log(clazz, loglevel, msg, params);
	    }
	});
    }

    /**
     * Preload proxy settings according to the global options.
     * The load must be performed when the settings change while running.
     */
    public void load() {
	synchronized (ProxySettingsLoader.class) {
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

	    // convert port to an integer
	    Integer portInt = null;
	    try {
		if (port != null) {
		    portInt = Integer.parseInt(port);
		}
	    } catch (NumberFormatException ex) {
		LOG.warn("Failed to convert port string '{}' to a number. Using to proxy.", port);
	    }

	    switch (scheme) {
		case "SOCKS":
		    // try to load SOCKS proxy
		    if (host != null && portInt != null) {
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
			ps.setFallbackSelector(new FixedSocksSelector(host, portInt));
			selector = ps;

			List<Pattern> exclusions = parseExclusionHosts(excl);
			selector = new RegexProxySelector(selector, exclusions);
		    }
		    break;
		case "HTTP":
		case "HTTPS":
		    // try to load HTTP proxy
		    if (host != null && portInt != null) {
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
			    Proxy proxy = new HttpConnectProxy(scheme, valid, host, portInt, user, pass);
			    selector = new SingleProxySelector(proxy);
			}

			List<Pattern> exclusions = parseExclusionHosts(excl);
			selector = new RegexProxySelector(selector, exclusions);
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

    private void clearSystemProperties() {
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

    private void clearSystemProperty(@Nonnull String key) throws IllegalArgumentException {
	try {
	    System.clearProperty(key);
	} catch (SecurityException ex) {
	    LOG.warn("Failed to clear system property '{}'.", key);
	}
    }

    private void setSystemProperty(@Nonnull String key, @Nullable String value) {
	if (value != null) {
	    try {
		System.setProperty(key, value);
	    } catch (SecurityException ex) {
		LOG.warn("Failed to set system property '{}'.", key);
	    }
	}
    }

    private void setSocksProperties(List<Proxy> proxies) {
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

    private void setHttpProperties(List<Proxy> proxies) {
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
    private InetSocketAddress getProxyAddress(Proxy.Type type, List<Proxy> proxies) {
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

    private Authenticator createAuthenticator(final @Nonnull String user, final @Nonnull String pass) {
	return new Authenticator() {
	    @Override
	    protected PasswordAuthentication getPasswordAuthentication() {
		if (getRequestorType() == Authenticator.RequestorType.PROXY) {
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

}
