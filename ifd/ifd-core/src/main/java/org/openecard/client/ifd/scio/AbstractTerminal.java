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

package org.openecard.client.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.util.Map;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.util.CardCommandStatus;
import org.openecard.client.common.util.PINUtils;
import org.openecard.client.common.util.UtilException;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.*;
import org.openecard.client.ifd.scio.reader.PCSCFeatures;
import org.openecard.client.ifd.scio.reader.PCSCPinVerify;
import org.openecard.client.ifd.scio.wrapper.SCTerminal;
import org.openecard.client.ifd.scio.wrapper.SCWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
class AbstractTerminal {

    private static final Logger _logger = LoggerFactory.getLogger(AbstractTerminal.class);

    private final IFD ifd;
    private final SCWrapper scwrapper;
    private final UserConsent gui;
    private final byte[] ctxHandle;
    private final BigInteger displayIdx;

    private IFDCapabilitiesType capabilities;
    private Boolean canBeep = null;
    private Boolean canBlink = null;
    private Boolean canDisplay = null;
    private Boolean canEnter = null;
    private BigInteger keyIdx = null;

    public AbstractTerminal(IFD ifd, SCWrapper scwrapper, UserConsent gui, byte[] ctxHandle, BigInteger displayIdx) {
	this.ifd = ifd;
	this.scwrapper = scwrapper;
	this.gui = gui;
	this.ctxHandle = ctxHandle;
	this.displayIdx = displayIdx;
    }

    public void output(String ifdName, OutputInfoType outInfo) throws IFDException {
	getCapabilities(ifdName);

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


    public VerifyUserResponse verifyUser(VerifyUser verify) throws IFDException {
	byte[] handle = verify.getSlotHandle();
	// get capabilities
	final SCTerminal term = scwrapper.getTerminal(handle);
	getCapabilities(term.getName());

	// check if is possible to perform PinCompare protocol
	if (! this.capabilities.getSlotCapability().get(0).getProtocol().contains(ECardConstants.Protocol.PIN_COMPARE)) {
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

	// check which type of authentication to perform
	if (inputUnit.getBiometricInput() != null) {
	    // TODO: implement
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.UNKNOWN_INPUT_UNIT, "Biometric authentication not supported by IFD.");
	    _logger.warn(ex.getMessage(), ex);
	    throw ex;
	} else if (inputUnit.getPinInput() != null) {
	    final PinInputType pinInput = inputUnit.getPinInput();

	    // we have a sophisticated card reader
	    if (canNativePinVerify(handle)) {
		// display message instructing user what to do
		UserConsentNavigator ucr = gui.obtainNavigator(pinUserConsent(allMsgs.getAuthenticationRequestMessage()));
		ExecutionEngine exec = new ExecutionEngine(ucr);
		// add custom pinAction to submit pin to terminal
		NativePinStepAction pinAction = new NativePinStepAction("enter-pin", pinInput, term, template);
		exec.addCustomAction(pinAction);
		// run gui
		ResultStatus status = exec.process();
		if (status == ResultStatus.CANCEL) {
		    IFDException ex = new IFDException(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "PIN entry cancelled by user.");
		    _logger.warn(ex.getMessage(), ex);
		    throw ex;
		} else if (pinAction.exception != null) {
		    _logger.warn(pinAction.exception.getMessage(), pinAction.exception);
		    throw pinAction.exception;
		}
		// input by user
		byte[] verifyResponse = pinAction.response;
		// evaluate result
		Result result = checkNativePinVerify(verifyResponse);
		VerifyUserResponse response = WSHelper.makeResponse(VerifyUserResponse.class, result);
		response.setResponse(verifyResponse);
		return response;

	    } else if (isVirtual()) { // software method
		// get pin, encode and send
		UserConsentNavigator ucr = gui.obtainNavigator(pinUserConsent(allMsgs.getAuthenticationRequestMessage(), pinInput.getPasswordAttributes().getMinLength().intValue(), pinInput.getPasswordAttributes().getMaxLength().intValue()));
		ExecutionEngine exec = new ExecutionEngine(ucr);
		ResultStatus status = exec.process();
		if (status == ResultStatus.CANCEL) {
		    IFDException ex = new IFDException(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "PIN entry cancelled by user.");
		    _logger.warn(ex.getMessage(), ex);
		    throw ex;
		}

		String rawPIN = getPinFromUserConsent(exec);
		PasswordAttributesType attributes = pinInput.getPasswordAttributes();
		Transmit verifyTransmit;

		try {
		    verifyTransmit = PINUtils.buildVerifyTransmit(rawPIN, attributes, template, handle);
		} catch (UtilException e) {
		    IFDException ex = new IFDException(e);
		    throw ex;
		}

		// send to reader
		TransmitResponse transResp = ifd.transmit(verifyTransmit);

		// produce messages
		if (transResp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		    if (transResp.getOutputAPDU().isEmpty()) {
			IFDException ex = new IFDException(transResp.getResult());
			_logger.warn(ex.getMessage(), ex);
			throw ex;
		    } else {
			VerifyUserResponse response = WSHelper.makeResponse(VerifyUserResponse.class, transResp.getResult());
			response.setResponse(transResp.getOutputAPDU().get(0));
			return response;
		    }
		} else {
		    VerifyUserResponse response = WSHelper.makeResponse(VerifyUserResponse.class, transResp.getResult());
		    response.setResponse(transResp.getOutputAPDU().get(0));
		    return response;
		}
	    } else {
		IFDException ex = new IFDException("No input unit available to perform PinCompare protocol.");
		_logger.warn(ex.getMessage(), ex);
		throw ex;
	    }
	} else {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.UNKNOWN_INPUT_UNIT, "Unsupported authentication input method requested.");
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

    private boolean canNativePinVerify(byte[] slotHandle) {
	try {
	    SCTerminal term = this.scwrapper.getTerminal(slotHandle);
	    return term.supportsPinCompare();
	} catch (IFDException ex) {
	    return false;
	}
    }

    private byte[] nativePinVerify(PinInputType pinInput, SCTerminal term, byte[] template) throws IFDException {
	// get data for verify command and perform it
	PCSCPinVerify verifyStruct = new PCSCPinVerify(pinInput.getPasswordAttributes(), template);
	byte[] verifyStructData = verifyStruct.toBytes();
	byte[] result = term.executeCtrlCode(PCSCFeatures.VERIFY_PIN_DIRECT, verifyStructData);
	return result;
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

    private void getCapabilities(String ifdName) throws IFDException {
	GetIFDCapabilities capabilitiesReq = new GetIFDCapabilities();
	capabilitiesReq.setContextHandle(ctxHandle);
	capabilitiesReq.setIFDName(ifdName);

	GetIFDCapabilitiesResponse cap = ifd.getIFDCapabilities(capabilitiesReq);
	Result r = cap.getResult();
	if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    IFDException ex = new IFDException(r);
	    _logger.warn(ex.getMessage(), ex);
	    throw ex;
	}
	this.capabilities = cap.getIFDCapabilities();
    }


    private static UserConsentDescription pinUserConsent(String title, int minLength, int maxLength) {
	UserConsentDescription uc = new UserConsentDescription(title);
	// create step
	Step s = new Step("enter-pin", "Enter PIN");
	uc.getSteps().add(s);
	// add text instructing user
	PasswordField i1 = new PasswordField("pin");
	s.getInputInfoUnits().add(i1);
	i1.setDescription("PIN:");
	i1.setMinLength(minLength);
	i1.setMaxLength(maxLength);

	return uc;
    }
    private static UserConsentDescription pinUserConsent(String title) {
	UserConsentDescription uc = new UserConsentDescription(title);
	// create step
	Step s = new Step("enter-pin", "Enter PIN");
	uc.getSteps().add(s);
	s.setInstantReturn(true);
	// add text instructing user
	Text i1 = new Text();
	s.getInputInfoUnits().add(i1);
	i1.setText("Enter your secret in the connected chip card terminal.");

	return uc;
    }

    private static String getPinFromUserConsent(ExecutionEngine response) {
	PasswordField p = (PasswordField) response.getResults().get("enter-pin").getResult("pin");
	return p.getValue();
    }


    /**
     * Action to perform a native pin verify in the GUI executor.
     */
    private class NativePinStepAction extends StepAction {
	public IFDException exception = null;
	public byte[] response = null;
	private final PinInputType pinInput;
	private final SCTerminal term;
	private final byte[] template;

	public NativePinStepAction(String stepName, PinInputType pinInput, SCTerminal term, byte[] template) {
	    super(stepName);
	    this.pinInput = pinInput;
	    this.term = term;
	    this.template = template;
	}

	@Override
	public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	    try {
		response = nativePinVerify(pinInput, term, template);
	    } catch (IFDException ex) {
		exception = ex;
	    }
	    return new StepActionResult(StepActionResultStatus.NEXT);
	}

    }

}
