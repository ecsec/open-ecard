package org.openecard.client.crypto.common.asn1.cvc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openecard.client.crypto.common.asn1.eac.EFCardAccessTest;


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

	    malformedCertificates.add(new CardVerifiableCertificate(cvca));
	    malformedCertificates.add(new CardVerifiableCertificate(dv));
	    malformedCertificates.add(new CardVerifiableCertificate(malformedAT));
	} catch (Exception ex) {
	    fail(ex.getMessage());
	}
    }

    @Test
    public void testChain() throws Exception {
	init();
	CardVerifiableCertificateChain chain = new CardVerifiableCertificateChain(certificates);
	assertEquals(certificates.get(0), chain.getCVCACertificate());
	assertEquals(certificates.get(1), chain.getDVCertificate());
	assertEquals(certificates.get(2), chain.getTerminalCertificate());
	assertEquals(certificates, chain.getCertificateChain());

	/*
	 * test missing cvca certificate
	 */
	certificates.remove(0);
	chain = new CardVerifiableCertificateChain(certificates);
	assertEquals(certificates.get(0), chain.getDVCertificate());
	assertEquals(certificates.get(1), chain.getTerminalCertificate());
	assertEquals(certificates, chain.getCertificateChain());

	/*
	 * test add
	 */
	certificates = new ArrayList<CardVerifiableCertificate>();
	certificates.add(new CardVerifiableCertificate(cvca));
	certificates.add(new CardVerifiableCertificate(dv));
	certificates.add(new CardVerifiableCertificate(at));
	chain.addCertificate(new CardVerifiableCertificate(cvca));
	assertTrue(certificates.get(0).equals(chain.getCVCACertificate()));
	assertTrue(certificates.get(1).equals(chain.getDVCertificate()));
	assertTrue(certificates.get(2).equals(chain.getTerminalCertificate()));

	/*
	 * test malformed chain with cvca
	 */
	try {
	    chain = new CardVerifiableCertificateChain(malformedCertificates);
	} catch (CertificateException expected) {
	}

	/*
	 * test malformed chain without cvca
	 */
	try {
	    malformedCertificates.remove(0);
	    chain = new CardVerifiableCertificateChain(malformedCertificates);
	} catch (CertificateException expected) {
	}
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
