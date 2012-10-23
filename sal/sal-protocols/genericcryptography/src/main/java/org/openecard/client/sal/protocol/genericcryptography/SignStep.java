/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import javax.smartcardio.ResponseAPDU;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.util.CardCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the ProtocolStep interface for the Sign step of
 * the GenericCryptography protocol.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SignStep implements ProtocolStep<Sign, SignResponse> {

    private static final Logger _logger = LoggerFactory.getLogger(SignStep.class);

    private Dispatcher dispatcher;

    /**
     * 
     * @param dispatcher the dispatcher to use for message delivery
     */
    public SignStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Sign;
    }

    private ResponseAPDU transmitSingleAPDU(byte[] apdu, byte[] slotHandle) throws WSException, IllegalAccessException,
	    NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	ArrayList<byte[]> responses = new ArrayList<byte[]>() {
	    {
		add(new byte[] { (byte) 0x90, (byte) 0x00 });
		add(new byte[] { (byte) 0x63, (byte) 0xC3 });
	    }
	};

	Transmit t = CardCommands.makeTransmit(slotHandle, apdu, responses);
	TransmitResponse tr = (TransmitResponse) WSHelper.checkResult((TransmitResponse) this.dispatcher.deliver(t));
	return new ResponseAPDU(tr.getOutputAPDU().get(0));
    }

    @Override
    public SignResponse perform(Sign sign, Map<String, Object> internalData) {
	SignResponse res = new SignResponse();
	try {
	    ConnectionHandleType connectionHandle = sign.getConnectionHandle();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    CardStateEntry cardStateEntry = (CardStateEntry) internalData.get("cardState");
	    String didName = sign.getDIDName();
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName,
		    connectionHandle.getCardApplication());
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(
		    (iso.std.iso_iec._24727.tech.schema.CryptoMarkerType) didStructure.getDIDMarker());

	    if (!cardStateEntry.checkDIDSecurityCondition(connectionHandle.getCardApplication(), didName,
		    CryptographicServiceActionName.SIGN)) {
		return WSHelper.makeResponse(SignResponse.class,
			WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	    }

	    byte keyReference = cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef()[0];
	    byte algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getCardAlgRef()[0];

	    String[] signatureGenerationInfo = cryptoMarker.getSignatureGenerationInfo();
	    if (didStructure.getDIDScope().equals(DIDScopeType.LOCAL)) {
		keyReference = (byte) (0x80 | keyReference);
	    }
	    ResponseAPDU rapdu = null;
	    for (int i = 0; i < signatureGenerationInfo.length; i++) {
		String command = signatureGenerationInfo[i];
		if (command.equals("MSE_KEY")) {
		    String nextCommand = signatureGenerationInfo[i + 1];
		    // using next command until a better solution comes in mind
		    if (nextCommand.equals("INT_AUTH")) {
			rapdu = transmitSingleAPDU(CardCommands.ManageSecurityEnvironment.mseSelectPrKeyIntAuth(
				keyReference, algorithmIdentifier), slotHandle);
		    } else if (nextCommand.equals("PSO_CDS")) {
			rapdu = transmitSingleAPDU(CardCommands.ManageSecurityEnvironment.mseSelectPrKeySignature(
				keyReference, algorithmIdentifier), slotHandle);
		    }
		} else if (command.equals("PSO_CDS")) {
		    rapdu = transmitSingleAPDU(
			    CardCommands.PerformSecurityOperation.computeDigitalSignature(sign.getMessage()),
			    slotHandle);
		} else if (command.equals("INT_AUTH")) {
		    rapdu = transmitSingleAPDU(
			    CardCommands.InternalAuthenticate.generic(sign.getMessage(), (short) 0x0), slotHandle);
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
	    _logger.warn(e.getMessage(), e);
	    res = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResult(e));
	}
	return res;
    }

}
