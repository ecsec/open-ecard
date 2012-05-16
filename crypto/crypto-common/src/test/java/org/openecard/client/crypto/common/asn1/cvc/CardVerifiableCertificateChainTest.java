package org.openecard.client.crypto.common.asn1.cvc;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import org.junit.Test;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.crypto.common.asn1.eac.EFCardAccessTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardVerifiableCertificateChainTest {

    private ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();
    private ArrayList<CardVerifiableCertificate> malformedCertificates = new ArrayList<CardVerifiableCertificate>();
    private static final Logger _logger = LoggerFactory.getLogger(CardVerifiableCertificateChainTest.class);

    public void init() {
	try {
	    // Setup logger
	    ConsoleHandler ch = new ConsoleHandler();
	    ch.setLevel(Level.FINEST);
	    LogManager.getLogger(CardVerifiableCertificateChain.class.getName()).addHandler(ch);
	    LogManager.getLogger(CardVerifiableCertificateChain.class.getName()).setLevel(Level.FINEST);

	    byte[] cvca = loadTestFile("cert_cvca.cvcert");
	    byte[] dv = loadTestFile("cert_dv.cvcert");
	    byte[] at = loadTestFile("cert_at.cvcert");
	    byte[] malformedAT = loadTestFile("cert_at_malformed.cvcert");

	    certificates.add(new CardVerifiableCertificate(cvca));
	    certificates.add(new CardVerifiableCertificate(dv));
	    certificates.add(new CardVerifiableCertificate(at));

	    malformedCertificates.add(new CardVerifiableCertificate(cvca));
	    malformedCertificates.add(new CardVerifiableCertificate(dv));
	    malformedCertificates.add(new CardVerifiableCertificate(malformedAT));
	} catch (Exception ex) {
	    _logger.error(LoggingConstants.THROWING, "Exception", ex);
	}
    }

    @Test
    public void testChain() throws GeneralSecurityException {
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
	 * test malformed chain with cvca
	 */
	try {
	    chain = new CardVerifiableCertificateChain(malformedCertificates);
	    fail("A malformedCertificates should have been thrown because of malformed chain.");
	} catch (CertificateException e) {
	    /* expected */
	}

	/*
	 * test malformed chain without cvca
	 */
	try {
	    malformedCertificates.remove(0);
	    chain = new CardVerifiableCertificateChain(malformedCertificates);
	    fail("A malformedCertificates should have been thrown because of malformed chain.");
	} catch (CertificateException e) {
	    /* expected */
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
	    _logger.error(LoggingConstants.THROWING, "Exception", e);
	}
	return baos.toByteArray();
    }

}
