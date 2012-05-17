package org.openecard.client.crypto.common.asn1.cvc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openecard.client.crypto.common.asn1.eac.EFCardAccessTest;
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PublicKeyTest {

    private CardVerifiableCertificateChain chain;

    public PublicKeyTest() {
    }

    @Before
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
	Assert.assertEquals(pk.getObjectIdentifier(), TAObjectIdentifier.id_TA_ECDSA_SHA_512);
    }

    @Test
    public void testKeyDV() throws Exception {
	PublicKey pk = chain.getDVCertificate().getPublicKey();
	Assert.assertEquals(pk.getObjectIdentifier(), TAObjectIdentifier.id_TA_ECDSA_SHA_512);
    }

    @Test
    public void testKeyTerminal() throws Exception {
	PublicKey pk = chain.getTerminalCertificate().getPublicKey();
	Assert.assertEquals(pk.getObjectIdentifier(), TAObjectIdentifier.id_TA_ECDSA_SHA_512);
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
