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

package org.openecard.sal.protocol.eac.actions;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ByteUtils;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.anytype.PACEInputType;
import org.openecard.sal.protocol.eac.anytype.PasswordID;
import org.openecard.sal.protocol.eac.gui.PINStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * StepAction for capturing the user PIN on the EAC GUI.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PINStepAction extends StepAction {

    private static final Logger logger = LoggerFactory.getLogger(PINStepAction.class);

    private final EACData eacData;
    private final boolean capturePin;
    private final byte[] slotHandle;
    private final Dispatcher dispatcher;

    private int retryCounter;

    public PINStepAction(EACData eacData, boolean capturePin, byte[] slotHandle, Dispatcher dispatcher, Step step) {
	super(step);
	this.eacData = eacData;
	this.capturePin = capturePin;
	this.slotHandle = slotHandle;
	this.dispatcher = dispatcher;
	this.retryCounter = 0;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isBack()) {
	    return new StepActionResult(StepActionResultStatus.BACK);
	}

	// Create PACEInputType
	DIDAuthenticationDataType protoData = eacData.didRequest.getAuthenticationProtocolData();
	AuthDataMap paceAuthMap;
	try {
	    paceAuthMap = new AuthDataMap(protoData);
	} catch (ParserConfigurationException ex) {
	    logger.error("Failed to read EAC Protocol data.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}
	AuthDataResponse paceInputMap = paceAuthMap.createResponse(protoData);

	if (capturePin) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField p = (PasswordField) executionResults.getResult(PINStep.PIN_FIELD);
	    String pin = p.getValue();
	    // let the user enter the pin again, when there is none entered
	    // TODO: check pin length and possibly allowed charset with CardInfo file
	    if (pin.isEmpty()) {
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, pin);
	    }
	}

	// perform PACE
	paceInputMap.addElement(PACEInputType.PIN_ID, PasswordID.parse(eacData.pinID).getByteAsString());
	paceInputMap.addElement(PACEInputType.CHAT, eacData.selectedCHAT.toString());
	String certDesc = ByteUtils.toHexString(eacData.rawCertificateDescription);
	paceInputMap.addElement(PACEInputType.CERTIFICATE_DESCRIPTION, certDesc);

	// EstablishChannel
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(slotHandle);
	establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);

	try {
	    EstablishChannelResponse establishChannelResponse = (EstablishChannelResponse) dispatcher.deliver(establishChannel);
	    WSHelper.checkResult(establishChannelResponse);
	    eacData.paceResponse = establishChannelResponse;
	    // PACE completed successfully, proceed with next step
	    return new StepActionResult(StepActionResultStatus.NEXT);
	} catch (WSException ex) {
	    if (capturePin) {
		// repeat until retry counter is reached
		// TODO: replace 3 by a number determined in the pin management
		// TODO: retrycounter 3 does not work with the nPA, because it also needs the CAN after the second time
		if (retryCounter < 2) {
		    retryCounter++;
		    logger.info("Wrong PIN entered, trying again (try number {}).", retryCounter);
		    // TODO: replace this dialog with a version displaying the retry counter
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		} else {
		    logger.warn("Wrong PIN entered {} times.", retryCounter + 1);
		    return new StepActionResult(StepActionResultStatus.CANCEL);
		}
	    } else {
		logger.warn("PIN not entered successfully in terminal.");
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (IllegalAccessException ex) {
	    logger.error("Failed to dispatch EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (NoSuchMethodException ex) {
	    logger.error("Failed to dispatch EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (InvocationTargetException ex) {
	    logger.error("Failed to dispatch EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}
    }

}
