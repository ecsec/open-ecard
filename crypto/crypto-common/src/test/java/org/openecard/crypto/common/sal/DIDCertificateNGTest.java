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
package org.openecard.crypto.common.sal;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import org.openecard.common.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 *
 * @author Hans-Martin Haase
 */
public class DIDCertificateNGTest {

    private static final String CLIENT_CERT_SUBJECT = "T=Mag., SERIALNUMBER=308766205469, GIVENNAME=XXXMaria-Theresia "
	    + "Kunigunde, SURNAME=XXXHabsburg-Lothringen, CN=XXXMaria-Theresia Kunigunde XXXHabsburg-Lothringen, C=AT";
    private static final String CLIENT_CERT_ISSUER = "CN=a-sign-Premium-Test-Sig-02, OU=a-sign-Premium-Test-Sig-02, "
	    + "O=A-Trust Ges. f. Sicherheitssysteme im elektr. Datenverkehr GmbH, C=AT";
    private static final String ISSUER_CA_SUBJECT = "CN=a-sign-Premium-Test-Sig-02, OU=a-sign-Premium-Test-Sig-02, "
	    + "O=A-Trust Ges. f. Sicherheitssysteme im elektr. Datenverkehr GmbH, C=AT";
    private static final String ISSUER_CA_ISSUER = "CN=A-Trust-Test-Qual-02, OU=A-Trust-Test-Qual-02, O=A-Trust Ges. f."
	    + " Sicherheitssysteme im elektr. Datenverkehr GmbH, C=AT";


    private DIDCertificate cert;

    @BeforeTest
    public void init() throws IOException, CertificateException {
	InputStream qsCert = FileUtils.resolveResourceAsStream(DIDCertificateNGTest.class, "TestCertQSeCard.cer");
	CertificateFactory certFac = CertificateFactory.getInstance("X.509");
	Certificate qsCertObj = certFac.generateCertificate(qsCert);
	cert = new DIDCertificate(qsCertObj.getEncoded());
	InputStream qsCertRoot = FileUtils.resolveResourceAsStream(DIDCertificateNGTest.class, "TestCertQSCAeCard.cer");
	Certificate qsCertCA = certFac.generateCertificate(qsCertRoot);
	cert.addChainCertificate(qsCertCA.getEncoded());
    }


    @Test(groups={"it"})
    public void testChainBuilding() throws CertificateException {
	List<Certificate> certs = cert.buildPath();
	Assert.assertEquals(certs.size(), 2);

	X509Certificate clientCert = (X509Certificate) certs.get(0);
	X509Certificate issuerCa = (X509Certificate) certs.get(1);

	Assert.assertEquals(clientCert.getSubjectX500Principal().toString(), CLIENT_CERT_SUBJECT);
	Assert.assertEquals(clientCert.getIssuerX500Principal().toString(), CLIENT_CERT_ISSUER);
	Assert.assertEquals(issuerCa.getSubjectX500Principal().toString(), ISSUER_CA_SUBJECT);
	Assert.assertEquals(issuerCa.getIssuerX500Principal().toString(), ISSUER_CA_ISSUER);
    }

}
