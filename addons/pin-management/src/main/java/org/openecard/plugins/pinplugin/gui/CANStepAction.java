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

package org.openecard.plugins.pinplugin.gui;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;
import org.openecard.common.ifd.anytype.PACEInputType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * StepAction for performing PACE with the CAN.
 * <br/> If PACE fails the Step for entering CAN will be shown again.
 * <br/> If PACE succeeds the Step for PIN changing will be shown.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CANStepAction extends StepAction {

    // translation and logger
    private static final Logger logger = LoggerFactory.getLogger(CANStepAction.class);
    private final I18n lang = I18n.getTranslation("pinplugin");

    // translation constants
    private static final String CANSTEP_TITLE = "action.changepin.userconsent.canstep.title";
    private static final String PINSTEP_TITLE = "action.changepin.userconsent.pinstep.title";

    private static final String PIN_ID_CAN = "2";

    private final boolean capturePin;
    private final Dispatcher dispatcher;

    private String can;
    private ConnectionHandleType conHandle;
    private RecognizedState state;

    /**
     * Creates a new instance of CANStepAction.
     * 
     * @param capturePin True if the PIN has to be captured by software else false.
     * @param conHandle ConnectionHandle identifying the connection to the card.
     * @param dispatcher The Dispatcher to use.
     * @param step Step this Action belongs to.
     * @param state The current state of the PIN.
     */
    public CANStepAction(boolean capturePin, ConnectionHandleType conHandle, Dispatcher dispatcher, Step step,
	    RecognizedState state) {
	super(step);
	this.capturePin = capturePin;
	this.conHandle = conHandle;
	this.dispatcher = dispatcher;
	this.state = state;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isBack()) {
	    return new StepActionResult(StepActionResultStatus.BACK);
	}
	if (!state.equals(RecognizedState.PIN_suspended)) {
	    return new StepActionResult(StepActionResultStatus.NEXT);
	}

	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp;
	try {
	    tmp = new AuthDataMap(paceInput);
	} catch (ParserConfigurationException ex) {
	    logger.error("Failed to read empty Protocol data.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}

	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (capturePin) {
	    ExecutionResults executionResults = oldResults.get(getStepID());

	    if (!verifyUserInput(executionResults)) {
		// let the user enter the can again, when input verification failed
		return new StepActionResult(StepActionResultStatus.REPEAT, createReplacementStep(false, true));
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, can);
	    }
	}
	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_CAN);

	// perform PACE by EstablishChannelCommand
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(conHandle.getSlotHandle());
	establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);

	try {
	    EstablishChannelResponse ecr = (EstablishChannelResponse) dispatcher.deliver(establishChannel);
	    WSHelper.checkResult(ecr);

	    // pace was successfully performed, so get to the next step
	    String title = lang.translationForKey(PINSTEP_TITLE);
	    int retryCounter = 1;
	    Step replacementStep = new ChangePINStep("pin-entry", title, capturePin, retryCounter, false, false);
	    StepAction pinAction = new PINStepAction(capturePin, conHandle, dispatcher, replacementStep, retryCounter);
	    replacementStep.setAction(pinAction);
	    return new StepActionResult(StepActionResultStatus.NEXT, replacementStep);
	} catch (WSException ex) {
	    logger.info("Wrong CAN entered, trying again");
	    return new StepActionResult(StepActionResultStatus.REPEAT, createReplacementStep(true, false));
	} catch (InvocationTargetException ex) {
	    logger.error("Exception while dispatching EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (DispatcherException ex) {
	    logger.error("Failed to dispatch EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}
    }

    /**
     * Verify the input of the user (e.g. no empty mandatory fields, pin length, allowed charset).
     * 
     * @param executionResults The results containing the OutputInfoUnits of interest.
     * @return True if the input of the user could be verified, else false.
     */
    private boolean verifyUserInput(ExecutionResults executionResults) {
	// TODO: check pin length and possibly allowed charset with CardInfo file
	PasswordField canField = (PasswordField) executionResults.getResult(CANEntryStep.CAN_FIELD);
	can = canField.getValue();
	if (can.isEmpty() || can.length() != 6) {
	    return false;
	}
	return true;
    }

    /**
     * Create the step that asks the user to insert the CAN.
     * 
     * @return Step for CAN entry
     */
    private Step createReplacementStep(boolean enteredWrong, boolean verifyFailed) {
	String title = lang.translationForKey(CANSTEP_TITLE);
	CANEntryStep canStep = new CANEntryStep("can-entry", title, capturePin, state, enteredWrong, verifyFailed);
	StepAction pinAction = new CANStepAction(capturePin, conHandle, dispatcher, canStep, state);
	canStep.setAction(pinAction);
	return canStep;
    }

}
