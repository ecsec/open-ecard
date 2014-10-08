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

import org.openecard.bouncycastle.asn1.x500.RDN;
import org.openecard.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Certificate verifier which only checks the hostname against the received certificate.
 *
 * @author Tobias Wich
 */
public class HostnameVerifier implements CertificateVerifier {

    @Override
    public void isValid(Certificate chain) throws CertificateVerificationException {
	// no hostname, no verification
    }

    @Override
    public void isValid(Certificate chain, String hostname) throws CertificateVerificationException {
	// check hostname
	if (hostname != null) {
	    org.openecard.bouncycastle.asn1.x509.Certificate cert = chain.getCertificateAt(0);
	    RDN[] cn = cert.getSubject().getRDNs(BCStrictStyle.CN);
	    if (cn.length != 1) {
		throw new CertificateVerificationException("Multiple CN entries in certificate's Subject.");
	    }
	    // extract hostname from certificate
	    // TODO: add safeguard code if cn doesn't contain a string
	    String hostNameReference = cn[0].getFirst().getValue().toString();
	    checkWildcardName(hostname, hostNameReference);
	}
    }


    private static void checkWildcardName(String givenHost, String wildcardHost) throws CertificateVerificationException {
	final String errorMsg = "Hostname in certificate differs from actually requested host.";
	String[] givenToken = givenHost.split("\\.");
	String[] wildToken = wildcardHost.split("\\.");
	// error if number of token is different
	if (givenToken.length != wildToken.length) {
	    throw new CertificateVerificationException(errorMsg);
	}
	// compare entries
	for (int i = 0; i < givenToken.length; i++) {
	    if (wildToken[i].equals("*")) {
		// skip wildcard part
		continue;
	    }
	    if (! givenToken[i].equals(wildToken[i])) {
		throw new CertificateVerificationException(errorMsg);
	    }
	}
    }

}
