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

package org.openecard.crypto.tls;

import org.openecard.crypto.tls.auth.DynamicAuthentication;
import java.io.IOException;
import org.openecard.bouncycastle.crypto.tls.CipherSuite;
import org.openecard.bouncycastle.crypto.tls.DefaultTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCipherFactory;
import org.openecard.crypto.tls.auth.ContextAware;


/**
 * Standard TLS client also implementing the ClientCertTlsClient interface. <br/>
 * If not modified, the TlsAuthentication instance returned by {@link #getAuthentication()} is of type
 * {@link DynamicAuthentication} without further modifications.
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
    public int[] getCipherSuites() {
	return new int[] {
	    // recommended ciphers from TR-02102-2 sec. 3.3.1
	    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_DHE_DSS_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
	    CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
	    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_DHE_DSS_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
	    CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,
	    // acceptable in case DHE is not available
	    CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_DH_DSS_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_DH_RSA_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_DH_DSS_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_DH_RSA_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,
	    CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,
	    CipherSuite.TLS_DH_DSS_WITH_AES_256_CBC_SHA256,
	    CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA256,
	    CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,
	    CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,
	    CipherSuite.TLS_DH_DSS_WITH_AES_128_CBC_SHA256,
	    CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA256,
	    // SHA1 is acceptable until 2015
	    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_DH_DSS_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA,
	    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_DH_DSS_WITH_AES_128_CBC_SHA,
	    CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA,
	};
    }

    @Override
    public synchronized TlsAuthentication getAuthentication() throws IOException {
	if (tlsAuth == null) {
	    DynamicAuthentication tlsAuthTmp = new DynamicAuthentication();
	    tlsAuthTmp.setHostname(fqdn);
	    tlsAuth = tlsAuthTmp;
	}
	if (tlsAuth instanceof ContextAware) {
	    ((ContextAware) tlsAuth).setContext(context);
	}
	return tlsAuth;
    }

    @Override
    public synchronized void setAuthentication(TlsAuthentication tlsAuth) {
	this.tlsAuth = tlsAuth;
    }

}
