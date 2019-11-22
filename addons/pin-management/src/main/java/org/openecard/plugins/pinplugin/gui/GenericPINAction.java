/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.ControlIFD;
import iso.std.iso_iec._24727.tech.schema.ControlIFDResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import static iso.std.iso_iec._24727.tech.schema.PasswordTypeType.ASCII_NUMERIC;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.DynamicContext;
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
import org.openecard.plugins.pinplugin.CardCapturer;
import org.openecard.plugins.pinplugin.CardStateView;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.CAN_CORRECT;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.DYNCTX_INSTANCE_KEY;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.PIN_CORRECT;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.PUK_CORRECT;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class GenericPINAction extends StepAction {

    private static final Logger LOG = LoggerFactory.getLogger(GenericPINAction.class);

    public static final String ERROR_STEP_ID = "error";
    private static final String PIN_ID_CAN = "2";
    private static final String PIN_ID_PIN = "3";
    private static final String PIN_ID_PUK = "4";
    private static final String ISO_8859_1 = "ISO-8859-1";

    // Translation constants
    private static final String PUK_SUCCESS = "action.unblockpin.userconsent.pukstep.puk_success";
    private static final String CHANGE_SUCCESS = "action.changepin.userconsent.successstep.description";
    private static final String ERROR_CARD_REMOVED = "action.error.card.removed";
    private static final String ERROR_INTERNAL = "action.error.internal";
    private static final String ERROR_NON_MATCHING_PASSWORDS = "action.error.missing_password_match";
    private static final String ERROR_TIMEOUT = "action.error.timeout";
    private static final String ERROR_TITLE = "action.error.title";
    private static final String ERROR_USER_CANCELLATION_OR_CARD_REMOVED = "action.error.user_cancellation";
    private static final String SUCCESS_TITLE = "action.success.title";
    private static final String ERROR_UNKNOWN = "action.error.unknown";

    private final I18n lang = I18n.getTranslation("pinplugin");
    private final Dispatcher dispatcher;
    private final GenericPINStep gPINStep;

    private final CardStateView cardView;
    private final CardCapturer cardCapturer;


    public GenericPINAction(String stepID, Dispatcher dispatcher, GenericPINStep gPINStep, CardCapturer cardCapturer) {
	super(gPINStep);
	this.gPINStep = gPINStep;
	this.dispatcher = dispatcher;
	this.cardView = cardCapturer.aquireView();
	this.cardCapturer = cardCapturer;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isCancelled()) {
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}

	try {
	    cardCapturer.updateCardState();
	} catch (WSHelper.WSException ex) {
	    LOG.error("Failed to prepare Generic PIN step.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}

	// clean up values
	clearCorrectValues();

	RecognizedState state = cardView.getPinState();
	switch (state) {
	    case PIN_activated_RC3:
	    case PIN_activated_RC2:
		return performPINChange(oldResults);
	    case PIN_suspended:
		return performResumePIN(oldResults);
	    case PIN_resumed:
		return performPINChange(oldResults);
	    case PIN_blocked:
		return performUnblockPIN(oldResults);
	    case PIN_deactivated:
	    case PUK_blocked:
	    case UNKNOWN:
		// nothing todo here the error message was displayed so just return next.
		return new StepActionResult(StepActionResultStatus.NEXT);
	}

	return null;
    }

    private EstablishChannelResponse performPACEWithPIN(Map<String, ExecutionResults> oldResults)
	    throws ParserConfigurationException {
	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp = new AuthDataMap(paceInput);

	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (this.cardView.capturePin()) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField oldPINField = (PasswordField) executionResults.getResult(GenericPINStep.OLD_PIN_FIELD);
	    char[] oldPINValue = oldPINField.getValue();

	    if (oldPINValue.length > 6 && oldPINValue.length < 5) {
		// let the user enter the can again, when input verification failed
		return null;
	    } else {
		paceInputMap.addElement(PACEInputType.PIN, new String(oldPINValue));
	    }
	}
	paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PIN);

	// perform PACE by EstablishChannelCommand
	EstablishChannel eChannel = createEstablishChannelStructure(paceInputMap);
	return (EstablishChannelResponse) dispatcher.safeDeliver(eChannel);
    }

    private EstablishChannelResponse performPACEWithCAN(Map<String, ExecutionResults> oldResults)
	    throws ParserConfigurationException {
	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp = new AuthDataMap(paceInput);

	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (this.cardView.capturePin()) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField canField = (PasswordField) executionResults.getResult(GenericPINStep.CAN_FIELD);
	    String canValue = new String(canField.getValue());

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
	return (EstablishChannelResponse) dispatcher.safeDeliver(eChannel);
    }

    private EstablishChannelResponse performPACEWithPUK(Map<String, ExecutionResults> oldResults)
	    throws ParserConfigurationException {
	DIDAuthenticationDataType paceInput = new DIDAuthenticationDataType();
	paceInput.setProtocol(ECardConstants.Protocol.PACE);
	AuthDataMap tmp = new AuthDataMap(paceInput);

	AuthDataResponse paceInputMap = tmp.createResponse(paceInput);
	if (this.cardView.capturePin()) {
	    ExecutionResults executionResults = oldResults.get(getStepID());
	    PasswordField pukField = (PasswordField) executionResults.getResult(GenericPINStep.PUK_FIELD);
	    String pukValue = new String(pukField.getValue());

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
	return (EstablishChannelResponse) dispatcher.safeDeliver(eChannel);
    }

    private EstablishChannel createEstablishChannelStructure(AuthDataResponse paceInputMap) {
	// EstablishChannel
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(this.cardView.getHandle().getSlotHandle());
	establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);
	return establishChannel;
    }

    private StepActionResult performPINChange(Map<String, ExecutionResults> oldResults) {
	String newPINValue = null;
	String newPINRepeatValue = null;
 	if (this.cardView.capturePin()) {
	    try {
		ExecutionResults executionResults = oldResults.get(getStepID());
		PasswordField newPINField = (PasswordField) executionResults.getResult(GenericPINStep.NEW_PIN_FIELD);
		newPINValue = new String(newPINField.getValue());

		PasswordField newPINRepeatField = (PasswordField) executionResults.getResult(GenericPINStep.NEW_PIN_REPEAT_FIELD);
		newPINRepeatValue = new String(newPINRepeatField.getValue());

		byte[] pin1 = newPINValue.getBytes(ISO_8859_1);
		byte[] pin2 = newPINRepeatValue.getBytes(ISO_8859_1);

		if (! ByteUtils.compare(pin1, pin2)) {
		    LOG.warn("New PIN does not match the value from the confirmation field.");
		    gPINStep.updateState(this.cardView.getPinState()); // to reset the text fields
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		}

	    } catch (UnsupportedEncodingException ex) {
		LOG.error("ISO_8859_1 charset is not support.", ex);
		gPINStep.updateState(this.cardView.getPinState()); // to reset the text fields
		gPINStep.setFailedPINVerify(true, false);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	}

	try {
	    EstablishChannelResponse pinResponse = performPACEWithPIN(oldResults);
	    if (pinResponse == null) {
		// the entered pin has a wrong format repeat the entering of the data
		gPINStep.setFailedPINVerify(true, false);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }

	    if (pinResponse.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		switch (pinResponse.getResult().getResultMinor()) {
		    case ECardConstants.Minor.IFD.PASSWORD_ERROR:
			gPINStep.setFailedPINVerify(false, true);
			gPINStep.updateState(RecognizedState.PIN_activated_RC2);
			return new StepActionResult(StepActionResultStatus.REPEAT);
		    case ECardConstants.Minor.IFD.PASSWORD_SUSPENDED:
			gPINStep.setFailedPINVerify(false, true);
			gPINStep.updateState(RecognizedState.PIN_suspended);
			return new StepActionResult(StepActionResultStatus.REPEAT);
		    case ECardConstants.Minor.IFD.PASSWORD_BLOCKED:
			gPINStep.setFailedPINVerify(false, true);
			gPINStep.updateState(RecognizedState.PIN_blocked);
			return new StepActionResult(StepActionResultStatus.REPEAT);
		    default:
			WSHelper.checkResult(pinResponse);
			break;
		}
	    }

	    if (this.cardView.capturePin()) {
		// pace with the old pin was successful now modify the pin
		if (newPINValue.equals(newPINRepeatValue) && newPINValue.length() == 6) {
		    // no result check necessary everything except a 9000 leads to an APDU exception
		    sendResetRetryCounter(newPINValue.getBytes(ISO_8859_1));
		}
	    } else {
		ControlIFDResponse resp = sendModifyPIN();
		evaluateControlIFDResponse(resp);
	    }

	    // PIN modified successfully, proceed with next step
	    gPINStep.setFailedPINVerify(false, false);
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    generateSuccessStep(lang.translationForKey(CHANGE_SUCCESS)));
	} catch (APDUException | IFDException | ParserConfigurationException ex) {
	    LOG.error("An internal error occurred while trying to change the PIN", ex);
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    generateErrorStep(lang.translationForKey(ERROR_INTERNAL)));
	} catch (UnsupportedEncodingException ex) {
	    LOG.warn("The encoding of the PIN is wrong.", ex);
	    gPINStep.setFailedPINVerify(true, false);
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	} catch (WSHelper.WSException ex) {
	    // This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		LOG.error("User canceled the authentication manually or removed the card.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_USER_CANCELLATION_OR_CARD_REMOVED)));
	    }

	    // for people which think they have to remove the card in the process
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
		LOG.error("The SlotHandle was invalid so probably the user removed the card or an reset occurred.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)));
	    }

	    // for users which forgot to type in something
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.TIMEOUT_ERROR)) {
		LOG.error("The terminal timed out no password was entered.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_TIMEOUT)));
	    }

	    // the verification of the new pin failed
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.PASSWORDS_DONT_MATCH)) {
		LOG.error("The verification of the new PIN failed.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_NON_MATCHING_PASSWORDS)));
	    }

	    // We don't know what happend so just show an general error message
	    LOG.error("An unknown error occurred while trying to change the PIN.", ex);
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    generateErrorStep(lang.translationForKey(ERROR_UNKNOWN)));
	} finally {
	}
    }

    private StepActionResult performResumePIN(Map<String, ExecutionResults> oldResults) {
	try {
	    EstablishChannelResponse canResponse = performPACEWithCAN(oldResults);

	    if (canResponse == null) {
		gPINStep.setFailedCANVerify(true, false);
		gPINStep.updateState(this.cardView.getPinState()); // to reset the text fields
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }

	    if (canResponse.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		if (canResponse.getResult().getResultMinor().equals(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED)) {
		    gPINStep.setFailedCANVerify(false, true);
		    gPINStep.updateState(this.cardView.getPinState()); // to reset the text fields
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		} else {
		    WSHelper.checkResult(canResponse);
		}
	    }

	    gPINStep.setFailedCANVerify(false, false);
	    gPINStep.updateState(RecognizedState.PIN_resumed);
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	} catch (ParserConfigurationException ex) {
	    LOG.error("An internal error occurred while trying to resume the PIN.", ex);
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    generateErrorStep(lang.translationForKey(ERROR_INTERNAL)));
	} catch (WSHelper.WSException ex) {
	    // This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		LOG.error("User canceled the authentication manually or removed the card.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_USER_CANCELLATION_OR_CARD_REMOVED)));
	    }

	    // for people which think they have to remove the card in the process
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
		LOG.error("The SlotHandle was invalid so probably the user removed the card or an reset occurred.");
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)));
	    }

            // for users which forgot to type in something
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.TIMEOUT_ERROR)) {
		LOG.error("The terminal timed out no password was entered.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_TIMEOUT)));
	    }

	    LOG.error("An unknown error occurred while trying to verify the CAN.", ex);
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    generateErrorStep(lang.translationForKey(ERROR_UNKNOWN)));

	}
    }

    private StepActionResult performUnblockPIN(Map<String, ExecutionResults> oldResults) {
	try {
	    EstablishChannelResponse pukResponse = performPACEWithPUK(oldResults);

	    if (pukResponse == null) {
		gPINStep.setFailedPUKVerify(true, false);
		gPINStep.updateState(this.cardView.getPinState()); // to reset the text fields
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }

	    if (pukResponse.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		if (pukResponse.getResult().getResultMinor().equals(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED)) {
		    // i think we should not display the counter
		    //gPINStep.decreasePUKCounter();
		    gPINStep.setFailedPUKVerify(false, true);
		    gPINStep.updateState(this.cardView.getPinState()); // to reset the text fields
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		} else {
		     WSHelper.checkResult(pukResponse);
		}
	    }

	    // Here no exception is thrown so sent the ResetRetryCounter command
	    ResetRetryCounter resetRetryCounter = new ResetRetryCounter((byte) 0x03);
	    List<byte[]> responses = new ArrayList<>();
	    responses.add(new byte[] {(byte) 0x90, (byte) 0x00});
	    responses.add(new byte[] {(byte) 0x69, (byte) 0x84});

	    CardResponseAPDU resetCounterResponse = resetRetryCounter.transmit(
		    dispatcher,
		    this.cardView.getHandle().getSlotHandle(),
		    responses);
	    if (Arrays.equals(resetCounterResponse.getTrailer(), new byte[] {(byte) 0x69, (byte) 0x84})) {
		gPINStep.updateState(RecognizedState.PUK_blocked);
		gPINStep.setFailedPUKVerify(false, true);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } else if (Arrays.equals(resetCounterResponse.getTrailer(), new byte[] {(byte) 0x90, (byte) 0x00})) {
		gPINStep.updateState(RecognizedState.PIN_activated_RC3);
		gPINStep.setFailedPUKVerify(false, false);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateSuccessStep(lang.translationForKey(PUK_SUCCESS)));
	    } else {
		gPINStep.updateState(RecognizedState.UNKNOWN);
		gPINStep.setFailedPUKVerify(false, true);
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	} catch (APDUException | ParserConfigurationException ex) {
	    LOG.error("An internal error occurred while trying to unblock the PIN.", ex);
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    generateErrorStep(lang.translationForKey(ERROR_INTERNAL)));
	} catch (WSHelper.WSException ex) {
	    // This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		LOG.error("User canceled the authentication manually or removed the card.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_USER_CANCELLATION_OR_CARD_REMOVED)));
	    }

            // for users which forgot to type in something
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.TIMEOUT_ERROR)) {
		LOG.error("The terminal timed out no password was entered.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_TIMEOUT)));
	    }

	    // for people which think they have to remove the card in the process
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
		LOG.error("The SlotHandle was invalid so probably the user removed the card or an reset occurred.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)));
	    }

	     // We don't know what happend so just show an general error message
	    LOG.error("An unknown error occurred while trying to verify the PUK.", ex);
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    generateErrorStep(lang.translationForKey(ERROR_UNKNOWN)));
	} finally {
	}

    }

    /**
     * Send a ModifyPIN-PCSC-Command to the Terminal.
     *
     * @throws IFDException If building the Command fails.
     */
    private ControlIFDResponse sendModifyPIN() throws IFDException {
	PasswordAttributesType pwdAttr = create(true, ASCII_NUMERIC, 6, 6, 6);
	pwdAttr.setPadChar(new byte[] { (byte) 0x3F });
	PCSCPinModify ctrlStruct = new PCSCPinModify(pwdAttr, StringUtils.toByteArray("002C0203"));
	byte[] structData = ctrlStruct.toBytes();

	ControlIFD controlIFD = new ControlIFD();
	controlIFD.setCommand(ByteUtils.concatenate((byte) PCSCFeatures.MODIFY_PIN_DIRECT, structData));
	controlIFD.setSlotHandle(this.cardView.getHandle().getSlotHandle());
	return (ControlIFDResponse) dispatcher.safeDeliver(controlIFD);
    }

    /**
     * Send a ResetRetryCounter-APDU.
     *
     * @throws APDUException if the RRC-APDU could not be sent successfully
     */
    private CardResponseAPDU sendResetRetryCounter(byte[] newPIN) throws APDUException {
	ResetRetryCounter apdu = new ResetRetryCounter(newPIN, (byte) 0x03);
	return apdu.transmit(dispatcher, this.cardView.getHandle().getSlotHandle());
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
	Step successStep = new Step("success", lang.translationForKey(SUCCESS_TITLE));
	successStep.setReversible(false);
	Text successText = new Text(successMessage);
	successStep.getInputInfoUnits().add(successText);
	return successStep;
    }

    private Step generateErrorStep(String errorMessage) {
	Step errorStep = new Step(ERROR_STEP_ID, lang.translationForKey(ERROR_TITLE));
	errorStep.setReversible(false);
	Text errorText = new Text(errorMessage);
	errorStep.getInputInfoUnits().add(errorText);
	return errorStep;
    }

    private void evaluateControlIFDResponse(ControlIFDResponse response) throws WSHelper.WSException {
	byte[] resp = response.getResponse();
	switch(ByteUtils.toInteger(resp)) {
	    case 0x64A1:
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE,
			"Card was removed."));
		break;
	    case 0x6402:
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORDS_DONT_MATCH,
			"The entered passwords do not match."));
		break;
	    case 0x6401:
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
			"The user aborted the password entry."));
		break;
	}

	WSHelper.checkResult(response);
    }

    private void clearCorrectValues() {
	DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);
	ctx.remove(PIN_CORRECT);
	ctx.remove(CAN_CORRECT);
	ctx.remove(PUK_CORRECT);
    }



}
