/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

/**
 *
 * @author Tobias Wich
 */
class UpdatingProxySelector(private val supplier: SelectorSupplier) : ProxySelector() {
    private var lastUpdate: Long = System.currentTimeMillis()
    private var lastSelector: ProxySelector = loadSelector()

    @Synchronized
    private fun updateSelector() {
        val now = System.currentTimeMillis()
        val diff = now - lastUpdate
        if (lastUpdate == 0L || diff > UPDATE_DELTA) {
            lastUpdate = now

            var selector = supplier.find()
            if (selector == null) {
                selector = NoProxySelector()
            }
            this.lastSelector = loadSelector()
        }
    }

	private fun loadSelector(): ProxySelector {
		var selector = supplier.find()
		if (selector == null) {
			selector = NoProxySelector()
		}
		return selector
	}

    override fun select(uri: URI): List<Proxy> {
        updateSelector()
        return lastSelector.select(uri)
    }

    override fun connectFailed(uri: URI, sa: SocketAddress, ioe: IOException) {
        lastSelector.connectFailed(uri, sa, ioe)
    }
}

// 30s update interval
private const val UPDATE_DELTA = (30 * 1000).toLong()
