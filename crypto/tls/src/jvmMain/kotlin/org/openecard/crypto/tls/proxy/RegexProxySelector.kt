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
package org.openecard.crypto.tls.proxy

import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.util.regex.Pattern

/**
 * ProxySelector instance using a list of regexes as exclusion.
 * In case any of the regexes matches, no
 *
 * @author Tobias Wich
 */
class RegexProxySelector(
    private val parent: ProxySelector,
    private val hosts: List<Pattern>
) : ProxySelector() {
    /**
     * Checks if the given URL must be excluded from being proxied.
     *
     * @param uri URI to check for exclusion. Only Hostname and port are used.
     * @return `true` if the URI is excluded, `false` otherwise.
     */
    fun isExclusion(uri: URI): Boolean {
        val hostPort = "${uri.host}:${uri.port}"
        // check if any of the patterns matches, if so do not proxy
        for (next in hosts) {
            val m = next.matcher(hostPort)
            if (m.matches()) {
                return true
            }
        }
        return false
    }

    override fun select(uri: URI): List<Proxy> {
        if (isExclusion(uri)) {
            return listOf(Proxy.NO_PROXY)
        }
        // no match so far, ask parent selector
        return parent.select(uri)
    }

    override fun connectFailed(uri: URI, sa: SocketAddress, ioe: IOException) {
        parent.connectFailed(uri, sa, ioe)
    }
}
