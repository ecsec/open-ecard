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

package org.openecard.client.crypto.tls;

import java.io.IOException;
import org.openecard.bouncycastle.crypto.tls.DefaultTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCipherFactory;


/**
 * Standard TLS client also implementing the ClientCertTlsClient interface. <br/>
 * If not modified, the TlsAuthentication instance returned by {@link #getAuthentication()} is of type
 * {@link TlsNoAuthentication} without further modifications.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ClientCertDefaultTlsClient extends DefaultTlsClient implements ClientCertTlsClient {

    private final String fqdn;
    private TlsAuthentication tlsAuth;

    /**
     * Create a ClientCertDefaultTlsClient for the given parameters.
     *
     * @param fqdn Fully qualified domain name of the server. This parameter is needed for SNI and for the creation
     * of the certificate verifier.
     */
    public ClientCertDefaultTlsClient(String fqdn) {
	super(fqdn);
	this.fqdn = fqdn;
    }
    /**
     * Create a ClientCertDefaultTlsClient for the given parameters.
     *
     * @param tcf Cipher factory to use in this client.
     * @param fqdn Fully qualified domain name of the server. This parameter is needed for SNI and for the creation
     * of the certificate verifier.
     */
    public ClientCertDefaultTlsClient(TlsCipherFactory tcf, String fqdn) {
	super(tcf, fqdn);
	this.fqdn = fqdn;
    }


    @Override
    public synchronized TlsAuthentication getAuthentication() throws IOException {
	if (tlsAuth == null) {
	    TlsNoAuthentication tlsAuthTmp = new TlsNoAuthentication();
	    tlsAuthTmp.setHostname(fqdn);
	    tlsAuth = tlsAuthTmp;
	}
	return tlsAuth;
    }

    @Override
    public synchronized void setAuthentication(TlsAuthentication tlsAuth) {
	this.tlsAuth = tlsAuth;
    }

}
