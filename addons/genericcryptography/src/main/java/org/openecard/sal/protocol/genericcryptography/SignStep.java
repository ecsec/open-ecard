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
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.util.Collections;
import java.util.Map;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.bouncycastle.util.Arrays;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.InternalAuthenticate;
import org.openecard.common.apdu.ManageSecurityEnvironment;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.anytype.CryptoMarkerType;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.tlv.TLV;
import org.openecard.common.util.ByteUtils;
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

    //TODO extract the blocksize from somewhere
    private static final byte BLOCKSIZE = (byte) 256;
    private static final byte SET_COMPUTATION = (byte) 0x41;
    private static final byte KEY_REFERENCE_PRIVATE_KEY = (byte) 0x84;

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

	    byte[] keyReference = cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef();
	    byte[] algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getCardAlgRef();

	    if (didStructure.getDIDScope().equals(DIDScopeType.LOCAL)) {
		keyReference[0] = (byte) (0x80 | keyReference[0]);
	    }
	    byte[] message = sign.getMessage();
	    byte[] signature = new byte[0];

	    TLV tagAlgorithmIdentifier = new TLV();
	    tagAlgorithmIdentifier.setTagNumWithClass(0x80);
	    tagAlgorithmIdentifier.setValue(algorithmIdentifier);

	    CardCommandAPDU cmdAPDU = null;
	    CardResponseAPDU responseAPDU = null;

	    String[] signatureGenerationInfo = cryptoMarker.getSignatureGenerationInfo();
	    for (int i = 0; i < signatureGenerationInfo.length; i++) {
		String command = signatureGenerationInfo[i];
		String nextCmd = "";

		if (i < signatureGenerationInfo.length - 1) {
		    nextCmd = signatureGenerationInfo[i + 1];
		}

		if (command.equals("MSE_KEY")) {
		    TLV tagKeyReference = new TLV();
		    tagKeyReference.setTagNumWithClass(KEY_REFERENCE_PRIVATE_KEY);
		    tagKeyReference.setValue(keyReference);
		    if (nextCmd.equals("PSO_CDS")) {
			byte[] mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER());
			cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.DST, mseData);
		    } else if (nextCmd.equals("INT_AUTH")) {
			byte[] mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER());
			cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.AT, mseData);
		    } else {
			String msg = "The command 'MSE_KEY' followed by '" + nextCmd + "' is currently not supported.";
			logger.error(msg);
			throw new IncorrectParameterException(msg);
		    }
		} else if (command.equals("PSO_CDS")) {
		    cmdAPDU = new PSOComputeDigitalSignature(message, BLOCKSIZE);
		} else if (command.equals("INT_AUTH")) {
		    cmdAPDU = new InternalAuthenticate(message, BLOCKSIZE);
		} else if (command.equals("MSE_RESTORE")) {
		    cmdAPDU = new ManageSecurityEnvironment.Restore(ManageSecurityEnvironment.DST);
		} else if (command.equals("MSE_HASH")) {
		    cmdAPDU = new ManageSecurityEnvironment.Set(SET_COMPUTATION, ManageSecurityEnvironment.HT);
		} else if (command.equals("PSO_HASH")) {
		    cmdAPDU = new PSOHash(signature);
		} else if (command.equals("MSE_DS")) {
		    cmdAPDU = new ManageSecurityEnvironment.Set(SET_COMPUTATION, ManageSecurityEnvironment.DST);
		} else if (command.equals("MSE_KEY_DS")) {
		    cmdAPDU = new ManageSecurityEnvironment.Set(SET_COMPUTATION, ManageSecurityEnvironment.DST);
		} else {
		    String msg = "The signature generation command '" + command + "' is unknown.";
		    throw new IncorrectParameterException(msg);
		}

		responseAPDU = cmdAPDU.transmit(dispatcher, slotHandle, Collections.<byte[]>emptyList());
	    }

	    byte[] signedMessage = responseAPDU.getData();

	    // check if further response data is available
	    while (responseAPDU.getTrailer()[0] == (byte) 0x61) {
		CardCommandAPDU getResponseData = new CardCommandAPDU((byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00,
			responseAPDU.getTrailer()[1]);
		responseAPDU = getResponseData.transmit(dispatcher, slotHandle, Collections.<byte[]>emptyList());
		signedMessage = Arrays.concatenate(signedMessage, responseAPDU.getData());
	    }

	    if (! Arrays.areEqual(responseAPDU.getTrailer(), new byte[] {(byte) 0x90, (byte) 0x00})) {
		TransmitResponse tr = new TransmitResponse();
		tr.getOutputAPDU().add(responseAPDU.toByteArray());
		WSHelper.checkResult(response);
	    }

	    response.setSignature(signedMessage);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.warn(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

}
