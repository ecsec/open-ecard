/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.transport.tls;

import java.io.IOException;
import org.openecard.bouncycastle.crypto.tls.DefaultTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;

/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DefaultTlsClientImpl extends DefaultTlsClient {

    private TlsAuthentication tlsAuthentication;

    /**
     * 
     * @param host Hostname as Fully Qualified Domain Name (FQDN)
     * @param tlsAuthentication Authentication to use for this client
     */
    public DefaultTlsClientImpl(String host, TlsAuthentication tlsAuthentication) {
	super(host);
	this.tlsAuthentication = tlsAuthentication;
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
	return tlsAuthentication;
    }

    @Override
    public void notifySessionID(byte[] sessionID) {
	// ignore and thus don't do session resuming
    }

}
