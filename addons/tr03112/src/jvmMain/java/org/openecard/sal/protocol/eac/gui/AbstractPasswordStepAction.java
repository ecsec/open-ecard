/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.sal.protocol.eac.gui;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.addon.Context;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;
import org.openecard.common.ifd.anytype.PACEInputType;
import org.openecard.common.util.ByteUtils;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.ifd.protocol.pace.common.PasswordID;
import org.openecard.sal.protocol.eac.EACData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public abstract class AbstractPasswordStepAction extends StepAction {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPasswordStepAction.class);

    private static final String PIN_ID_CAN = "2";

    protected final Context addonCtx;
    protected final EACData eacData;
    protected final boolean capturePin;
    protected final PINStep step;
    protected final DynamicContext ctx;

    public AbstractPasswordStepAction(Context addonCtx, EACData eacData, boolean capturePin, PINStep step) {
	super(step);
	this.addonCtx = addonCtx;
	this.eacData = eacData;
	this.capturePin = capturePin;
	this.step = step;
	this.ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	// indicate that the card stays connected
    }

    protected EstablishChannelResponse performPACEWithPIN(Map<String, ExecutionResults> oldResults, ConnectionHandleType conHandle) throws WSHelper.WSException, InterruptedException, PinOrCanEmptyException {
	DIDAuthenticationDataType protoData = eacData.didRequest.getAuthenticationProtocolData();
	AuthDataMap paceAuthMap;
	try {
	    paceAuthMap = new AuthDataMap(protoData);
	} catch (ParserConfigurationException ex) {
	    LOG.error("Failed to read EAC Protocol data.", ex);
	    return null;
	}
	AuthDataResponse paceInputMap = paceAuthMap.createResponse(protoData);

	if (capturePin) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField p = (PasswordField) executionResults.getResult(PINStep.PIN_FIELD);
	    if (p == null) {
		throw new PinOrCanEmptyException("The PIN field is missing");
	    }
	    char[] pinIn = p.getValue();
	    // let the user enter the pin again, when there is none entered
	    // TODO: check pin length and possibly allowed charset with CardInfo file
	    if (pinIn.length == 0) {
		throw new PinOrCanEmptyException("PIN must not be empty");
	    } else {
		// NOTE: saving pin as string prevents later removal of the value from memory !!!
		paceInputMap.addElement(PACEInputType.PIN, new String(pinIn));
	    }
	}

	// perform PACE
	paceInputMap.addElement(PACEInputType.PIN_ID, PasswordID.parse(eacData.pinID).getByteAsString());
	paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false");
	paceInputMap.addElement(PACEInputType.CHAT, eacData.selectedCHAT.toString());
	String certDesc = ByteUtils.toHexString(eacData.rawCertificateDescription);
	paceInputMap.addElement(PACEInputType.CERTIFICATE_DESCRIPTION, certDesc);
	EstablishChannel eChannel = createEstablishChannelStructure(conHandle, paceInputMap);
	EstablishChannelResponse res = (EstablishChannelResponse) addonCtx.getDispatcher().safeDeliver(eChannel);

	return res;
    }

    protected EstablishChannelResponse performPACEWithCAN(Map<String, ExecutionResults> oldResults, ConnectionHandleType conHandle) throws WSHelper.WSException, InterruptedException, CanLengthInvalidException {
	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp;
	try {
	    tmp = new AuthDataMap(paceInput);
	} catch (ParserConfigurationException ex) {
	    LOG.error("Failed to read empty Protocol data.", ex);
	    return null;
	}

	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (capturePin) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField canField = (PasswordField) executionResults.getResult(PINStep.CAN_FIELD);
	    if (canField == null) {
		throw new CanLengthInvalidException("The CAN field is missing");
	    }
	    String canValue = new String(canField.getValue());

	    if (canValue.length() != 6) {
		// let the user enter the can again, when input verification failed
		throw new CanLengthInvalidException("Can does not contain 6 digits.");
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, canValue);
	    }
	}
	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_CAN);
	paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false");

	// perform PACE by EstablishChannelCommand
	EstablishChannel eChannel = createEstablishChannelStructure(conHandle, paceInputMap);
	EstablishChannelResponse res = (EstablishChannelResponse) addonCtx.getDispatcher().safeDeliver(eChannel);

	return res;
    }

    private EstablishChannel createEstablishChannelStructure(ConnectionHandleType conHandle, AuthDataResponse paceInputMap) {
	// EstablishChannel
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(conHandle.getSlotHandle());
	establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);
	return establishChannel;
    }

}
