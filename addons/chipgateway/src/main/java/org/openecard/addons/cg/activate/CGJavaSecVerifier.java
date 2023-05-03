/****************************************************************************
 * Copyright (C) 2016-2017 ecsec GmbH.
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

package org.openecard.addons.cg.activate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.openecard.addons.cg.impl.AllowedApiEndpoints;
import org.openecard.addons.cg.impl.ChipGatewayProperties;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.verify.JavaSecVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class CGJavaSecVerifier extends JavaSecVerifier {

    public CGJavaSecVerifier() throws RuntimeException {
	super(ChipGatewayProperties.isRevocationCheck());
    }

    @Override
    protected Set<TrustAnchor> getTrustStore() {
	return new CGTrustStoreLoader().getTrustAnchors();
    }

    @Override
    public void isValid(TlsServerCertificate chain, String hostname) throws CertificateVerificationException {
	try {
	    CertPathBuilderResult certPathBuilderResult = validateCertificate(chain, hostname);

	    if (ChipGatewayProperties.isUseApiEndpointWhitelist()) {
		X509Certificate cert = (X509Certificate) certPathBuilderResult.getCertPath().getCertificates().get(0);
		X500Principal subj = cert.getSubjectX500Principal();
		if (! AllowedApiEndpoints.instance().isInSubjects(subj)) {
		    String msg = "The certificate used in the signature has an invalid subject: " + subj.getName();
		    throw new CertificateVerificationException(msg);
		}
	    }

	} catch (IOException ex) {
	    if (ex instanceof CertificateVerificationException) {
		throw (CertificateVerificationException) ex;
	    }
	    throw new CertificateVerificationException("Error converting certificate chain to java.security format.");
	}
    }

}
