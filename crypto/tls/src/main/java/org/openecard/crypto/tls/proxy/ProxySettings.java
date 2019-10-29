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

package org.openecard.crypto.tls.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.annotation.Nonnull;
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
	if (! isAndroid() && !isIOS()) {
	    ProxySettingsLoader psl = new ProxySettingsLoader();
	    psl.load();
	}
    }

    /**
     * Preload proxy settings according to the global options.
     * The load must be performed when the settings change while running.
     */
    public static synchronized void load() {
	if (! isAndroid()) {
	    new ProxySettingsLoader().load();
	}
    }

    private static boolean isAndroid() {
	try {
	    Class.forName("android.app.Activity");
	    return true;
	} catch (ClassNotFoundException e) {
	    return false;
	}
    }

    private static boolean isIOS() {
	try {
	    Class.forName("org.robovm.apple.foundation.NSObject");
	    return true;
	} catch (ClassNotFoundException e) {
	    return false;
	}
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

	if (isAndroid() && p.type() == Proxy.Type.HTTP) {
	    LOG.debug("Replacing proxy implementation for Android system.");
	    SocketAddress sa = p.address();
	    if (sa instanceof InetSocketAddress) {
		InetSocketAddress isa = (InetSocketAddress) sa;
		String phost = isa.getHostString();
		int pport = isa.getPort();
		HttpConnectProxy hcp = new HttpConnectProxy("HTTP", false, phost, pport, null, null);
		p = hcp;
	    }
	}

	if (p instanceof HttpConnectProxy) {
	    LOG.debug("Using custom HttpConnectProxy to obtain socket.");
	    HttpConnectProxy hcp = (HttpConnectProxy) p;
	    return hcp.getSocket(hostname, port);
	} else {
	    LOG.debug("Using proxy ({}) to obtain socket.", p.type());
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

}
