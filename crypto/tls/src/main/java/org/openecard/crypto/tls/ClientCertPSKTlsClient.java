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
import java.util.Hashtable;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.CipherSuite;
import org.openecard.bouncycastle.crypto.tls.NamedCurve;
import org.openecard.bouncycastle.crypto.tls.PSKTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCipherFactory;
import org.openecard.bouncycastle.crypto.tls.TlsECCUtils;
import org.openecard.bouncycastle.crypto.tls.TlsExtensionsUtils;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;
import org.openecard.crypto.tls.auth.ContextAware;


/**
 * PSK TLS client also implementing the ClientCertTlsClient interface. <br/>
 * If not modified, the TlsAuthentication instance returned by {@link #getAuthentication()} is of type
 * {@link DynamicAuthentication} without further modifications.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ClientCertPSKTlsClient extends PSKTlsClient implements ClientCertTlsClient {

    private final String host;
    private TlsAuthentication tlsAuth;

    /**
     * Create a ClientCertPSKTlsClient for the given parameters.
     *
     * @param pskId PSK to use for this connection.
     * @param host Host or IP address. Value must not be null.
     * @param doSni Control whether the server should send the SNI Header in the Client Hello.
     */
    public ClientCertPSKTlsClient(@Nonnull TlsPSKIdentity pskId, @Nonnull String host, boolean doSni) {
	super(pskId, doSni ? host : null);
	this.host = host;
    }
    /**
     * Create a ClientCertPSKTlsClient for the given parameters.
     *
     * @param tcf Cipher factory to use in this client.
     * @param pskId PSK to use for this connection.
     * @param host Host or IP address. Value must not be null.
     * @param doSni Control whether the server should send the SNI Header in the Client Hello.
     */
    public ClientCertPSKTlsClient(@Nonnull TlsCipherFactory tcf, @Nonnull TlsPSKIdentity pskId, @Nonnull String host,
	    boolean doSni) {
	super(tcf, pskId, host);
	this.host = host;
    }


    @Override
    public int[] getCipherSuites() {
	return new int[] {
	    // recommended ciphers from TR-02102-2 sec. 3.3.1
	    CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384,
	    CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384,
	    CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256,
	    CipherSuite.TLS_RSA_PSK_WITH_AES_128_CBC_SHA256,
	    // must have according to TR-03124-1 sec. 4.4
	    CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA,
	};
    }

    @Override
    public synchronized TlsAuthentication getAuthentication() throws IOException {
	if (tlsAuth == null) {
	    tlsAuth = new DynamicAuthentication(host);
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

    @Override
    public Hashtable getClientExtensions() throws IOException {
	Hashtable clientExtensions = super.getClientExtensions();
	clientExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(clientExtensions);
	// code taken from AbstractTlsClient, if that should ever change modify it here too
	if (TlsECCUtils.containsECCCipherSuites(getCipherSuites())) {
            this.namedCurves = new int[] {
		// required parameters TR-03116-4 sec. 4.1.4
		NamedCurve.secp224r1, NamedCurve.secp256r1, NamedCurve.brainpoolP256r1,
		// other possible parameters TR-02102-2 sec. 3.6
		NamedCurve.secp384r1, NamedCurve.brainpoolP384r1, NamedCurve.brainpoolP512r1,
	    };

            TlsECCUtils.addSupportedEllipticCurvesExtension(clientExtensions, namedCurves);
	}

	return clientExtensions;
    }

}
