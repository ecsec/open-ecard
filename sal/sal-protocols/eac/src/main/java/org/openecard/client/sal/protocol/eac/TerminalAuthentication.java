/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.sal.protocol.eac;

import java.util.ArrayList;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.apdu.ExternalAuthentication;
import org.openecard.client.common.apdu.GetChallenge;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.protocol.exception.ProtocolException;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.client.sal.protocol.eac.apdu.MSESetATTA;
import org.openecard.client.sal.protocol.eac.apdu.MSESetDST;
import org.openecard.client.sal.protocol.eac.apdu.PSOVerifyCertificate;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TerminalAuthentication {

    private Dispatcher dispatcher;
    private byte[] slotHandle;

    /**
     * Creates a new Terminal Authentication.
     *
     * @param ifd IFD
     * @param slotHandle Slot handle
     */
    public TerminalAuthentication(Dispatcher dispatcher, byte[] slotHandle) {
	this.dispatcher = dispatcher;
	this.slotHandle = slotHandle;
    }

    /**
     * Verify certificates. (Step 1)
     *
     * @param certificates Certificates
     * @throws ProtocolException
     */
    public void verifyCertificates(CardVerifiableCertificateChain certificateChain) throws ProtocolException {
	try {
	    ArrayList<CardVerifiableCertificate> certificates = certificateChain.getCertificateChain();

	    for (int i = certificates.size(); i >= 0; i--) {
		CardVerifiableCertificate cvc = (CardVerifiableCertificate) certificates.get(i);
		// MSE:SetDST
		CardCommandAPDU mseSetAT = new MSESetDST(cvc.getCAR().toByteArray());
		mseSetAT.transmit(dispatcher, slotHandle);
		// PSO:Verify Certificate
		CardCommandAPDU psovc = new PSOVerifyCertificate(cvc.getBody());
		psovc.transmit(dispatcher, slotHandle);
	    }
	} catch (WSException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Initialize Terminal Authentication. Sends an MSE:Set AT APDU. (Step 2)
     *
     * @param oid Terminal Authentication object identifier
     * @param chr Certificate Holder Reference (CHR)
     * @param key Ephemeral public key
     * @param aad Authenticated Auxiliary Data (AAD)
     * @throws ProtocolException
     */
    public void mseSetAT(byte[] oid, byte[] chr, byte[] key, byte[] aad) throws ProtocolException {
	try {
	    CardCommandAPDU mseSetAT = new MSESetATTA(oid, chr, key, aad);
	    mseSetAT.transmit(dispatcher, slotHandle);
	} catch (WSException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Gets a challenge from the PICC. (Step 3)
     *
     * @return Challenge
     * @throws ProtocolException
     */
    public byte[] getChallenge() throws ProtocolException {
	try {
	    CardCommandAPDU getChallenge = new GetChallenge();
	    CardResponseAPDU response = getChallenge.transmit(dispatcher, slotHandle);

	    return response.getData();
	} catch (WSException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Performs an External Authentication.
     *
     * @param terminalSignature Terminal signature
     * @throws ProtocolException
     */
    public void externalAuthentication(byte[] terminalSignature) throws ProtocolException {
	try {
	    CardCommandAPDU externalAuthentication = new ExternalAuthentication(terminalSignature);
	    externalAuthentication.transmit(dispatcher, slotHandle);
	} catch (WSException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

}
