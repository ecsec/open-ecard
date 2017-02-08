/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class SubjectWhitelistTest {

    @Test
    public void testCert() throws IOException, CertificateException {
	try (InputStream certIn = SubjectWhitelistTest.class.getResourceAsStream("/stub.cg.crt")) {
	    CertificateFactory fac = CertificateFactory.getInstance("X.509");
	    X509Certificate cert = (X509Certificate) fac.generateCertificate(certIn);
	    X500Principal principal = cert.getSubjectX500Principal();
	    Assert.assertTrue(AllowedSubjects.instance().isInSubjects(principal));
	}
    }

    @Test
    public void testApiEndpoints() throws CertificateException {
	String testCertStr = "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";
	String prodCertStr = "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";
	matchApiEndpoint(testCertStr);
	matchApiEndpoint(prodCertStr);
    }

    private void matchApiEndpoint(String certStr) throws CertificateException {
	certStr = String.format("%s\n%s\n%s\n", "-----BEGIN CERTIFICATE-----", certStr, "-----END CERTIFICATE-----");
	ByteArrayInputStream certStream = new ByteArrayInputStream(certStr.getBytes(StandardCharsets.UTF_8));
	CertificateFactory cf = CertificateFactory.getInstance("X.509");
	X509Certificate cert = (X509Certificate) cf.generateCertificate(certStream);
	X500Principal certSub = cert.getSubjectX500Principal();
	Assert.assertTrue(AllowedApiEndpoints.instance().isInSubjects(certSub));
    }

}
