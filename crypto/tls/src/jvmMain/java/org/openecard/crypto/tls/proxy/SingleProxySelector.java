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
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;


/**
 *
 * @author Tobias WIch
 */
public class SingleProxySelector extends ProxySelector {

    private final List<Proxy> proxy;

    public SingleProxySelector(@Nonnull Proxy proxy) {
	this.proxy = Arrays.asList(proxy);
    }

    @Override
    public List<Proxy> select(URI uri) {
	return proxy;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
	// override for yourself if you want
    }

}
