/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.crypto.common.keystore.KeyTools;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Verifier checking that the chain is linked via Issuer and Subject entries and that the signature is valid.
 *
 * @author Tobias Wich
 */
public class SignatureVerifier implements CertificateVerifier {

    @Override
    public void isValid(Certificate chain, String hostOrIP) throws CertificateVerificationException {
	CertPath path;
	try {
	    path = KeyTools.convertCertificates(chain);
	} catch (CertificateException | IOException ex) {
	    String msg = "Failed to convert certificates to JCA format.";
	    throw new CertificateVerificationException(msg);
	}

	try {
	    List<? extends java.security.cert.Certificate> certs = path.getCertificates();
	    // check all but the last certificate
	    for (int i = 0; i < certs.size() - 1; i++) {
		java.security.cert.Certificate child = certs.get(i);
		java.security.cert.Certificate parent = certs.get(i + 1);
		assert(child instanceof X509Certificate);
		assert(parent instanceof X509Certificate);
		X509Certificate childX509 = (X509Certificate) child;
		X509Certificate parentX509 = (X509Certificate) parent;
		// verify signature
		child.verify(parent.getPublicKey());
		// and match subject and issuer
		if (! childX509.getIssuerX500Principal().equals(parentX509.getSubjectX500Principal())) {
		    String msg = "The provided certificate chain is missing an issuer certificate.";
		    throw new CertificateVerificationException(msg);
		}
	    }
	} catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
	    String msg = "System is missing correct certificate validation support.";
	    throw new CertificateVerificationException(msg, ex);
	} catch (CertificateException | InvalidKeyException ex) {
	    String msg = "Failed to convert certificate or key.";
	    throw new CertificateVerificationException(msg, ex);
	} catch (SignatureException ex) {
	    String msg = "Failed to verify the signature.";
	    throw new CertificateVerificationException(msg, ex);
	}
    }

}
