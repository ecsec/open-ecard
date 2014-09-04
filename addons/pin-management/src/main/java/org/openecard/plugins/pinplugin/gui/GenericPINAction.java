/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ControlIFDResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import static iso.std.iso_iec._24727.tech.schema.PasswordTypeType.ASCII_NUMERIC;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;
import org.openecard.common.apdu.ResetRetryCounter;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.ifd.anytype.PACEInputType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
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


/**
 *
 * @author Hans-Martin Haase 
 */
public class GenericPINAction extends StepAction {

    private static final Logger logger = LoggerFactory.getLogger(GenericPINAction.class);
    private static final String PIN_ID_CAN = "2";
    private static final String PIN_ID_PIN = "3";
    private static final String PIN_ID_PUK = "4";
    private static final String ISO_8859_1 = "ISO-8859-1";

    // Translation constants
    private static final String CAN_SUCCESS = "action.changepin.userconsent.canstep.can_success";
    private static final String PUK_SUCCESS = "action.unblockpin.userconsent.pukstep.puk_success";
    private static final String CHANGE_SUCCESS = "action.changepin.userconsent.successstep.description";
    private static final String ERROR_CARD_REMOVED = "action.error.card.removed";

    private final I18n lang = I18n.getTranslation("pinplugin");
    private final boolean capturePin;
    private final ConnectionHandleType cHandle;
    private final Dispatcher dispatcher;
    private final byte[] slotHandle;

    private final GenericPINStep gPINStep;

    private RecognizedState state;


    public GenericPINAction(String stepID, RecognizedState state, ConnectionHandleType cHandle, Dispatcher dispatcher,
	    GenericPINStep gPINStep, boolean capturePin) {
	super(gPINStep);
	this.gPINStep = gPINStep;
	this.capturePin = capturePin;
	this.cHandle = cHandle;
	this.dispatcher = dispatcher;
	this.state = state;
	slotHandle = cHandle.getSlotHandle();
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isCancelled()) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}


	switch (state) {
	    case PIN_activated_RC3:
	    case PIN_activated_RC2:
		return performPINChange(oldResults);
	    case PIN_suspended:
		return performResumePIN(oldResults);
	    case PIN_resumed:
		return performPINChange(oldResults);
	    case PIN_deactivated:
		// nothing todo here the error message was displayed so just return next.
		return new StepActionResult(StepActionResultStatus.NEXT);
	    case PIN_blocked:
		return performUnblockPIN(oldResults);
	    case PUK_blocked:
		// nothing todo here the error message was displayed so just return next.
		return new StepActionResult(StepActionResultStatus.NEXT);
	    case UNKNOWN:
		// nothing todo here the error message was displayed so just return next.
		return new StepActionResult(StepActionResultStatus.NEXT);
	}
	return null;

    }

    private EstablishChannelResponse performPACEWithPIN(Map<String, ExecutionResults> oldResults)
	    throws DispatcherException, InvocationTargetException, ParserConfigurationException {
	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp = new AuthDataMap(paceInput);

	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (capturePin) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField oldPINField = (PasswordField) executionResults.getResult(GenericPINStep.OLD_PIN_FIELD);
	    String oldPINValue = oldPINField.getValue();

	    if (oldPINValue.length() > 6 && oldPINValue.length() < 5) {
		// let the user enter the can again, when input verification failed
		return null;
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, oldPINValue);
	    }
	}
	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PIN);

	// perform PACE by EstablishChannelCommand
	EstablishChannel eChannel = createEstablishChannelStructure(paceInputMap);
	return (EstablishChannelResponse) dispatcher.deliver(eChannel);
    }

    private EstablishChannelResponse performPACEWithCAN(Map<String, ExecutionResults> oldResults)
	    throws DispatcherException, InvocationTargetException, ParserConfigurationException {
	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp = new AuthDataMap(paceInput);

	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (capturePin) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField canField = (PasswordField) executionResults.getResult(GenericPINStep.CAN_FIELD);
	    String canValue = canField.getValue();

	    if (canValue.length() != 6) {
		// let the user enter the can again, when input verification failed
		return null;
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, canValue);
	    }
	}
	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_CAN);

	// perform PACE by EstablishChannelCommand
	EstablishChannel eChannel = createEstablishChannelStructure(paceInputMap);
	return (EstablishChannelResponse) dispatcher.deliver(eChannel);
    }

    private EstablishChannelResponse performPACEWithPUK(Map<String, ExecutionResults> oldResults)
	    throws DispatcherException, InvocationTargetException, ParserConfigurationException {
	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp = new AuthDataMap(paceInput);
	
	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (capturePin) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField pukField = (PasswordField) executionResults.getResult(GenericPINStep.PUK_FIELD);
	    String pukValue = pukField.getValue();

	    if (pukValue.length() != 10) {
		// let the user enter the pin again, when there is none entered
		// TODO inform user that something with his input is wrong
		return null;
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, pukValue);
	    }
	}

	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PUK);

	EstablishChannel eChannel = createEstablishChannelStructure(paceInputMap);
	return (EstablishChannelResponse) dispatcher.deliver(eChannel);
    }

    private EstablishChannel createEstablishChannelStructure(AuthDataResponse paceInputMap) {
	// EstablishChannel
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(slotHandle);
	establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);
	return establishChannel;
    }

    private StepActionResult performPINChange(Map<String, ExecutionResults> oldResults) {
	String newPINValue = null;
	String newPINRepeatValue = null;
 	if (capturePin) {
	    try {
		ExecutionResults executionResults = oldResults.get(getStepID());
		PasswordField newPINField = (PasswordField) executionResults.getResult(GenericPINStep.NEW_PIN_FIELD);
		newPINValue = newPINField.getValue();

		PasswordField newPINRepeatField = (PasswordField) executionResults.getResult(GenericPINStep.NEW_PIN_REPEAT_FIELD);
		newPINRepeatValue = newPINRepeatField.getValue();

		byte[] pin1 = newPINValue.getBytes(ISO_8859_1);
		byte[] pin2 = newPINRepeatValue.getBytes(ISO_8859_1);

		if (! ByteUtils.compare(pin1, pin2)) {
		    gPINStep.updateState(state); // to reset the text fields
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		}

	    } catch (UnsupportedEncodingException ex) {
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }

	}

	try {
	    EstablishChannelResponse pinResponse = performPACEWithPIN(oldResults);
	    if (pinResponse == null) {
		// the entered pin has a wrong format repeat the entering of the data
		gPINStep.setFailedPINVerify(false);
		gPINStep.setWrongPINFormat(true);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	    WSHelper.checkResult(pinResponse);

	    if (capturePin) {
		// pace with the old pin was successful now modify the pin

		if (newPINValue.equals(newPINRepeatValue) && newPINValue.length() == 6) {
		    CardResponseAPDU resp = sendResetRetryCounter(newPINValue.getBytes(ISO_8859_1));
		    // TODO check result
		}
	    } else {
		ControlIFDResponse resp = sendModifyPIN();
		WSHelper.checkResult(resp);
	    }

	    // PIN modified successfully, proceed with next step
	    return new StepActionResult(StepActionResultStatus.REPEAT, 
		    generateSuccessStep(lang.translationForKey(CHANGE_SUCCESS)));
	} catch (InvocationTargetException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (DispatcherException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (WSHelper.WSException ex) {
	    // This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		logger.error("User canceled the authentication manually.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }

	    // for people which think they have to remove the card in the process
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
		logger.error("The SlotHandle was invalid so probably the user removed the card or an reset occurred.");
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)));
	    }

	    gPINStep.setFailedPINVerify(true);
	    gPINStep.setWrongPINFormat(false);
	    switch(state) {
		case PIN_activated_RC3:
		    gPINStep.updateState(RecognizedState.PIN_activated_RC2);
		    state = RecognizedState.PIN_activated_RC2;
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		case PIN_activated_RC2:
		    gPINStep.updateState(RecognizedState.PIN_suspended);
		    state = RecognizedState.PIN_suspended;
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		case PIN_resumed:
		    gPINStep.updateState(RecognizedState.PIN_blocked);
		    state = RecognizedState.PIN_blocked;
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		default:
		    // This should never happen
		    return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (APDUException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (IFDException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (ParserConfigurationException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (UnsupportedEncodingException ex) {
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	}
    }

    private StepActionResult performResumePIN(Map<String, ExecutionResults> oldResults) {
	try {
	    EstablishChannelResponse canResponse = performPACEWithCAN(oldResults);
	    if (canResponse == null) {
		gPINStep.setWrongCANFormat(true);
		gPINStep.setFailedCANVerify(false);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	    WSHelper.checkResult(canResponse);
	    gPINStep.updateState(RecognizedState.PIN_resumed);
	    state = RecognizedState.PIN_resumed;
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	} catch (DispatcherException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (InvocationTargetException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (WSHelper.WSException ex) {
	    // This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		logger.error("User canceled the authentication manually.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }

	    // for people which think they have to remove the card in the process
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
		logger.error("The SlotHandle was invalid so probably the user removed the card or an reset occurred.");
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)));
	    }
	    
	    gPINStep.setWrongCANFormat(false);
	    gPINStep.setFailedCANVerify(true);
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	} catch (ParserConfigurationException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}
    }

    private StepActionResult performUnblockPIN(Map<String, ExecutionResults> oldResults) {
	try {
	    EstablishChannelResponse pukResponse = performPACEWithPUK(oldResults);
	    if (pukResponse == null) {
		gPINStep.setWrongPUKFormat(true);
		gPINStep.setFailedPUKVerify(false);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	    WSHelper.checkResult(pukResponse);

	    // Here no exception is thrown so sent the ResetRetryCounter command
	    ResetRetryCounter resetRetryCounter = new ResetRetryCounter((byte) 0x03);
	    List<byte[]> responses = new ArrayList<>();
	    responses.add(new byte[] {(byte) 0x90, (byte) 0x00});
	    responses.add(new byte[] {(byte) 0x69, (byte) 0x84});

	    CardResponseAPDU resetCounterResponse = resetRetryCounter.transmit(dispatcher, slotHandle, responses);
	    if (Arrays.equals(resetCounterResponse.getTrailer(), new byte[] {(byte) 0x69, (byte) 0x84})) {
		gPINStep.updateState(RecognizedState.PUK_blocked);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } else if (Arrays.equals(resetCounterResponse.getTrailer(), new byte[] {(byte) 0x90, (byte) 0x00})) {
		gPINStep.updateState(RecognizedState.PIN_activated_RC3);
		return new StepActionResult(StepActionResultStatus.REPEAT, 
			generateSuccessStep(lang.translationForKey(PUK_SUCCESS)));
	    } else {
		gPINStep.updateState(RecognizedState.UNKNOWN);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	} catch (DispatcherException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (InvocationTargetException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (WSHelper.WSException ex) {
	    // This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		logger.error("User canceled the authentication manually.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }

	    // for people which think they have to remove the card in the process
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
		logger.error("The SlotHandle was invalid so probably the user removed the card or an reset occurred.");
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)));
	    }
	    
	    gPINStep.decreasePUKCounter();
	    gPINStep.setWrongPUKFormat(false);
	    gPINStep.setFailedPUKVerify(true);
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	} catch (APDUException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (ParserConfigurationException ex) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}

    }

    /**
     * Send a ModifyPIN-PCSC-Command to the Terminal.
     *
     * @throws IFDException If building the Command fails.
     * @throws InvocationTargetException If the ControlIFD command fails.
     * @throws DispatcherException If an error in the dispatcher occurs.
     */
    private ControlIFDResponse sendModifyPIN() throws IFDException, InvocationTargetException, DispatcherException {
	PasswordAttributesType pwdAttr = create(true, ASCII_NUMERIC, 6, 6, 6);
	pwdAttr.setPadChar(new byte[] { (byte) 0x3F });
	PCSCPinModify ctrlStruct = new PCSCPinModify(pwdAttr, StringUtils.toByteArray("002C0203"));
	byte[] structData = ctrlStruct.toBytes();

	ControlIFD controlIFD = new ControlIFD();
	controlIFD.setCommand(ByteUtils.concatenate((byte) PCSCFeatures.MODIFY_PIN_DIRECT, structData));
	controlIFD.setContextHandle(cHandle.getContextHandle());
	controlIFD.setIFDName(cHandle.getIFDName());
	return (ControlIFDResponse) dispatcher.deliver(controlIFD);
    }

    /**
     * Send a ResetRetryCounter-APDU.
     *
     * @throws APDUException if the RRC-APDU could not be sent successfully
     */
    private CardResponseAPDU sendResetRetryCounter(byte[] newPIN) throws APDUException {
	ResetRetryCounter apdu = new ResetRetryCounter(newPIN, (byte) 0x03);
	return apdu.transmit(dispatcher, cHandle.getSlotHandle());
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

    private Step generateSuccessStep(String successMessage) {
	Step successStep = new Step("Success");
	successStep.setReversible(false);
	Text successText = new Text(successMessage);
	successStep.getInputInfoUnits().add(successText);
	return successStep;
    }

    private Step generateErrorStep(String errorMessage) {
	Step errorStep = new Step("Error");
	errorStep.setReversible(false);
	Text errorText = new Text(errorMessage);
	errorStep.getInputInfoUnits().add(errorText);
	return errorStep;
    }

}
