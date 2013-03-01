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

package org.openecard.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import java.util.Map;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.InternalAuthenticate;
import org.openecard.common.apdu.ManageSecurityEnvironment;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.FunctionType;
import org.openecard.common.sal.ProtocolStep;
import org.openecard.common.sal.anytype.CryptoMarkerType;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.sal.protocol.genericcryptography.apdu.PSOComputeDigitalSignature;
import org.openecard.sal.protocol.genericcryptography.apdu.PSOHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the Sign step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.9.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SignStep implements ProtocolStep<Sign, SignResponse> {

    private static final Logger logger = LoggerFactory.getLogger(SignStep.class);
    private final Dispatcher dispatcher;

    /**
     * Creates a new SignStep.
     *
     * @param dispatcher Dispatcher
     */
    public SignStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Sign;
    }

    @Override
    public SignResponse perform(Sign sign, Map<String, Object> internalData) {
	SignResponse response = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(sign);
	    String didName = SALUtils.getDIDName(sign);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle);
	    DIDStructureType didStructure = SALUtils.getDIDStructure(sign, didName, cardStateEntry, connectionHandle);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didStructure.getDIDMarker());

	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    byte[] applicationID = connectionHandle.getCardApplication();
	    Assert.securityConditionDID(cardStateEntry, applicationID, didName, CryptographicServiceActionName.SIGN);

	    byte keyReference = cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef()[0];
	    byte[] algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getCardAlgRef();


	    if (didStructure.getDIDScope().equals(DIDScopeType.LOCAL)) {
		keyReference = (byte) (0x80 | keyReference);
	    }
	    byte[] message = sign.getMessage();
	    byte[] signature = new byte[0];

	    CardCommandAPDU commandAPDU = null;
	    CardResponseAPDU responseAPDU = null;

	    String[] signatureGenerationInfo = cryptoMarker.getSignatureGenerationInfo();
	    for (int i = 0; i < signatureGenerationInfo.length; i++) {
		String command = signatureGenerationInfo[i];

		if (command.equals("MSE_KEY")) {
		    commandAPDU = new ManageSecurityEnvironment.Set((byte) 0x41, ManageSecurityEnvironment.DST);
		} else if (command.equals("PSO_CDS")) {
		    commandAPDU = new PSOComputeDigitalSignature(message);
		} else if (command.equals("INT_AUTH")) {
		    commandAPDU = new InternalAuthenticate(message);
		} else if (command.equals("MSE_RESTORE")) {
		    commandAPDU = new ManageSecurityEnvironment.Restore(ManageSecurityEnvironment.DST);
		} else if (command.equals("MSE_HASH")) {
		    commandAPDU = new ManageSecurityEnvironment.Set((byte) 0x41, ManageSecurityEnvironment.HT);
		} else if (command.equals("PSO_HASH")) {
		    commandAPDU = new PSOHash(signature);
		} else if (command.equals("MSE_DS")) {
		    commandAPDU = new ManageSecurityEnvironment.Set((byte) 0x41, ManageSecurityEnvironment.DST);
		} else if (command.equals("MSE_KEY_DS")) {
		    commandAPDU = new ManageSecurityEnvironment.Set((byte) 0x41, ManageSecurityEnvironment.DST);
		} else {
		    throw new IncorrectParameterException("The signature generation command '" + command + "' is unknown.");
		}

		responseAPDU = commandAPDU.transmit(dispatcher, slotHandle);
	    }

	    response.setSignature(responseAPDU.getData());

	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.warn(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

}
