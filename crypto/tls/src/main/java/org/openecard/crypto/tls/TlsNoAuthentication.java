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

import java.io.IOException;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the TlsAuthentication interface for certificate verification.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TlsNoAuthentication implements TlsAuthentication {

    private static final Logger logger = LoggerFactory.getLogger(TlsNoAuthentication.class);

    private String hostname = null;
    private CertificateVerifier certVerifier = null;

    private Certificate lastCertChain;

    /**
     * Sets the host name for the certificate verification step.
     *
     * @see #notifyServerCertificate(org.openecard.bouncycastle.crypto.tls.Certificate)
     * @param hostname
     */
    public void setHostname(String hostname) {
	this.hostname = hostname;
    }
    /**
     * Sets the implementation for the certificate verification step.
     *
     * @see #notifyServerCertificate(org.openecard.bouncycastle.crypto.tls.Certificate)
     * @see CertificateVerifier
     * @param certVerifier
     */
    public void setCertificateVerifier(CertificateVerifier certVerifier) {
	this.certVerifier = certVerifier;
    }


    /**
     * Verify the server certificate of the TLS handshake.
     * In case no implementation is set (via {@link #setCertificateVerifier(CertificateVerifier)}), no action is
     * performed.<br/>
     * The actual implementation is responsible for the types of verification that are performed. Besides the usual
     * hostname and certificate chain verification, those types could also include CRL and OCSP checking.
     *
     * @see CertificateVerifier
     * @param crtfct Certificate chain of the server as transmitted in the TLS handshake.
     * @throws IOException when certificate verification failed.
     */
    @Override
    public void notifyServerCertificate(Certificate crtfct) throws IOException {
	// save server certificate
	this.lastCertChain = crtfct;
	// try to validate
	if (certVerifier != null) {
	    // perform validation depending on the available parameters
	    if (hostname != null) {
		certVerifier.isValid(crtfct, hostname);
	    } else {
		logger.warn("Hostname not available for certificate verification.");
		certVerifier.isValid(crtfct);
	    }
	} else {
	    // no verifier available
	    logger.warn("No certificate verifier available, skipping certificate verification.");
	}
    }

    /**
     * This function is not implemented and always returns an empty chain.
     * From RFC 4346 sec. 7.4.6:
     * <p>If no suitable certificate is available, the client SHOULD send a certificate message containing no
     * certificates.</p>
     */
    @Override
    public TlsCredentials getClientCredentials(CertificateRequest cr) {
	return new TlsCredentials() {
	    @Override
	    public Certificate getCertificate() {
		return Certificate.EMPTY_CHAIN;
	    }
	};
    }

    /**
     * Returns the certificate chain which is processed during the TLS authentication.
     *
     * @return The certificate chain of the last certificate validation or null if none is available.
     */
    @Nullable
    public Certificate getServerCertificate() {
	return lastCertChain;
    }

}
