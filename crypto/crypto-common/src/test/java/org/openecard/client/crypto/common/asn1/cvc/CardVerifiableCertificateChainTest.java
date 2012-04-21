package org.openecard.client.crypto.common.asn1.cvc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.openecard.client.crypto.common.asn1.eac.EFCardAccessTest;


/**
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class CardVerifiableCertificateChainTest {

    private ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();

    public void init() {
	try {
	    byte[] cvca = loadTestFile("cert_cvca.cvcert");
	    byte[] dv = loadTestFile("cert_dv.cvcert");
	    byte[] at = loadTestFile("cert_at.cvcert");

	    certificates.add(new CardVerifiableCertificate(cvca));
	    certificates.add(new CardVerifiableCertificate(dv));
	    certificates.add(new CardVerifiableCertificate(at));
	} catch (Exception ex) {
	    Logger.getLogger(CardVerifiableCertificateChainTest.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    @Test
    public void testChain() throws GeneralSecurityException {
	init();
	CardVerifiableCertificateChain chain = new CardVerifiableCertificateChain(certificates);
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
	    Logger.getLogger(EFCardAccessTest.class.getName()).log(Level.SEVERE, "Exception", e);
	}
	return baos.toByteArray();
    }

}
