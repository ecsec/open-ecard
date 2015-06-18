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

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * StepAction for performing PACE with the PUK.
 *
 * @author Dirk Petrautzki
 */
public class PUKStepAction extends StepAction {

    private static final Logger logger = LoggerFactory.getLogger(PUKStepAction.class);

    private static final String PIN_ID_PUK = "4";

    private final boolean capturePin;
    private final byte[] slotHandle;
    private final Dispatcher dispatcher;

    private String puk;

    /**
     * Create a new instance of PUKStepAction.
     *
     * @param capturePin True if the PIN has to be captured by software else false
     * @param slotHandle The unique SlotHandle for the card to use
     * @param step the step this action belongs to
     * @param dispatcher The Dispatcher to use
     */
    public PUKStepAction(boolean capturePin, byte[] slotHandle, Dispatcher dispatcher, Step step) {
	super(step);
	this.capturePin = capturePin;
	this.slotHandle = slotHandle;
	this.dispatcher = dispatcher;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isBack()) {
	    return new StepActionResult(StepActionResultStatus.BACK);
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
		// let the user enter the pin again, when there is none entered
		// TODO inform user that something with his input is wrong
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, puk);
	    }
	}

	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PUK);
	// perform PACE by sending an EstablishChannel
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(slotHandle);
	establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);

	try {
	    EstablishChannelResponse establishChannelResponse = (EstablishChannelResponse) dispatcher.deliver(establishChannel);
	    WSHelper.checkResult(establishChannelResponse);

	    // pace was successfully performed, so get to the next step
	    return new StepActionResult(StepActionResultStatus.NEXT);
	} catch (WSException ex) {
	    logger.info("Wrong PUK entered, trying again");
	    //TODO update the step to inform the user that he entered the puk wrong
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	} catch (InvocationTargetException ex) {
	    logger.error("Exception while dispatching EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (DispatcherException ex) {
	    logger.error("Failed to dispatch EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} finally {
	    DestroyChannel destroyChannel = new DestroyChannel();
	    destroyChannel.setSlotHandle(slotHandle);
	    try {
		dispatcher.deliver(destroyChannel);
	    } catch (InvocationTargetException ex) {
		logger.error("Exception while dispatching DestroyChannelCommand.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    } catch (DispatcherException ex) {
		logger.error("Failed to dispatch DestroyChannelCommand.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	}
    }

    /**
     * Verify the input of the user (e.g. no empty mandatory fields, pin length, allowed charset).
     * 
     * @param executionResults The results containing the OutputInfoUnits of interest.
     * @return True if the input of the user could be verified, else false
     */
    private boolean verifyUserInput(ExecutionResults executionResults) {
	// TODO: check pin length and possibly allowed charset with CardInfo file

	PasswordField pukField = (PasswordField) executionResults.getResult(UnblockPINDialog.PUK_FIELD);

	puk = pukField.getValue();
	if (puk.isEmpty()) {
	    return false;
	}

	return true;
    }

}
