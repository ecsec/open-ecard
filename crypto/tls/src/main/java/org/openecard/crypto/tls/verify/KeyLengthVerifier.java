/****************************************************************************
 * Copyright (C) 2014-2017 ecsec GmbH.
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
import org.openecard.bouncycastle.asn1.x509.Certificate;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.bouncycastle.tls.crypto.TlsCertificate;
import org.openecard.crypto.common.keystore.KeyLengthException;
import org.openecard.crypto.common.keystore.KeyTools;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Verifier checking the key length against the requirements in BSI TR-03116-4.
 *
 * @author Tobias Wich
 */
public class KeyLengthVerifier implements CertificateVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(KeyLengthVerifier.class);

    @Override
    public void isValid(TlsServerCertificate chain, String hostname) throws CertificateVerificationException {
	try {
	    boolean firstCert = true;
	    // check each certificate

	    for (TlsCertificate next : chain.getCertificate().getCertificateList()) {
		Certificate x509 = Certificate.getInstance(next.getEncoded());
		boolean selfSigned = x509.getIssuer().equals(x509.getSubject());

		// skip key comparison step if this is a root certificate, but still check self signed server certs
		boolean isRootCert = selfSigned && ! firstCert;
		if (! isRootCert) {
		    // determine if key has the minimum size
		    KeyTools.assertKeyLength(x509);

		    firstCert = false;
		}
	    }
	} catch (IOException ex) {
	    String msg = "Failed to extract public key from certificate.";
	    throw new CertificateVerificationException(msg, ex);
	} catch (KeyLengthException ex) {
	    String msg = "The key in the certificate does not satisfy the length requirements.";
	    throw new CertificateVerificationException(msg, ex);
	}
    }

}
