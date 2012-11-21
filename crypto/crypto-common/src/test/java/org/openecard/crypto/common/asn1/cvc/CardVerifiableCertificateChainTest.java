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

package org.openecard.crypto.common.asn1.cvc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import org.openecard.crypto.common.asn1.eac.EFCardAccessTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardVerifiableCertificateChainTest {

    private ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();
    private ArrayList<CardVerifiableCertificate> malformedCertificates = new ArrayList<CardVerifiableCertificate>();
    private byte[] cvca, dv, at, malformedAT;

    public void init() {
	try {
	    cvca = loadTestFile("cert_cvca.cvcert");
	    dv = loadTestFile("cert_dv.cvcert");
	    at = loadTestFile("cert_at.cvcert");
	    malformedAT = loadTestFile("cert_at_malformed.cvcert");

	    certificates.add(new CardVerifiableCertificate(cvca));
	    certificates.add(new CardVerifiableCertificate(dv));
	    certificates.add(new CardVerifiableCertificate(at));
//	    certificates.add(new CardVerifiableCertificate(malformedAT));

	    malformedCertificates.add(new CardVerifiableCertificate(cvca));
	    malformedCertificates.add(new CardVerifiableCertificate(dv));
	    malformedCertificates.add(new CardVerifiableCertificate(malformedAT));
	} catch (Exception ex) {
	    fail(ex.getMessage());
	}
    }

    @Test(enabled=true)
    public void testChain() throws Exception {
	init();
	CardVerifiableCertificateChain chain = new CardVerifiableCertificateChain(certificates);
	assertEquals(certificates.get(0), chain.getCVCACertificates().get(0));
	assertEquals(certificates.get(1), chain.getDVCertificates().get(0));
	assertEquals(certificates.get(2), chain.getTerminalCertificates().get(0));

	/*
	 * test missing cvca certificate
	 */
	certificates.remove(0);
	chain = new CardVerifiableCertificateChain(certificates);
	assertEquals(certificates.get(0), chain.getDVCertificates().get(0));
	assertEquals(certificates.get(1), chain.getTerminalCertificates().get(0));

	/*
	 * test add
	 */
	certificates = new ArrayList<CardVerifiableCertificate>();
	certificates.add(new CardVerifiableCertificate(cvca));
	certificates.add(new CardVerifiableCertificate(dv));
	certificates.add(new CardVerifiableCertificate(at));
	chain.addCertificate(new CardVerifiableCertificate(cvca));

	assertTrue(certificates.get(0).compare(chain.getCVCACertificates().get(0)));
	assertTrue(certificates.get(1).compare(chain.getDVCertificates().get(0)));
	assertTrue(certificates.get(2).compare(chain.getTerminalCertificates().get(0)));
    }

    private byte[] loadTestFile(String file) throws Exception {
	String path = "/" + file;
	InputStream is = EFCardAccessTest.class.getResourceAsStream(path);
	ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());
	try {
	    int b;
	    while ((b = is.read()) != -1) {
		baos.write((byte) b);
	    }
	} catch (Exception e) {
	    fail(e.getMessage());
	}
	return baos.toByteArray();
    }

}
