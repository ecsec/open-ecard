/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ResponseAPDU;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.EAC2InputType;
import org.openecard.client.common.sal.anytype.EAC2OutputType;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TerminalAuthenticationStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private Dispatcher dispatcher;
    private byte[] slotHandle;
    private static final Logger _logger = LogManager.getLogger(TerminalAuthenticationStep.class.getName());

    public TerminalAuthenticationStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    private ResponseAPDU transmitSingleAPDU(byte[] apdu) throws WSException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	ArrayList<byte[]> responses = new ArrayList<byte[]>() {{
	    add(new byte[] { (byte) 0x90, (byte) 0x00 });
	    add(new byte[] { 0x6A, (byte) 0x88 });
	}};

	Transmit t = CardCommands.makeTransmit(slotHandle, apdu, responses);
	TransmitResponse tr = (TransmitResponse) WSHelper.checkResult((TransmitResponse) dispatcher.deliver(t));
	return new ResponseAPDU(tr.getOutputAPDU().get(0));
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData)", new Object[] { didAuthenticate, internalData });
	} // </editor-fold>
	try {
	    this.slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();
	    this.transmitSingleAPDU(CardCommands.ManageSecurityEnvironment.setDST((byte[]) internalData.get("CAR")));

	    EAC2InputType eac2input = new EAC2InputType(didAuthenticate.getAuthenticationProtocolData());
	    internalData.put("pubkey", ByteUtils.toHexString(eac2input.getEphemeralPublicKey()));

	    for (CardVerifiableCertificate cvc : eac2input.getCertificates()) {
		this.transmitSingleAPDU(CardCommands.PerformSecurityOperation.verifySelfDescriptiveCertificate(cvc.getBody()));
		this.transmitSingleAPDU(CardCommands.ManageSecurityEnvironment.setDST(cvc.getCertificateHolderReference()));
	    }

	    CardVerifiableCertificate eServiceCertificate = (CardVerifiableCertificate) internalData.get("eServiceCertificate");
	    this.transmitSingleAPDU(CardCommands.PerformSecurityOperation.verifySelfDescriptiveCertificate(eServiceCertificate.getBody()));

	    //FIXME oid is fix
	    byte[] apdu = CardCommands.ManageSecurityEnvironment.setAT.TA(ObjectIdentifierUtils.getValue(TAObjectIdentifier.id_TA_ECDSA_SHA_256), eServiceCertificate.getCertificateHolderReference(), eac2input.getCompressedEphemeralPublicKey(), (byte[]) internalData.get("authenticatedAuxiliaryData"));
	    this.transmitSingleAPDU(apdu);

	    apdu = CardCommands.GetChallenge.generic();
	    byte[] challenge = this.transmitSingleAPDU(apdu).getData();

	    DIDAuthenticateResponse didAuthenticateResponse = new DIDAuthenticateResponse();
	    Result r = new Result();
	    r.setResultMajor(ECardConstants.Major.OK);
	    didAuthenticateResponse.setResult(r);

	    EAC2OutputType eac2output = new EAC2OutputType(didAuthenticate.getAuthenticationProtocolData(), challenge);

	    didAuthenticateResponse.setAuthenticationProtocolData(eac2output.getAuthDataType());
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData)", didAuthenticateResponse);
	    } // </editor-fold>
	    return didAuthenticateResponse;
	} catch (Exception ex) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData)", ex.getMessage(), ex);
	    } // </editor-fold>
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultUnknownError(ex.getMessage()));
	}
    }

}
