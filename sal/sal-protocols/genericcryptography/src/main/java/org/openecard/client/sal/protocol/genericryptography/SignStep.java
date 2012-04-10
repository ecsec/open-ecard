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

package org.openecard.client.sal.protocol.genericryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ResponseAPDU;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.sal.TinySAL;
import org.openecard.ws.IFD;
import org.openecard.ws.SAL;


/**
 *
 * @author Dirk Petrautzki <petrautzki at hs-coburg.de>
 */
public class SignStep implements ProtocolStep<Sign, SignResponse> {

    private TinySAL sal;
    private IFD ifd;
    private static final Logger _logger = LogManager.getLogger(SignStep.class.getName());

    public SignStep(SAL sal, IFD ifd) {
	this.ifd = ifd;
	this.sal = (TinySAL) sal;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Sign;
    }

    private ResponseAPDU transmitSingleAPDU(byte[] apdu, byte[] slotHandle, IFD ifd) throws WSException {
	ArrayList<byte[]> responses = new ArrayList<byte[]>() {{
	    add(new byte[] { (byte) 0x90, (byte) 0x00 });
	    add(new byte[] { (byte) 0x63, (byte) 0xC3 });
	}};

	Transmit t = CardCommands.makeTransmit(slotHandle, apdu, responses);
	TransmitResponse tr = (TransmitResponse) WSHelper.checkResult(ifd.transmit(t));
	return new ResponseAPDU(tr.getOutputAPDU().get(0));
    }

    @Override
    public SignResponse perform(Sign sign, Map<String, Object> internalData) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "perform(Sign sign, Map<String, Object> internalData)");
	} // </editor-fold>
	SignResponse res = new SignResponse();
	try {
	    ConnectionHandleType connectionHandle = sign.getConnectionHandle();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    CardStateEntry cardStateEntry = sal.getStates().getEntry(connectionHandle);
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    String didName = sign.getDIDName();
	    DIDScopeType didScope = sign.getDIDScope();
	    DIDStructureType didStructure = cardInfoWrapper.getDIDStructure(didName, didScope);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType((iso.std.iso_iec._24727.tech.schema.CryptoMarkerType) didStructure.getDIDMarker());

	    if (!cardInfoWrapper.checkSecurityCondition(didName, didScope, CryptographicServiceActionName.SIGN)) {
		return WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	    }

	    byte keyReference = cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef()[0];
	    byte algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getCardAlgRef()[0];

	    ArrayList<String> signatureGenerationInfo = new ArrayList<String>(Arrays.asList(cryptoMarker.getSignatureGenerationInfo()));
	    // FIXME
	    byte localKeyRef = (byte) (0x80 | keyReference);
	    ResponseAPDU rapdu = null;
	    for (String command : signatureGenerationInfo) {
		if (command.equals("MSE_KEY")) {
		    // FIXME how to figure out with type of mse_key to use
		    rapdu = transmitSingleAPDU(CardCommands.ManageSecurityEnvironment.mseSelectPrKeySignature(localKeyRef, algorithmIdentifier), slotHandle, ifd);
		    /*
		     * rapdu = transmitSingleAPDU(
		     * CardCommands.ManageSecurityEnvironment
		     * .mseSelectPrKeyIntAuth(localKeyRef, algorithmIdentifier),
		     * slotHandle, ifd);
		     */
		} else if (command.equals("PSO_CDS")) {
		    rapdu = transmitSingleAPDU(CardCommands.PerformSecurityOperation.computeDigitalSignature(sign.getMessage()),
					       slotHandle, ifd);
		} else if (command.equals("INT_AUTH")) {
		    rapdu = transmitSingleAPDU(CardCommands.InternalAuthenticate.generic(sign.getMessage(), (short) 0x0), slotHandle, ifd);
		} else if (command.equals("MSE_RESTORE")) {
		    WSHelper.makeResultUnknownError("Not yet implemented");
		} else if (command.equals("MSE_HASH")) {
		    WSHelper.makeResultUnknownError("Not yet implemented");
		} else if (command.equals("PSO_HASH")) {
		    WSHelper.makeResultUnknownError("Not yet implemented");
		} else if (command.equals("MSE_DS")) {
		    WSHelper.makeResultUnknownError("Not yet implemented");
		} else if (command.equals("MSE_KEY_DS")) {
		    WSHelper.makeResultUnknownError("Not yet implemented");
		}
	    }

	    Result result = new Result();
	    result.setResultMajor(org.openecard.client.common.ECardConstants.Major.OK);
	    res.setResult(result);
	    res.setSignature(rapdu.getData());

	} catch (Exception e) {
	    e.printStackTrace();
	    res = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResult(e));
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "perform(Sign sign, Map<String, Object> internalData)", res);
	} // </editor-fold>
	return res;
    }

}
