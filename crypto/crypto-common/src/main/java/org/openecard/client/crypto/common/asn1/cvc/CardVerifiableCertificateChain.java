/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.crypto.common.asn1.cvc;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

/**
 * Implements a chain of Card Verifiable Certificates.
 * See BSI-TR-03110, version 2.10, part 3, section 2.
 * See BSI-TR-03110, version 2.10, part 3, section C.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardVerifiableCertificateChain {

    private ArrayList<CardVerifiableCertificate> chain = new ArrayList<CardVerifiableCertificate>();
    private CardVerifiableCertificate cvca;
    private CardVerifiableCertificate dv;
    private CardVerifiableCertificate terminal;

    /**
     * Creates a new certificate chain.
     *
     * @param certificates Certificates
     * @throws GeneralSecurityException
     */
    public CardVerifiableCertificateChain(ArrayList<CardVerifiableCertificate> certificates) throws GeneralSecurityException {
	parseChain(certificates);
	verifyChain();
    }

    /**
     * Parses the certificate chain.
     *
     * @param certificates Certificates
     */
    private void parseChain(ArrayList<CardVerifiableCertificate> certificates) {
	for (int i = 0; i < certificates.size(); i++) {
	    CardVerifiableCertificate cvc = (CardVerifiableCertificate) certificates.get(i);

	    CHAT.Role role = cvc.getCHAT().getRole();
	    if (role.equals(CHAT.Role.CVCA)) {
		cvca = cvc;
		chain.add(cvca);
	    } else if (role.equals(CHAT.Role.DV_OFFICIAL)
		    || role.equals(CHAT.Role.DV_NON_OFFICIAL)) {
		dv = cvc;
		chain.add(dv);
	    } else if (role.equals(CHAT.Role.AUTHENTICATION_TERMINAL)
		    || role.equals(CHAT.Role.INSPECTION_TERMINAL)
		    || role.equals(CHAT.Role.SIGNATURE_TERMINAL)) {
		terminal = cvc;
		chain.add(terminal);
	    }
	}
    }

    /**
     * Verifies the certificate chain.
     * [1] The CAR and the CHR of the CVCA certificate should be equal.
     * [2] The CAR of the DV certificate should refer to the CHR of the CVCA.
     * [3] The CAR of the terminal certificate should refer to the CHR of the DV certificate.
     *
     * @throws CertificateException
     */
    private void verifyChain() throws CertificateException {
	if (!cvca.getCAR().equals(cvca.getCHR())
		|| !dv.getCAR().equals(cvca.getCHR())
		|| !terminal.getCAR().equals(dv.getCHR())) {
	    throw new CertificateException("Malformed certificate chain");
	}
    }

    /**
     * Returns the certificate of the Country Verifying CA (CVCA).
     *
     * @return CVCA certificate
     */
    public CardVerifiableCertificate getCVCACertificate() {
	return cvca;
    }

    /**
     * Returns the certificate of the Document Verifier (DV).
     *
     * @return DV certificate
     */
    public CardVerifiableCertificate getDVCertificate() {
	return dv;
    }

    /**
     * Returns the certificate of the terminal.
     *
     * @return Terminal certificate
     */
    public CardVerifiableCertificate getTerminalCertificate() {
	return terminal;
    }

    /**
     * Returns the certificate chain.
     *
     * @return Certificate chain
     */
    public ArrayList<CardVerifiableCertificate> getCertificateChain() {
	return chain;
    }
}
