/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.AltVUMessagesType;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.InputUnitType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.OutputInfoType;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PinInputType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse;
import java.math.BigInteger;
import java.util.List;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.common.CardCommandStatus;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.util.PINUtils;
import org.openecard.common.util.UtilException;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;
import org.openecard.ifd.scio.wrapper.ChannelManager;
import org.openecard.ifd.scio.wrapper.HandledChannel;
import org.openecard.ifd.scio.wrapper.TerminalInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
class AbstractTerminal {

    private static final Logger _logger = LoggerFactory.getLogger(AbstractTerminal.class);

    private final I18n lang = I18n.getTranslation("pace");

    private final IFD ifd;
    private final ChannelManager cm;
    private final HandledChannel channel;
    private final TerminalInfo terminalInfo;
    private final UserConsent gui;
    private final byte[] ctxHandle;
    private final BigInteger displayIdx;

    private IFDCapabilitiesType capabilities;
    private Boolean canBeep = null;
    private Boolean canBlink = null;
    private Boolean canDisplay = null;
    private Boolean canEnter = null;
    private BigInteger keyIdx = null;

    public AbstractTerminal(IFD ifd, ChannelManager cm, HandledChannel channel, UserConsent gui, byte[] ctxHandle, BigInteger displayIdx) {
	this.ifd = ifd;
	this.cm = cm;
	this.channel = channel;
	this.terminalInfo = new TerminalInfo(cm, channel);
	this.gui = gui;
	this.ctxHandle = ctxHandle;
	this.displayIdx = displayIdx;
    }

    public void output(String ifdName, OutputInfoType outInfo) throws IFDException {
	getCapabilities();

	// extract values from outInfo for convenience
	BigInteger didx = outInfo.getDisplayIndex();
	if (didx == null) {
	    didx = BigInteger.valueOf(0);
	}
	String msg = outInfo.getMessage();
	BigInteger timeout = outInfo.getTimeout();
	Boolean acoustic = outInfo.isAcousticalSignal();
	if (acoustic == null) {
	    acoustic = Boolean.FALSE;
	}
	Boolean optic = outInfo.isOpticalSignal();
	if (optic == null) {
	    optic = Boolean.FALSE;
	}

	if (acoustic.booleanValue()) {
	    if (canBeep() || isVirtual()) {
		beep();
	    } else {
		IFDException ex = new IFDException("No device to output a beep available.");
		_logger.warn(ex.getMessage(), ex);
		throw ex;
	    }
	}
	if (optic.booleanValue()) {
	    if (canBlink() || isVirtual()) {
		blink();
	    } else {
		IFDException ex = new IFDException("No device to output a blink available.");
		_logger.warn(ex.getMessage(), ex);
		throw ex;
	    }
	}
	if (msg != null) {
	    if (canDisplay() || isVirtual()) {
		display(msg, timeout);
	    } else {
		IFDException ex = new IFDException("No device to output a message available.");
		_logger.warn(ex.getMessage(), ex);
		throw ex;
	    }
	}
    }


    public VerifyUserResponse verifyUser(VerifyUser verify) throws SCIOException, IFDException {
	byte[] handle = verify.getSlotHandle();
	// get capabilities
	getCapabilities();

	// check if is possible to perform PinCompare protocol
	List<String> protoList = this.capabilities.getSlotCapability().get(0).getProtocol();
	if (! protoList.contains(ECardConstants.Protocol.PIN_COMPARE)) {
	    throw new IFDException("PinCompare protocol is not supported by this IFD.");
	}

	// get values from requested command
	InputUnitType inputUnit = verify.getInputUnit();
	AltVUMessagesType allMsgs = getMessagesOrDefaults(verify.getAltVUMessages());
	BigInteger firstTimeout = verify.getTimeoutUntilFirstKey();
	firstTimeout = (firstTimeout == null) ? BigInteger.valueOf(60000) : firstTimeout;
	BigInteger otherTimeout = verify.getTimeoutAfterFirstKey();
	otherTimeout = (otherTimeout == null) ? BigInteger.valueOf(15000) : otherTimeout;
	final byte[] template = verify.getTemplate();

	VerifyUserResponse response;
	Result result;
	// check which type of authentication to perform
	if (inputUnit.getBiometricInput() != null) {
	    // TODO: implement
	    String msg = "Biometric authentication not supported by IFD.";
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.IO.UNKNOWN_INPUT_UNIT, msg);
	    _logger.warn(ex.getMessage(), ex);
	    throw ex;
	} else if (inputUnit.getPinInput() != null) {
	    final PinInputType pinInput = inputUnit.getPinInput();

	    // we have a sophisticated card reader
	    if (terminalInfo.supportsPinCompare()) {
		// create custom pinAction to submit pin to terminal
		NativePinStepAction pinAction = new NativePinStepAction("enter-pin", pinInput, channel, terminalInfo, template);
		// display message instructing user what to do
		UserConsentDescription uc = pinUserConsent("step_pin_userconsent", pinAction);
		UserConsentNavigator ucr = gui.obtainNavigator(uc);
		ExecutionEngine exec = new ExecutionEngine(ucr);
		// run gui
		ResultStatus status = exec.process();
		if (status == ResultStatus.CANCEL) {
		    String msg = "PIN entry cancelled by user.";
		    _logger.warn(msg);
		    result = WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg);
		    response = WSHelper.makeResponse(VerifyUserResponse.class, result);
		} else if (pinAction.exception != null) {
		    _logger.warn(pinAction.exception.getMessage(), pinAction.exception);
		    result = WSHelper.makeResultError(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED,
			    pinAction.exception.getMessage());
		    response = WSHelper.makeResponse(VerifyUserResponse.class, result);
		} else {
		    // input by user
		    byte[] verifyResponse = pinAction.response;
		    // evaluate result
		    result = checkNativePinVerify(verifyResponse);
		    response = WSHelper.makeResponse(VerifyUserResponse.class, result);
		    response.setResponse(verifyResponse);
		}
		
		return response;

	    } else if (isVirtual()) { // software method
		// get pin, encode and send
		int minLength = pinInput.getPasswordAttributes().getMinLength().intValue();
		int maxLength = pinInput.getPasswordAttributes().getMaxLength().intValue();
		UserConsentDescription uc = pinUserConsent("step_pin_userconsent", minLength, maxLength);
		UserConsentNavigator ucr = gui.obtainNavigator(uc);
		ExecutionEngine exec = new ExecutionEngine(ucr);
		ResultStatus status = exec.process();
		if (status == ResultStatus.CANCEL) {
		    String msg = "PIN entry cancelled by user.";
		    _logger.warn(msg);
		    result = WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg);
		    response = WSHelper.makeResponse(VerifyUserResponse.class, result);
		    return response;
		}

		String rawPIN = getPinFromUserConsent(exec);
		PasswordAttributesType attributes = pinInput.getPasswordAttributes();
		Transmit verifyTransmit;

		try {
		    verifyTransmit = PINUtils.buildVerifyTransmit(rawPIN, attributes, template, handle);
		} catch (UtilException e) {
		    String msg = "Failed to create the verifyTransmit message.";
		    _logger.error(msg, e);
		    result = WSHelper.makeResultError(ECardConstants.Minor.IFD.UNKNOWN_ERROR, msg);
		    response = WSHelper.makeResponse(VerifyUserResponse.class, result);
		    return response;
		}

		// send to reader
		TransmitResponse transResp = ifd.transmit(verifyTransmit);

		// produce messages
		if (transResp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		    if (transResp.getOutputAPDU().isEmpty()) {
			result = WSHelper.makeResultError(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED, 
				transResp.getResult().getResultMessage().getValue());
			response = WSHelper.makeResponse(VerifyUserResponse.class, result);
			return response;
		    } else {
			response = WSHelper.makeResponse(VerifyUserResponse.class, transResp.getResult());
			response.setResponse(transResp.getOutputAPDU().get(0));
			return response;
		    }
		} else {
		    response = WSHelper.makeResponse(VerifyUserResponse.class, transResp.getResult());
		    response.setResponse(transResp.getOutputAPDU().get(0));
		    return response;
		}
	    } else {
		IFDException ex = new IFDException("No input unit available to perform PinCompare protocol.");
		_logger.warn(ex.getMessage(), ex);
		throw ex;
	    }
	} else {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.IO.UNKNOWN_INPUT_UNIT, "Unsupported authentication input method requested.");
	    _logger.warn(ex.getMessage(), ex);
	    throw ex;
	}
    }


    private static AltVUMessagesType getMessagesOrDefaults(AltVUMessagesType messages) {
	AltVUMessagesType allMsgs = new AltVUMessagesType();

	if (messages == null || messages.getAuthenticationRequestMessage() == null) {
	    allMsgs.setAuthenticationRequestMessage("Enter secret:");
	} else {
	    allMsgs.setAuthenticationRequestMessage(messages.getAuthenticationRequestMessage());
	}
	if (messages == null || messages.getSuccessMessage() == null) {
	    allMsgs.setSuccessMessage("Secret entered successfully.");
	} else {
	    allMsgs.setSuccessMessage(messages.getSuccessMessage());
	}
	if (messages == null || messages.getAuthenticationFailedMessage() == null) {
	    allMsgs.setAuthenticationFailedMessage("Secret not entered successfully.");
	} else {
	    allMsgs.setAuthenticationFailedMessage(messages.getAuthenticationFailedMessage());
	}
	if (messages == null || messages.getRequestConfirmationMessage() == null) {
	    allMsgs.setRequestConfirmationMessage("Enter secret again:");
	} else {
	    allMsgs.setRequestConfirmationMessage(messages.getRequestConfirmationMessage());
	}
	if (messages == null || messages.getCancelMessage() == null) {
	    allMsgs.setCancelMessage("Canceled secret input.");
	} else {
	    allMsgs.setCancelMessage(messages.getCancelMessage());
	}

	return allMsgs;
    }


    private void beep() {
	if (canBeep()) {
	    // TODO: implement
	}
    }

    private void blink() {
	if (canBlink()) {
	    // TODO: implement
	}
    }

    private void display(String msg, BigInteger timeout) {
	if (canDisplay()) {
	    // TODO: implement
	}
    }

    private boolean canBeep() {
	if (canBeep == null) {
	    canBeep = capabilities.isAcousticSignalUnit();
	}
	return canBeep.booleanValue();
    }

    private boolean canBlink() {
	if (canBlink == null) {
	    canBlink = capabilities.isOpticalSignalUnit();
	}
	return canBlink.booleanValue();
    }

    private boolean canDisplay() {
	if (canDisplay == null) {
	    canDisplay = Boolean.FALSE;
	    if (displayIdx == null && ! capabilities.getDisplayCapability().isEmpty()) {
		canDisplay = Boolean.TRUE;
	    } else {
		for (DisplayCapabilityType disp : capabilities.getDisplayCapability()) {
		    if (disp.getIndex().equals(displayIdx)) {
			canDisplay = Boolean.TRUE;
			break;
		    }
		}
	    }
	}
	return canDisplay.booleanValue();
    }

    private DisplayCapabilityType getDisplayCapabilities() {
	if (canDisplay) {
	    if (displayIdx == null) {
		DisplayCapabilityType disp = capabilities.getDisplayCapability().get(0);
		return disp;
	    } else {
		for (DisplayCapabilityType disp : capabilities.getDisplayCapability()) {
		    if (disp.getIndex().equals(displayIdx)) {
			return disp;
		    }
		}
		return null;
	    }
	} else {
	    return null;
	}
    }

    private boolean canEnter() {
	if (canEnter == null) {
	    canEnter = Boolean.FALSE;
	    if (keyIdx == null && ! capabilities.getKeyPadCapability().isEmpty()) {
		canEnter = Boolean.TRUE;
	    } else {
		for (KeyPadCapabilityType key : capabilities.getKeyPadCapability()) {
		    if (key.getIndex().equals(keyIdx)) {
			canEnter = Boolean.TRUE;
			break;
		    }
		}
	    }
	}
	return canEnter.booleanValue();
    }

    private KeyPadCapabilityType getKeypadCapabilities() {
	if (canEnter) {
	    if (keyIdx == null) {
		KeyPadCapabilityType key = capabilities.getKeyPadCapability().get(0);
		return key;
	    } else {
		for (KeyPadCapabilityType key : capabilities.getKeyPadCapability()) {
		    if (key.getIndex().equals(keyIdx)) {
			return key;
		    }
		}
		return null;
	    }
	} else {
	    return null;
	}
    }

    private boolean isVirtual() {
	return gui != null;
    }


    private static Result checkNativePinVerify(byte[] response) {
	byte sw1 = response[0];
	byte sw2 = response[1];
	if (sw1 == (byte)0x64) {
	    if (sw2 == (byte)0x00) {
		return WSHelper.makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Verify operation timed out.");
	    } else if (sw2 == (byte)0x01) {
		return WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "Verify operation was cancelled with the cancel button.");
	    } else if (sw2 == (byte)0x02) {
		return WSHelper.makeResultUnknownError("Modify PIN operation failed because two PINs were different.");
	    } else if (sw2 == (byte)0x03) {
		return WSHelper.makeResultUnknownError("PIN has wrong length.");
	    }
	} else if (sw1 == (byte)0x6b) {
	    if (sw2 == (byte)0x80) {
		return WSHelper.makeResultUnknownError("Invalid parameter passed to verify command.");
	    }
	} else if (sw1 == (byte)0x90) {
	    if (sw2 == (byte)0x00) {
		return WSHelper.makeResultOK();
	    }
	}
	return WSHelper.makeResultUnknownError(CardCommandStatus.getMessage(response));
    }

    private void getCapabilities() throws IFDException {
	GetIFDCapabilities capabilitiesReq = new GetIFDCapabilities();
	capabilitiesReq.setContextHandle(ctxHandle);
	capabilitiesReq.setIFDName(terminalInfo.getName());

	GetIFDCapabilitiesResponse cap = ifd.getIFDCapabilities(capabilitiesReq);
	Result r = cap.getResult();
	if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    IFDException ex = new IFDException(r);
	    _logger.warn(ex.getMessage(), ex);
	    throw ex;
	}
	this.capabilities = cap.getIFDCapabilities();
    }


    private UserConsentDescription pinUserConsent(String title, int minLength, int maxLength) {
	UserConsentDescription uc = new UserConsentDescription(lang.translationForKey(title));
	// create step
	Step s = new Step("enter-pin", lang.translationForKey("step_pace_title", "PIN"));
	uc.getSteps().add(s);
	// add text instructing user
	PasswordField i1 = new PasswordField("pin");
	s.getInputInfoUnits().add(i1);
	i1.setDescription("PIN:");
	i1.setMinLength(minLength);
	i1.setMaxLength(maxLength);

	return uc;
    }
    private UserConsentDescription pinUserConsent(String title, StepAction action) {
	UserConsentDescription uc = new UserConsentDescription(lang.translationForKey(title));
	// create step
	Step s = new Step("enter-pin", lang.translationForKey("step_pace_title", "PIN"));
	s.setAction(action);
	uc.getSteps().add(s);
	s.setInstantReturn(true);
	// add text instructing user
	Text i1 = new Text();
	s.getInputInfoUnits().add(i1);
	i1.setText(lang.translationForKey("step_pace_native_description", "PIN"));

	return uc;
    }

    private static String getPinFromUserConsent(ExecutionEngine response) {
	PasswordField p = (PasswordField) response.getResults().get("enter-pin").getResult("pin");
	return p.getValue();
    }

}
