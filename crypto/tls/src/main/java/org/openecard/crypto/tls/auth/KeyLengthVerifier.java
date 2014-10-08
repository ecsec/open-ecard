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

package org.openecard.crypto.tls.auth;

import java.security.PublicKey;
import java.security.cert.CertificateParsingException;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.jce.provider.X509CertificateObject;
import org.openecard.crypto.common.keystore.KeyTools;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Verifier checking the key length against the requirements in BSI TR-03116-4.
 *
 * @author Tobias Wich
 */
public class KeyLengthVerifier implements CertificateVerifier {

    @Override
    public void isValid(Certificate chain) throws CertificateVerificationException {
	isValid(chain, null);
    }

    @Override
    public void isValid(Certificate chain, String hostname) throws CertificateVerificationException {
	// check each certificate
	for (org.openecard.bouncycastle.asn1.x509.Certificate certStruct : chain.getCertificateList()) {
	    certStruct.getSubjectPublicKeyInfo().getAlgorithm();
	    try {
		X509CertificateObject c = new X509CertificateObject(certStruct);

		// get public key and determine minimum size for the actual type
		PublicKey pk = c.getPublicKey();
		int reference;
		if (pk instanceof RSAPublicKey) {
		    reference = 2048;
		} else if (pk instanceof DSAPublicKey) {
		    reference = 2048;
		} else if (pk instanceof ECPublicKey) {
		    reference = 224;
		} else {
		    String msg = String.format("Unsupported key type (%s) used in certificate.", pk.getAlgorithm());
		    throw new CertificateVerificationException(msg);
		}

		assertKeyLength(reference, KeyTools.getKeySize(pk));
	    } catch (CertificateParsingException ex) {
		throw new CertificateVerificationException("Failed to convert public key to JCA type.");
	    }
	}
    }

    private void assertKeyLength(int reference, int numbits) throws CertificateVerificationException {
	if (numbits < reference) {
	    String msg = "The key size in the certificate does not meet the requirements ";
	    msg += String.format("(%d < %d).", numbits, reference);
	    throw new CertificateVerificationException(msg);
	}
    }

}
