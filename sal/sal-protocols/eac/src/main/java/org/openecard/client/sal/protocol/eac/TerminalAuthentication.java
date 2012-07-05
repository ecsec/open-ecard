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

package org.openecard.client.sal.protocol.eac;

import java.util.Arrays;
import java.util.List;
import org.openecard.client.common.apdu.ExternalAuthentication;
import org.openecard.client.common.apdu.GetChallenge;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.apdu.exception.APDUException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.protocol.exception.ProtocolException;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.client.sal.protocol.eac.apdu.MSESetATTA;
import org.openecard.client.sal.protocol.eac.apdu.MSESetDST;
import org.openecard.client.sal.protocol.eac.apdu.PSOVerifyCertificate;


/**
 * Implements the Terminal Authentication protocol.
 * See BSI-TR-03110, version 2.10, part 2, Section B.3.4.
 * See BSI-TR-03110, version 2.10, part 3, Section B.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TerminalAuthentication {

    private final Dispatcher dispatcher;
    private final byte[] slotHandle;

    /**
     * Creates a new Terminal Authentication.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     */
    public TerminalAuthentication(Dispatcher dispatcher, byte[] slotHandle) {
	this.dispatcher = dispatcher;
	this.slotHandle = slotHandle;
    }

    /**
     * Verify certificates.
     * Sends an MSE:Set DST APDU and PSO:Verify Certificate APDU per certificate. (Protocol step 1)
     * See BSI-TR-03110, version 2.10, part 3, B.11.4.
     * See BSI-TR-03110, version 2.10, part 3, B.11.5.
     *
     * @param certificateChain Certificate chain
     * @throws ProtocolException
     */
    public void verifyCertificates(CardVerifiableCertificateChain certificateChain, byte[] currentCAR) throws ProtocolException {
	try {
	    List<CardVerifiableCertificate> certificates = certificateChain.getCertificateChain();

	    // get first certificate with matching CAR
	    int i = (certificates.size() - 1);
	    for (; i >= 0; i--) {
		CardVerifiableCertificate cvc = (CardVerifiableCertificate) certificates.get(i);
		if (Arrays.equals(cvc.getCAR().toByteArray(), currentCAR)) {
		    break;
		}
	    }

	    // send certificate-chain to the mrtd
	    for (; i >= 0; i--) {
		CardVerifiableCertificate cvc = (CardVerifiableCertificate) certificates.get(i);

		// MSE:SetDST APDU
		CardCommandAPDU mseSetDST = new MSESetDST(cvc.getCAR().toByteArray());
		mseSetDST.transmit(dispatcher, slotHandle);
		// PSO:Verify Certificate APDU
		CardCommandAPDU psovc = new PSOVerifyCertificate(cvc.getCertificate().getValue());
		psovc.transmit(dispatcher, slotHandle);
	    }
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Initializes the Terminal Authentication protocol.
     * Sends an MSE:Set AT APDU. (Protocol step 2)
     * See BSI-TR-03110, version 2.10, part 3, B.11.1.
     *
     * @param oID Terminal Authentication object identifier
     * @param chr Certificate Holder Reference (CHR)
     * @param key Ephemeral public key
     * @param aad Authenticated Auxiliary Data (AAD)
     * @throws ProtocolException
     */
    public void mseSetAT(byte[] oID, byte[] chr, byte[] key, byte[] aad) throws ProtocolException {
	try {
	    CardCommandAPDU mseSetAT = new MSESetATTA(oID, chr, key, aad);
	    mseSetAT.transmit(dispatcher, slotHandle);
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Gets a challenge from the PICC.
     * Sends a Get Challenge APDU. (Protocol step 3)
     * See BSI-TR-03110, version 2.10, part 3, B.11.6.
     *
     * @return Challenge
     * @throws ProtocolException
     */
    public byte[] getChallenge() throws ProtocolException {
	try {
	    CardCommandAPDU getChallenge = new GetChallenge();
	    CardResponseAPDU response = getChallenge.transmit(dispatcher, slotHandle);

	    return response.getData();
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Performs an External Authentication.
     * Sends an External Authentication APDU. (Protocol step 4)
     * See BSI-TR-03110, version 2.10, part 3, B.11.7.
     *
     * @param terminalSignature Terminal signature
     * @throws ProtocolException
     */
    public void externalAuthentication(byte[] terminalSignature) throws ProtocolException {
	try {
	    CardCommandAPDU externalAuthentication = new ExternalAuthentication(terminalSignature);
	    externalAuthentication.transmit(dispatcher, slotHandle);
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

}
