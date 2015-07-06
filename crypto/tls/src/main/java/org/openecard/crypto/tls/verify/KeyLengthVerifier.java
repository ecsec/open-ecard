/****************************************************************************
 * Copyright (C) 2014-2015 ecsec GmbH.
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
import org.openecard.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.openecard.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.openecard.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.openecard.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.openecard.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.openecard.bouncycastle.crypto.params.RSAKeyParameters;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.util.PublicKeyFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(KeyLengthVerifier.class);

    @Override
    public void isValid(Certificate chain, String hostname) throws CertificateVerificationException {
	try {
	    boolean firstCert = true;
	    // check each certificate
	    for (org.openecard.bouncycastle.asn1.x509.Certificate x509 : chain.getCertificateList()) {
		boolean selfSigned = x509.getIssuer().equals(x509.getSubject());

		// skip key comparison step if this is a root certificate, but still check self signed server certs
		boolean isRootCert = selfSigned && ! firstCert;
		if (! isRootCert) {
		    // get public key and determine minimum size for the actual type
		    SubjectPublicKeyInfo pkInfo = x509.getSubjectPublicKeyInfo();
		    AlgorithmIdentifier pkAlg = pkInfo.getAlgorithm();
		    AsymmetricKeyParameter key = PublicKeyFactory.createKey(pkInfo);
		    int reference;
		    if (key instanceof RSAKeyParameters) {
			reference = 2048;
		    } else if (key instanceof DSAPublicKeyParameters) {
			reference = 2048;
		    } else if (key instanceof ECPublicKeyParameters) {
			reference = 224;
		    } else {
			String alg = pkAlg.getAlgorithm().getId();
			String msg = String.format("Unsupported key type (%s) used in certificate.", alg);
			throw new CertificateVerificationException(msg);
		    }

		    assertKeyLength(reference, KeyTools.getKeySize(key));
		    firstCert = false;
		}
	    }
	} catch (IOException ex) {
	    String msg = "Failed to extract public key from certificate.";
	    throw new CertificateVerificationException(msg, ex);
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
