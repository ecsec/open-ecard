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

package org.openecard.client.crypto.common.asn1.cvc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import org.openecard.client.crypto.common.asn1.eac.EFCardAccessTest;
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PublicKeyTest {

    private CardVerifiableCertificateChain chain;


    @BeforeTest
    public void setUp() throws Exception {
	try {
	    byte[] cvca = loadTestFile("cert_cvca.cvcert");
	    byte[] dv = loadTestFile("cert_dv.cvcert");
	    byte[] at = loadTestFile("cert_at.cvcert");

	    ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();
	    certificates.add(new CardVerifiableCertificate(cvca));
	    certificates.add(new CardVerifiableCertificate(dv));
	    certificates.add(new CardVerifiableCertificate(at));
	    chain = new CardVerifiableCertificateChain(certificates);

	} catch (Exception ex) {
	    fail(ex.getMessage());
	}
    }

    @Test
    public void testKeyCVCA() throws Exception {
	PublicKey pk = chain.getCVCACertificate().getPublicKey();
	assertEquals(pk.getObjectIdentifier(), TAObjectIdentifier.id_TA_ECDSA_SHA_512);
    }

    @Test
    public void testKeyDV() throws Exception {
	PublicKey pk = chain.getDVCertificate().getPublicKey();
	assertEquals(pk.getObjectIdentifier(), TAObjectIdentifier.id_TA_ECDSA_SHA_512);
    }

    @Test
    public void testKeyTerminal() throws Exception {
	PublicKey pk = chain.getTerminalCertificate().getPublicKey();
	assertEquals(pk.getObjectIdentifier(), TAObjectIdentifier.id_TA_ECDSA_SHA_512);
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
