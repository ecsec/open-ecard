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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.crypto.common.asn1.cvc;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Implements a chain of Card Verifiable Certificates.
 * See BSI-TR-03110, version 2.10, part 3, section 2.
 * See BSI-TR-03110, version 2.10, part 3, section C.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
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
     * @throws CertificateException
     */
    public CardVerifiableCertificateChain(List<CardVerifiableCertificate> certificates) throws CertificateException {
	parseChain(certificates);
	// FIXME not working yet with all servers.
//	verifyChain();
    }

    /**
     * Parses the certificate chain.
     *
     * @param certificates Certificates
     */
    private void parseChain(List<CardVerifiableCertificate> certificates) {
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
	if (cvca != null) {
	    if (!cvca.getCAR().equals(cvca.getCHR())
		    || !dv.getCAR().equals(cvca.getCHR())
		    || !terminal.getCAR().equals(dv.getCHR())) {
		throw new CertificateException("Malformed certificate chain");
	    }
	} else {
	    if (!terminal.getCAR().equals(dv.getCHR())) {
		throw new CertificateException("Malformed certificate chain");
	    }
	}
    }

    /**
     * Adds a new certificate to the chain.
     *
     * @param certificate Certificate
     */
    public void addCertificate(final CardVerifiableCertificate certificate) {
	parseChain(new LinkedList<CardVerifiableCertificate>() {

	    {
		add(certificate);
	    }
	});
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
    public List<CardVerifiableCertificate> getCertificateChain() {
	return chain;
    }

}
