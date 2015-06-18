/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
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

    private static final Logger logger = LoggerFactory.getLogger(ProxySettings.class);

    private static ProxySettings defaultInstance;
    private static Proxy systemProxy;

    private final Proxy proxy;
    private final RegexProxySelector selector;

    static {
	load();
    }
    /**
     * Preload proxy settings according to the global options.
     * The load must be performed when the settings change while running.
     */
    public static synchronized void load() {
	Proxy p = null;

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

	if ("SOCKS".equals(scheme)) {
	    // try to load SOCKS proxy
	    try {
		if (host != null && port != null) {
		    p = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, Integer.parseInt(port)));
		}
	    } catch (NumberFormatException ex) {
	    }
	} else if ("HTTP".equals(scheme) || "HTTPS".equals(scheme)) {
	    // try to load HTTP CONNECT proxy
	    try {
		// the default is always validate
		boolean valid = true;
		if (validate != null) {
		    valid = Boolean.parseBoolean(validate);
		}
		if (host != null && port != null) {
		    p = new HttpConnectProxy(scheme, valid, host, Integer.parseInt(port), user, pass);
		}
	    } catch (NumberFormatException ex) {
	    }
	} else if (! scheme.isEmpty()) {
	    logger.warn("Unsupported proxy scheme {} used.", scheme);
	}

	// convert exclusion to regexes
	List<Pattern> exclusions = parseExclusionHosts(excl);

	systemProxy = p;
	// instantiate default instance
	defaultInstance = new ProxySettings(exclusions);
    }

    /**
     * Converts a list of host exclusion entries to regexes.
     * The list has the form {@code *.example.com;localhost:8080;}.
     *
     * @param excl Exclusion entries as defined above.
     * @return Possibly empty list of patterns matching the given host patterns in the exclusion list.
     */
    public static List<Pattern> parseExclusionHosts(@Nullable String excl) {
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

		ArrayList<Pattern> result = new ArrayList<>(exclStrs.size());
		for (String next : exclStrs) {
		    try {
			Pattern p = createPattern(next);
			result.add(p);
		    } catch (PatternSyntaxException ex) {
			logger.error("Failed to parse proxy exclusion pattern '{}'.", next);
		    }
		}

		return result;
	    }
	}
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
     * @param exclusions List of exclusions for which no proxying will be performed.
     */
    public ProxySettings(@Nonnull List<Pattern> exclusions) {
	this(null, exclusions);
    }
    /**
     * Create instance for the given proxy configuration.
     *
     * @param proxy Proxy to use when creating sockets.
     * @param exclusions List of exclusions for which no proxying will be performed.
     */
    private ProxySettings(@Nullable Proxy proxy, @Nonnull List<Pattern> exclusions) {
	this.proxy = proxy;
	this.selector = new RegexProxySelector(ProxySelector.getDefault(), exclusions);
    }

    /**
     * Gets default ProxySettings instance.
     * The configuration for the default instance is loaded from the config file.
     *
     * @see OpenecardProperties
     * @return Default ProxySettings instance.
     */
    public static ProxySettings getDefault() {
	return defaultInstance;
    }


    /**
     * Gets proxy instance for the chosen proxy configuration.
     * This may either be a proxy specified when creating the instance, the proxy set via the
     * {@link OpenecardProperties}, or the proxy selected by Java's {@link ProxySelector}.<br>
     * In case the ProxySelector is used, the host and port are needed in order to select the correct proxy
     * (see {@link ProxySelector#select(java.net.URI)}).
     *
     * @param hostname Hostname for the proxy determination.
     * @param port Port for the proxy determination.
     * @return Proxy object according to the configuration of the ProxySettings instance.
     * @throws URISyntaxException If host and/or port are invalid.
     */
    private Proxy getProxy(String hostname, int port) throws URISyntaxException {
	Proxy p;
	URI uri = new URI("socket://" + hostname + ":" + port);
	if (proxy == null) {
	    // try to use the one from the system settings
	    if (systemProxy != null) {
		// we have a proxy defined in the config
		if (selector.isExclusion(uri)) {
		    p = Proxy.NO_PROXY;
		} else {
		    p = systemProxy;
		}
	    } else {
		// ask Java for the proxy
		p = selector.select(uri).get(0);
	    }
	} else {
	    // proxy overridden by this class instance
	    if (selector.isExclusion(uri)) {
		p = Proxy.NO_PROXY;
	    } else {
		p = proxy;
	    }
	}
	logger.debug("Selecting proxy: {}", p);

	return p;
    }

    /**
     * Gets connected socket using the proxy configured in this ProxySettings instance.
     *
     * @param hostname Host to connect the socket to.
     * @param port Port to connect the socket to.
     * @return Connected socket
     * @throws IOException If socket could not be connected.
     * @throws URISyntaxException If proxy could not be determined for the host-port combination.
     */
    public Socket getSocket(String hostname, int port) throws IOException, URISyntaxException {
	Proxy p = getProxy(hostname, port);
	Socket sock;
	// HTTP CONNECT proxy is not handled by the Socket class, so do it ourselves
	if (p instanceof HttpConnectProxy) {
	    HttpConnectProxy hp = (HttpConnectProxy) p;
	    sock = hp.getSocket(hostname, port);
	} else {
	    sock = new Socket(getProxy(hostname, port));
	    SocketAddress addr = new InetSocketAddress(hostname, port);
	    sock.setKeepAlive(true);
	     // this is pretty much, but not a problem, as this only shifts the responsibility to the server
	    sock.setSoTimeout(5 * 60 * 1000);
	    sock.connect(addr, 60 * 1000);
	}
	return sock;
    }

}
