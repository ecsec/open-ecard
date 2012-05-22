/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommandStatus;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.*;
import org.openecard.client.gui.executor.*;
import org.openecard.client.ifd.scio.reader.PCSCFeatures;
import org.openecard.client.ifd.scio.reader.PCSCPinVerify;
import org.openecard.client.ifd.scio.wrapper.SCTerminal;
import org.openecard.client.ifd.scio.wrapper.SCWrapper;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
class AbstractTerminal {

    private static final Logger _logger = LogManager.getLogger(AbstractTerminal.class.getName());

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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "AbstractTerminal(IFD ifd, SCWrapper scwrapper, UserConsent gui, byte[] ctxHandle, BigInteger displayIdx)", new Object[]{ifd, scwrapper, gui, ctxHandle, displayIdx});
	} // </editor-fold>
	this.ifd = ifd;
	this.scwrapper = scwrapper;
	this.gui = gui;
	this.ctxHandle = ctxHandle;
	this.displayIdx = displayIdx;
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "AbstractTerminal(IFD ifd, SCWrapper scwrapper, UserConsent gui, byte[] ctxHandle, BigInteger displayIdx)");
	} // </editor-fold>
    }

    public void output(String ifdName, OutputInfoType outInfo) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "output(String ifdName, OutputInfoType outInfo)", new Object[]{ifdName, outInfo});
	} // </editor-fold>

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
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "output(String ifdName, OutputInfoType outInfo)", ex.getMessage(), ex);
		} // </editor-fold>
		throw ex;
	    }
	}
	if (optic.booleanValue()) {
	    if (canBlink() || isVirtual()) {
		blink();
	    } else {
		IFDException ex = new IFDException("No device to output a blink available.");
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "output(String ifdName, OutputInfoType outInfo)", ex.getMessage(), ex);
		} // </editor-fold>
		throw ex;
	    }
	}
	if (msg != null) {
	    if (canDisplay() || isVirtual()) {
		display(msg, timeout);
	    } else {
		IFDException ex = new IFDException("No device to output a message available.");
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "output(String ifdName, OutputInfoType outInfo)", ex.getMessage(), ex);
		} // </editor-fold>
		throw ex;
	    }
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "output(String ifdName, OutputInfoType outInfo)");
	} // </editor-fold>
    }


    public VerifyUserResponse verifyUser(VerifyUser verify) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "verifyUser(VerifyUser verify)", verify);
	} // </editor-fold>
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
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser verify)", ex.getMessage(), ex);
	    } // </editor-fold>
	    throw ex;
	} else if (inputUnit.getPinInput() != null) {
	    final PinInputType pinInput = inputUnit.getPinInput();

	    // we have a sophisticated card reader
	    if (canNativePinVerify(handle)) {
		// display message instructing user what to do
		UserConsentNavigator ucr = gui.obtainNavigator(pinUserConsent(allMsgs.getAuthenticationRequestMessage()));
		ExecutionEngine exec = new ExecutionEngine(ucr);
		// add custom pinAction to submit pin to terminal
		NativePinStepAction pinAction = new NativePinStepAction("Enter PIN", pinInput, term, template);
		exec.addCustomAction(pinAction);
		// run gui
		ResultStatus status = exec.process();
		if (status == ResultStatus.CANCEL) {
		    IFDException ex = new IFDException(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "PIN entry cancelled by user.");
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser verify)", ex.getMessage(), ex);
		    } // </editor-fold>
		    throw ex;
		} else if (pinAction.exception != null) {
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser verify)", pinAction.exception.getMessage(), pinAction.exception);
		    } // </editor-fold>
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
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser verify)", ex.getMessage(), ex);
		    } // </editor-fold>
		    throw ex;
		}
		byte[] pin = IFDUtils.encodePin(getPinFromUserConsent(exec), pinInput.getPasswordAttributes());

		// send to reader
		byte[] pinCmd = ByteUtils.concatenate(template, (byte)pin.length);
		pinCmd = ByteUtils.concatenate(pinCmd, pin);
		Transmit transmit = new Transmit();
		transmit.setSlotHandle(handle);
		InputAPDUInfoType pinApdu = new InputAPDUInfoType();
		pinApdu.setInputAPDU(pinCmd);
		transmit.getInputAPDUInfo().add(pinApdu);
		TransmitResponse transResp = ifd.transmit(transmit);

		// produce messages
		if (transResp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		    if (transResp.getOutputAPDU().isEmpty()) {
			IFDException ex = new IFDException(transResp.getResult());
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.WARNING)) {
			    _logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser verify)", ex.getMessage(), ex);
			} // </editor-fold>
			throw ex;
		    } else {
			VerifyUserResponse response = WSHelper.makeResponse(VerifyUserResponse.class, transResp.getResult());
			response.setResponse(transResp.getOutputAPDU().get(0));
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.FINER)) {
			    _logger.exiting(this.getClass().getName(), "verifyUser(VerifyUser verify)", response);
			} // </editor-fold>
			return response;
		    }
		} else {
		    VerifyUserResponse response = WSHelper.makeResponse(VerifyUserResponse.class, transResp.getResult());
		    response.setResponse(transResp.getOutputAPDU().get(0));
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "verifyUser(VerifyUser verify)", response);
		    } // </editor-fold>
		    return response;
		}
	    } else {
		IFDException ex = new IFDException("No input unit available to perform PinCompare protocol.");
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser verify)", ex.getMessage(), ex);
		} // </editor-fold>
		throw ex;
	    }
	} else {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.UNKNOWN_INPUT_UNIT, "Unsupported authentication input method requested.");
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser verify)", ex.getMessage(), ex);
	    } // </editor-fold>
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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "beep()");
	} // </editor-fold>
	if (canBeep()) {
	    // TODO: implement
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "beep()");
	} // </editor-fold>
    }

    private void blink() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "blink()");
	} // </editor-fold>
	if (canBlink()) {
	    // TODO: implement
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "blink()");
	} // </editor-fold>
    }

    private void display(String msg, BigInteger timeout) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "display(String msg, BigInteger timeout)", new Object[]{msg, timeout});
	} // </editor-fold>
	if (canDisplay()) {
	    // TODO: implement
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "display(String msg, BigInteger timeout)");
	} // </editor-fold>
    }

    private boolean canBeep() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "canBeep()");
	} // </editor-fold>
	if (canBeep == null) {
	    canBeep = capabilities.isAcousticSignalUnit();
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "canBeep()", canBeep);
	} // </editor-fold>
	return canBeep.booleanValue();
    }

    private boolean canBlink() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "canBlink()");
	} // </editor-fold>
	if (canBlink == null) {
	    canBlink = capabilities.isOpticalSignalUnit();
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "canBlink()", canBlink);
	} // </editor-fold>
	return canBlink.booleanValue();
    }

    private boolean canDisplay() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "canDisplay()");
	} // </editor-fold>
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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "canDisplay()", canDisplay);
	} // </editor-fold>
	return canDisplay.booleanValue();
    }

    private DisplayCapabilityType getDisplayCapabilities() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getDisplayCapabilities()");
	} // </editor-fold>
	if (canDisplay) {
	    if (displayIdx == null) {
		DisplayCapabilityType disp = capabilities.getDisplayCapability().get(0);
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "getDisplayCapabilities()", disp);
		} // </editor-fold>
		return disp;
	    } else {
		for (DisplayCapabilityType disp : capabilities.getDisplayCapability()) {
		    if (disp.getIndex().equals(displayIdx)) {
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.FINER)) {
			    _logger.exiting(this.getClass().getName(), "getDisplayCapabilities()", disp);
			} // </editor-fold>
			return disp;
		    }
		}
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "getDisplayCapabilities()", null);
		} // </editor-fold>
		return null;
	    }
	} else {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "getDisplayCapabilities()", null);
	    } // </editor-fold>
	    return null;
	}
    }

    private boolean canEnter() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "canEnter()");
	} // </editor-fold>
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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "canEnter()", canEnter);
	} // </editor-fold>
	return canEnter.booleanValue();
    }

    private KeyPadCapabilityType getKeypadCapabilities() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getKeypadCapabilities()");
	} // </editor-fold>
	if (canEnter) {
	    if (keyIdx == null) {
		KeyPadCapabilityType key = capabilities.getKeyPadCapability().get(0);
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "getKeypadCapabilities()", key);
		} // </editor-fold>
		return key;
	    } else {
		for (KeyPadCapabilityType key : capabilities.getKeyPadCapability()) {
		    if (key.getIndex().equals(keyIdx)) {
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.FINER)) {
			    _logger.exiting(this.getClass().getName(), "getKeypadCapabilities()", key);
			} // </editor-fold>
			return key;
		    }
		}
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "getKeypadCapabilities()", null);
		} // </editor-fold>
		return null;
	    }
	} else {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "getKeypadCapabilities()", null);
	    } // </editor-fold>
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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getCapabilities(String ifdName)", ifdName);
	} // </editor-fold>
	GetIFDCapabilities capabilitiesReq = new GetIFDCapabilities();
	capabilitiesReq.setContextHandle(ctxHandle);
	capabilitiesReq.setIFDName(ifdName);

	GetIFDCapabilitiesResponse cap = ifd.getIFDCapabilities(capabilitiesReq);
	Result r = cap.getResult();
	if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    IFDException ex = new IFDException(r);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "getCapabilities(String ifdName)", ex.getMessage(), ex);
	    } // </editor-fold>
	    throw ex;
	}
	this.capabilities = cap.getIFDCapabilities();
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "getCapabilities(String ifdName)");
	} // </editor-fold>
    }


    private static UserConsentDescription pinUserConsent(String title, int minLength, int maxLength) {
	UserConsentDescription uc = new UserConsentDescription(title);
	// create step
	Step s = new Step("Enter PIN");
	uc.getSteps().add(s);
	// add text instructing user
	Passwordfield i1 = new Passwordfield();
	s.getInputInfoUnits().add(i1);
	i1.setID("pin");
	i1.setID("PIN:");
	i1.setMinLength(minLength);
	i1.setMaxLength(maxLength);

	return uc;
    }
    private static UserConsentDescription pinUserConsent(String title) {
	UserConsentDescription uc = new UserConsentDescription(title);
	// create step
	Step s = new Step("Enter PIN");
	uc.getSteps().add(s);
	s.setInstantReturn(true);
	// add text instructing user
	Text i1 = new Text();
	s.getInputInfoUnits().add(i1);
	i1.setText("Enter your secret in the connected chip card terminal.");

	return uc;
    }

    private static String getPinFromUserConsent(ExecutionEngine response) {
	List<OutputInfoUnit> results = response.getResults().get("Enter PIN").getResults();
	for (OutputInfoUnit next : results) {
	    if (next.type() == InfoUnitElementType.PASSWORD_FIELD) {
		Passwordfield p = (Passwordfield)next;
		if (p.getID().equals("pin")) {
		    return p.getValue();
		}
	    }
	}
	return null;
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
