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
import org.openecard.bouncycastle.crypto.tls.PSKTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCipherFactory;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;

/**
 * Extension of {@link PSKTlsClient} to override getAuthentication-Method to
 * return something else than NULL
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PSKTlsClientImpl extends PSKTlsClient {

    /**
     * 
     * @param cipherFactory ChiperFactory to use with this client
     * @param pskIdentity Identity + PSK for PSK
     * @param fqdn Hostname as Fully Qualified Domain Name (FQDN)
     */
    public PSKTlsClientImpl(TlsCipherFactory cipherFactory, TlsPSKIdentity pskIdentity, String fqdn) {
        super(cipherFactory, pskIdentity, fqdn);
    }

    /**
     * 
     * @param identity Identity for PSK
     * @param psk PSK for PSK
     * @param fqdn Hostname as Fully Qualified Domain Name (FQDN)
     */
    public PSKTlsClientImpl(byte[] identity, byte[] psk, String fqdn) {
        super(new TlsPSKIdentityImpl(identity, psk), fqdn);
    }

    /**
     * 
     * @param pskIdentity Identity + PSK for PSK
     * @param fqdn Hostname as Fully Qualified Domain Name (FQDN)
     */
    public PSKTlsClientImpl(TlsPSKIdentity pskIdentity, String fqdn) {
        super(pskIdentity, fqdn);
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
        return new DefaultTlsAuthentication(null);
    }

    @Override
    public void notifySessionID(byte[] sessionID) {
	// ignore and thus don't do session resuming
    }

}
