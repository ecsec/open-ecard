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

package org.openecard.crypto.tls.verify;

import java.io.IOException;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.DefaultTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;
import org.openecard.crypto.tls.CertificateVerifier;
import org.openecard.crypto.tls.auth.CertificateVerifierBuilder;
import org.openecard.crypto.tls.auth.KeyLengthVerifier;


/**
 * Implementation of BouncyCastle's abstract DefaultTlsClient.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DefaultTlsClientImpl extends DefaultTlsClient {

    public DefaultTlsClientImpl(String hostName) {
	super(hostName);
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
	return new TlsAuthentication() {
	    @Override
	    public void notifyServerCertificate(Certificate crtfct) throws IOException {
		JavaSecVerifier v = null;
		try {
		    v = new JavaSecVerifier();
		} catch (Exception ex) {
		    throw new IOException(ex);
		}
		CertificateVerifier cv = new CertificateVerifierBuilder()
			.and(v)
			.and(new KeyLengthVerifier())
			.build();
		cv.isValid(crtfct, "www.google.com");
	    }
	    @Override
	    public TlsCredentials getClientCredentials(CertificateRequest cr) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	};
    }

}
