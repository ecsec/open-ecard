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

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a chain of Card Verifiable Certificates.
 * See BSI-TR-03110, version 2.10, part 3, section 2.
 * See BSI-TR-03110, version 2.10, part 3, section C.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardVerifiableCertificateChain {

    private static final Logger _logger = LoggerFactory.getLogger(CertificateDescription.class);
    private final ArrayList<CardVerifiableCertificate> certs = new ArrayList<>();
    private final ArrayList<CardVerifiableCertificate> cvcaCerts = new ArrayList<>();
    private final ArrayList<CardVerifiableCertificate> dvCerts = new ArrayList<>();
    private final ArrayList<CardVerifiableCertificate> terminalCerts = new ArrayList<>();

    /**
     * Creates a new certificate chain.
     *
     * @param certificates Certificates
     * @throws CertificateException
     */
    public CardVerifiableCertificateChain(List<CardVerifiableCertificate> certificates) throws CertificateException {
	parseChain(certificates);
	// FIXME not working yet with all servers.
//	verify();
	_logger.warn("Verification of the certificate chain is disabled.");
    }

    /**
     * Parses the certificate chain.
     *
     * @param certificates Certificates
     */
    private void parseChain(List<CardVerifiableCertificate> certificates) throws CertificateException {
	for (CardVerifiableCertificate cvc : certificates) {
	    if (containsChertificate(cvc)) {
		break;
	    }

	    CHAT.Role role = cvc.getCHAT().getRole();

	    if (role.equals(CHAT.Role.CVCA)) {
		cvcaCerts.add(cvc);
		certs.add(cvc);
	    } else if (role.equals(CHAT.Role.DV_OFFICIAL)
		    || role.equals(CHAT.Role.DV_NON_OFFICIAL)) {
		dvCerts.add(cvc);
		certs.add(cvc);
	    } else if (role.equals(CHAT.Role.AUTHENTICATION_TERMINAL)
		    || role.equals(CHAT.Role.INSPECTION_TERMINAL)
		    || role.equals(CHAT.Role.SIGNATURE_TERMINAL)) {
		terminalCerts.add(cvc);
		certs.add(cvc);
	    } else {
		throw new CertificateException("Malformed certificate.");
	    }
	}
    }

    /**
     * Verifies the certificate chain.
     * [1] The CAR and the CHR of the CVCA certificates should be equal.
     * [2] The CAR of a DV certificate should refer to the CHR of a CVCA certificate.
     * [3] The CAR of a terminal certificate should refer to the CHR of a DV certificate.
     *
     * @throws CertificateException
     */
    public void verify() throws CertificateException {
	verify(terminalCerts, dvCerts);
	verify(dvCerts, cvcaCerts);
	verify(cvcaCerts, cvcaCerts);
    }

    private void verify(List<CardVerifiableCertificate> authorities, List<CardVerifiableCertificate> holders)
	    throws CertificateException {
	for (Iterator<CardVerifiableCertificate> ai = authorities.iterator(); ai.hasNext();) {
	    CardVerifiableCertificate authority = ai.next();

	    for (CardVerifiableCertificate holder : holders) {
		if (authority.getCAR().equals(holder.getCHR())) {
		    break;
		}

		if (! ai.hasNext()) {
		    String msg = String.format("Malformed certificate chain: Cannot find a CHR for the CAR (%s).",
			    authority.getCAR());
		    throw new CertificateException(msg);
		}
	    }
	}
    }

    /**
     * Checks if the certificate chain contains the given certificate.
     *
     * @param cvc Certificate
     * @return True if the chain contains the certificate, false otherwise
     */
    public boolean containsChertificate(CardVerifiableCertificate cvc) {
	for (CardVerifiableCertificate c : certs) {
	    if (c.compare(cvc)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Adds a new certificate to the chain.
     *
     * @param certificate Certificate
     * @throws CertificateException
     */
    public void addCertificate(final CardVerifiableCertificate certificate) throws CertificateException {
	parseChain(new LinkedList<CardVerifiableCertificate>() {
	    {
		add(certificate);
	    }
	});
    }

    /**
     * Adds new certificates to the chain.
     *
     * @param certificates Certificate
     * @throws CertificateException
     */
    public void addCertificates(ArrayList<CardVerifiableCertificate> certificates) throws CertificateException {
	parseChain(certificates);
    }

    /**
     * Returns the certificates of the Country Verifying CAs (CVCA).
     *
     * @return CVCA certificates
     */
    public List<CardVerifiableCertificate> getCVCACertificates() {
	return cvcaCerts;
    }

    /**
     * Returns the certificates of the Document Verifiers (DV).
     *
     * @return DV certificates
     */
    public List<CardVerifiableCertificate> getDVCertificates() {
	return dvCerts;
    }

    /**
     * Returns the certificates of the terminal.
     *
     * @return Terminal certificates
     */
    public List<CardVerifiableCertificate> getTerminalCertificates() {
	return terminalCerts;
    }

    /**
     * Returns the certificate chain.
     *
     * @return Certificate chain
     */
    public List<CardVerifiableCertificate> getCertificates() {
	return certs;
    }

    /**
     * Returns the certificate chain from the CAR.
     *
     * @param car Certification Authority Reference (CAR)
     * @return Certificate chain
     * @throws CertificateException
     */
    public CardVerifiableCertificateChain getCertificateChainFromCAR(byte[] car) throws CertificateException {
	return getCertificateChainFromCAR(new PublicKeyReference(car));
    }

    /**
     * Returns the certificate chain from the CAR.
     *
     * @param car Certification Authority Reference (CAR)
     * @return Certificate chain
     * @throws CertificateException
     */
    public CardVerifiableCertificateChain getCertificateChainFromCAR(PublicKeyReference car) throws CertificateException {
	List<CardVerifiableCertificate> certChain = buildChain(certs, car);
	return new CardVerifiableCertificateChain(certChain);
    }

    private ArrayList<CardVerifiableCertificate> buildChain(ArrayList<CardVerifiableCertificate> certs, PublicKeyReference car) {
	ArrayList<CardVerifiableCertificate> certChain = new ArrayList<>();

	for (CardVerifiableCertificate c : certs) {
	    if (c.getCAR().compare(car)) {
		certChain.add(c);
		certChain.addAll(buildChain(certs, c.getCHR()));
	    }
	}

	return certChain;
    }

}
