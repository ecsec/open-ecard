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

package org.openecard.crypto.tls.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class UpdatingProxySelector extends ProxySelector {

    // 30s update interval
    private static final long UPDATE_DELTA = 30 * 1000;
    private final SelectorSupplier supplier;

    private long lastUpdate = 0;
    private ProxySelector lastSelector;

    public UpdatingProxySelector(SelectorSupplier supplier) {
	this.supplier = supplier;
	loadSelector();
    }

    private synchronized void loadSelector() {
	long now = System.currentTimeMillis();
	long diff = now - lastUpdate;
	if (diff > UPDATE_DELTA) {
	    lastUpdate = now;

	    ProxySelector selector = supplier.find();
	    if (selector == null) {
		selector = new NoProxySelector();
	    }
	    this.lastSelector = selector;
	}
    }

    @Override
    public List<Proxy> select(URI uri) {
	loadSelector();
	return lastSelector.select(uri);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
	lastSelector.connectFailed(uri, sa, ioe);
    }

}
