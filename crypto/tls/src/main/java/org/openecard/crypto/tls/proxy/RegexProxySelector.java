/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;


/**
 * ProxySelector instance using a list of regexes as exclusion.
 * In case any of the regexes matches, no
 *
 * @author Tobias Wich
 */
public class RegexProxySelector extends ProxySelector {

    private final ProxySelector parent;
    private final List<Pattern> hosts;

    public RegexProxySelector(@Nonnull ProxySelector parent, @Nonnull List<Pattern> hosts) {
	this.parent = parent;
	this.hosts = hosts;
    }

    /**
     * Checks if the given URL must be excluded from being proxied.
     *
     * @param uri URI to check for exclusion. Only Hostname and port are used.
     * @return {@code true} if the URI is excluded, {@code false} otherwise.
     */
    public boolean isExclusion(URI uri) {
	String hostPort = uri.getHost() + ":" + uri.getPort();
	// check if any of the patterns matches, if so do not proxy
	for (Pattern next : hosts) {
	    Matcher m = next.matcher(hostPort);
	    if (m.matches()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public List<Proxy> select(URI uri) {
	if (isExclusion(uri)) {
	    return Collections.singletonList(Proxy.NO_PROXY);
	}
	// no match so far, ask parent selector
	return parent.select(uri);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
	parent.connectFailed(uri, sa, ioe);
    }

}
