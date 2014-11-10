/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Certifier asserting that the same certificate is used between different TLS connections.
 *
 * @author Tobias Wich
 */
public class SameCertVerifier implements CertificateVerifier {

    private static final Logger logger = LoggerFactory.getLogger(SameCertVerifier.class);

    private Certificate firstCert;

    @Override
    public void isValid(Certificate serverCertificate, String hostOrIP) throws CertificateVerificationException {
	if (firstCert == null) {
	    firstCert = serverCertificate;
	} else {
	    // we have a saved certificate, try to validate it by compariison
	    if (serverCertificate == null) {
		String msg = "No server certificate transmitted. Test against first certificate is invalid.";
		logger.error(msg);
		throw new CertificateVerificationException(msg);
	    } else {
		// chains must be of equal length
		if (firstCert.getLength() != serverCertificate.getLength()) {
		    String msg = "Server certificate changed during transaction..";
		    logger.error(msg);
		    throw new CertificateVerificationException(msg);
		} else  {
		    // compare each certificate in the chain
		    for (int i = 0; i < firstCert.getLength(); i++) {
			byte[] first;
			byte[] second;
			try {
			    first = firstCert.getCertificateAt(i).getEncoded();
			    second = serverCertificate.getCertificateAt(i).getEncoded();
			} catch (IOException ex) {
			    String msg = "Failed to serialize certificate";
			    logger.error(msg);
			    throw new CertificateVerificationException(msg, ex);
			}
			if (! ByteUtils.compare(first, second)) {
			    String msg = "Certificates retransmitted by the server differ.";
			    logger.error(msg);
			    throw new CertificateVerificationException(msg);
			}
		    }
		}
	    }
	}
    }

}
