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
import iso.std.iso_iec._24727.tech.schema.ControlIFD;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;
import org.openecard.common.apdu.ResetRetryCounter;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.ifd.anytype.PACEInputType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.ifd.scio.IFDException;
import org.openecard.ifd.scio.reader.PCSCFeatures;
import org.openecard.ifd.scio.reader.PCSCPinModify;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static iso.std.iso_iec._24727.tech.schema.PasswordTypeType.ASCII_NUMERIC;


/**
 * StepAction for performing PACE with the PIN and modify it.
 * <br/> This StepAction tries to perform PACE with the PIN as often as possible in dependence of the retry counter.
 * <br/> If PACE was executed successful the PIN is modified.
 * <br/> If the retry counter reaches 1 the CANEntryStep will be shown.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PINStepAction extends StepAction {

    // translation and logger
    private static final Logger logger = LoggerFactory.getLogger(PINStepAction.class);
    private final I18n lang = I18n.getTranslation("pinplugin");

    // translation constants
    private static final String PINSTEP_TITLE = "action.changepin.userconsent.pinstep.title";
    private static final String CANSTEP_TITLE = "action.changepin.userconsent.canstep.title";

    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String PIN_ID_PIN = "3";

    private final boolean capturePin;
    private final ConnectionHandleType conHandle;
    private final Dispatcher dispatcher;

    private int retryCounter;
    private String oldPIN;
    private byte[] newPIN;
    private byte[] newPINRepeat;

    /**
     * Create a new instance of PINStepAction.
     *
     * @param capturePin True if the PIN has to be captured by software else false
     * @param conHandle The unique ConnectionHandle for the card connection
     * @param step the step this action belongs to
     * @param dispatcher The Dispatcher to use
     * @param retryCounter RetryCounter of the PIN
     */
    public PINStepAction(boolean capturePin, ConnectionHandleType conHandle, Dispatcher dispatcher, Step step, int retryCounter) {
	super(step);
	this.capturePin = capturePin;
	this.conHandle = conHandle;
	this.dispatcher = dispatcher;
	this.retryCounter = retryCounter;
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
		// let the user enter the pin again, when input verification failed
		return new StepActionResult(StepActionResultStatus.REPEAT, createPINReplacementStep(false, true));
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, oldPIN);
	    }
	}
	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PIN);

	// perform PACE by EstablishChannel
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(conHandle.getSlotHandle());
	establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);

	try {
	    EstablishChannelResponse establishChannelResponse = 
		    (EstablishChannelResponse) dispatcher.deliver(establishChannel);
	    WSHelper.checkResult(establishChannelResponse);
	    // PACE completed successfully, we now modify the pin
	    if (capturePin) {
		sendResetRetryCounter();
	    } else {
		sendModifyPIN();
	    }
	    // PIN modified successfully, disconnect and proceed with next step
	    Disconnect disconnect = new Disconnect();
	    disconnect.setSlotHandle(conHandle.getSlotHandle());
	    try {
		dispatcher.deliver(disconnect);
	    } catch (IllegalArgumentException ex) {
		logger.error("Failed to transmit Disconnect command.", ex);
	    } catch (InvocationTargetException ex) {
		logger.error("Failed to transmit Disconnect command.", ex);
	    } catch (DispatcherException ex) {
		logger.error("Failed to transmit Disconnect command.", ex);
	    }
	    return new StepActionResult(StepActionResultStatus.NEXT);
	} catch (WSException ex) {
	    if (capturePin) {
		retryCounter--;
		logger.info("Wrong PIN entered, trying again (remaining tries {}).", retryCounter);
		if (retryCounter == 1) {
		    Step replacementStep = createCANReplacementStep();
		    return new StepActionResult(StepActionResultStatus.BACK, replacementStep);
		} else {
		    Step replacementStep = createPINReplacementStep(true, false);
		    return new StepActionResult(StepActionResultStatus.REPEAT, replacementStep);
		}
	    } else {
		logger.warn("PIN not entered successfully in terminal.");
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (InvocationTargetException ex) {
	    logger.error("Failed to dispatch EstablishChannelCommand.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (APDUException ex) {
	    logger.error("Failed to transmit Reset Retry Counter APDU.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (IllegalArgumentException ex) {
	    logger.error("Failed to transmit Reset Retry Counter APDU.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (IFDException ex) {
	    logger.error("Failed to transmit Reset Retry Counter APDU.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (DispatcherException ex) {
	    logger.error("Failed to transmit Reset Retry Counter APDU.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}
    }

    /**
     * Create the step that asks the user to insert the CAN.
     * 
     * @return Step for CAN entry
     */
    private Step createCANReplacementStep() {
	String title = lang.translationForKey(CANSTEP_TITLE);
	RecognizedState state = RecognizedState.PIN_suspended;
	CANEntryStep canStep = new CANEntryStep("can-entry", title , capturePin, state, false, false);
	StepAction pinAction = new CANStepAction(capturePin, conHandle, dispatcher, canStep, state);
	canStep.setAction(pinAction);
	return canStep;
    }

    /**
     * Send a ModifyPIN-PCSC-Command to the Terminal.
     * 
     * @throws IFDException If building the Command fails.
     * @throws InvocationTargetException If the ControlIFD command fails.
     * @throws DispatcherException If an error in the dispatcher occurs.
     */
    private void sendModifyPIN() throws IFDException, InvocationTargetException, DispatcherException {
	PasswordAttributesType pwdAttr = create(true, ASCII_NUMERIC, 6, 6, 6);
	pwdAttr.setPadChar(new byte[] { (byte) 0x3F });
	PCSCPinModify ctrlStruct = new PCSCPinModify(pwdAttr, StringUtils.toByteArray("002C0203"));
	byte[] structData = ctrlStruct.toBytes();

	ControlIFD controlIFD = new ControlIFD();
	controlIFD.setCommand(ByteUtils.concatenate((byte) PCSCFeatures.MODIFY_PIN_DIRECT, structData));
	controlIFD.setContextHandle(conHandle.getContextHandle());
	controlIFD.setIFDName(conHandle.getIFDName());
	dispatcher.deliver(controlIFD);
    }

    /**
     * Send a ResetRetryCounter-APDU.
     * 
     * @throws APDUException if the RRC-APDU could not be sent successfully
     */
    private void sendResetRetryCounter() throws APDUException {
	ResetRetryCounter apdu = new ResetRetryCounter(newPIN, (byte) 0x03);
	apdu.transmit(dispatcher, conHandle.getSlotHandle());
    }

    private static PasswordAttributesType create(boolean needsPadding, PasswordTypeType pwdType, int minLen,
	    int storedLen, int maxLen) {
	PasswordAttributesType r = new PasswordAttributesType();
	r.setMinLength(BigInteger.valueOf(minLen));
	r.setStoredLength(BigInteger.valueOf(storedLen));
	r.setPwdType(pwdType);
	if (needsPadding) {
	    r.getPwdFlags().add("needs-padding");
	}
	r.setMaxLength(BigInteger.valueOf(maxLen));
	return r;
    }

    /**
     * Verify the input of the user (e.g. no empty mandatory fields, pin length, allowed charset).
     * 
     * @param executionResults The results containing the OutputInfoUnits of interest.
     * @return True if the input of the user could be verified, else false.
     */
    private boolean verifyUserInput(ExecutionResults executionResults) {
	// TODO: check pin length and possibly allowed charset with CardInfo file

	PasswordField fieldOldPIN = (PasswordField) executionResults.getResult(ChangePINStep.OLD_PIN_FIELD);
	PasswordField fieldNewPIN = (PasswordField) executionResults.getResult(ChangePINStep.NEW_PIN_FIELD);
	PasswordField fieldNewPINRepeat = (PasswordField) executionResults.getResult(ChangePINStep.NEW_PIN_REPEAT_FIELD);

	oldPIN = fieldOldPIN.getValue();

	if (oldPIN.isEmpty()) {
	    return false;
	}
	if (fieldNewPIN.getValue().isEmpty()) {
	    return false;
	} else {
	    try {
		newPIN = fieldNewPIN.getValue().getBytes(ISO_8859_1);
	    } catch (UnsupportedEncodingException e) {
		return false;
	    }
	}
	if (fieldNewPINRepeat.getValue().isEmpty()) {
	    return false;
	} else {
	    try {
		newPINRepeat = fieldNewPINRepeat.getValue().getBytes(ISO_8859_1);
	    } catch (UnsupportedEncodingException e) {
		return false;
	    }
	}

	if (!ByteUtils.compare(newPIN, newPINRepeat)) {
	    return false;
	}
	return true;
    }

    /**
     * Create the step that asks the user to insert the old and new pins.
     * 
     * @return Step for PIN entry
     */
    private Step createPINReplacementStep(boolean enteredWrong, boolean verifyFailed) {
	String title = lang.translationForKey(PINSTEP_TITLE);
	Step changePINStep = new ChangePINStep("pin-entry", title, capturePin, retryCounter, enteredWrong, verifyFailed);
	StepAction pinAction = new PINStepAction(capturePin, conHandle, dispatcher, changePINStep, retryCounter);
	changePINStep.setAction(pinAction);
	return changePINStep;
    }

}
