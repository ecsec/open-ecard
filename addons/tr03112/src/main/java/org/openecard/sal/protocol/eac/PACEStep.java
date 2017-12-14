/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

package org.openecard.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.interfaces.ObjectSchemaValidator;
import org.openecard.common.interfaces.ObjectValidatorException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.Pair;
import org.openecard.common.util.Promise;
import org.openecard.common.util.TR03112Utils;
import org.openecard.crypto.common.asn1.cvc.CHAT;
import org.openecard.crypto.common.asn1.cvc.CHATVerifier;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateVerifier;
import org.openecard.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.crypto.common.asn1.eac.AuthenticatedAuxiliaryData;
import org.openecard.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.sal.protocol.eac.anytype.EAC1InputType;
import org.openecard.sal.protocol.eac.anytype.EAC1OutputType;
import org.openecard.sal.protocol.eac.anytype.ElementParsingException;
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType;
import org.openecard.sal.protocol.eac.anytype.PACEOutputType;
import org.openecard.sal.protocol.eac.anytype.PasswordID;
import org.openecard.sal.protocol.eac.gui.CHATStep;
import org.openecard.sal.protocol.eac.gui.CVCStep;
import org.openecard.sal.protocol.eac.gui.CVCStepAction;
import org.openecard.sal.protocol.eac.gui.CardMonitor;
import org.openecard.sal.protocol.eac.gui.CardRemovedFilter;
import org.openecard.sal.protocol.eac.gui.PINStep;
import org.openecard.sal.protocol.eac.gui.ProcessingStep;
import org.openecard.sal.protocol.eac.gui.ProcessingStepAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements PACE protocol step according to BSI TR-03112-7.
 *
 * @see "BSI-TR-03112, version 1.1.2., part 7, section 4.6.5."
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
public class PACEStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(PACEStep.class.getName());

    private static final I18n LANG = I18n.getTranslation("eac");
    private static final I18n LANG_PACE = I18n.getTranslation("pace");

    // GUI translation constants
    private static final String TITLE = "eac_user_consent_title";

    private final Dispatcher dispatcher;
    private final UserConsent gui;
    private final EventDispatcher eventDispatcher;

    /**
     * Creates a new PACE protocol step.
     *
     * @param dispatcher Dispatcher
     * @param gui GUI
     * @param eventManager 
     */
    public PACEStep(Dispatcher dispatcher, UserConsent gui, EventDispatcher eventManager) {
	this.dispatcher = dispatcher;
	this.gui = gui;
	this.eventDispatcher = eventManager;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate request, Map<String, Object> internalData) {
	// get context to save values in
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);

	DIDAuthenticate didAuthenticate = request;
	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	ConnectionHandleType conHandle = (ConnectionHandleType) dynCtx.get(TR03112Keys.CONNECTION_HANDLE);

	try {
	    ObjectSchemaValidator valid = (ObjectSchemaValidator) dynCtx.getPromise(EACProtocol.SCHEMA_VALIDATOR).deref();
	    boolean messageValid = valid.validateObject(request);
	    if (! messageValid) {
		String msg = "Validation of the EAC1InputType message failed.";
		LOG.error(msg);
		dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
		return response;
	    }
 	} catch (ObjectValidatorException ex) {
	    String msg = "Validation of the EAC1InputType message failed due to invalid input data.";
	    LOG.error(msg, ex);
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    return response;
	} catch (InterruptedException ex) {
	    String msg = "Thread interrupted while waiting for schema validator instance.";
	    LOG.error(msg, ex);
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    return response;
	}

	if (! ByteUtils.compare(conHandle.getSlotHandle(), didAuthenticate.getConnectionHandle().getSlotHandle())) {
	    String msg = "Invalid connection handle given in DIDAuthenticate message.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, msg);
	    response.setResult(r);
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	    return response;
	}

	byte[] slotHandle = conHandle.getSlotHandle();
	dynCtx.put(EACProtocol.SLOT_HANDLE, slotHandle);
	dynCtx.put(EACProtocol.DISPATCHER, dispatcher);

	try {
	    EAC1InputType eac1Input = new EAC1InputType(didAuthenticate.getAuthenticationProtocolData());
	    EAC1OutputType eac1Output = eac1Input.getOutputType();

	    AuthenticatedAuxiliaryData aad = new AuthenticatedAuxiliaryData(eac1Input.getAuthenticatedAuxiliaryData());
	    byte pinID = PasswordID.valueOf(didAuthenticate.getDIDName()).getByte();
	    final String passwordType = PasswordID.parse(pinID).getString();

	    // determine PACE capabilities of the terminal
	    boolean nativePace = genericPACESupport(conHandle);
	    dynCtx.put(EACProtocol.IS_NATIVE_PACE, nativePace);

	    // Certificate chain
	    CardVerifiableCertificateChain certChain = new CardVerifiableCertificateChain(eac1Input.getCertificates());
	    byte[] rawCertificateDescription = eac1Input.getCertificateDescription();
	    CertificateDescription certDescription = CertificateDescription.getInstance(rawCertificateDescription);

	    // put CertificateDescription into DynamicContext which is needed for later checks
	    dynCtx.put(TR03112Keys.ESERVICE_CERTIFICATE_DESC, certDescription);

	    // according to BSI-INSTANCE_KEY-7 we MUST perform some checks immediately after receiving the eService cert
	    Result activationChecksResult = performChecks(certDescription, dynCtx);
	    if (! ECardConstants.Major.OK.equals(activationChecksResult.getResultMajor())) {
		response.setResult(activationChecksResult);
		dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
		return response;
	    }

	    CHAT requiredCHAT = new CHAT(eac1Input.getRequiredCHAT());
	    CHAT optionalCHAT = new CHAT(eac1Input.getOptionalCHAT());

	    // get the PACEMarker
	    CardStateEntry cardState = (CardStateEntry) internalData.get(EACConstants.IDATA_CARD_STATE_ENTRY);
	    PACEMarkerType paceMarker = getPaceMarker(cardState, passwordType);
	    dynCtx.put(EACProtocol.PACE_MARKER, paceMarker);

	    // Verify that the certificate description matches the terminal certificate
	    CardVerifiableCertificate taCert = certChain.getTerminalCertificate();
	    CardVerifiableCertificateVerifier.verify(taCert, certDescription);
	    // Verify that the required CHAT matches the terminal certificate's CHAT
	    CHAT taCHAT = taCert.getCHAT();
	    
	    // Check that we got an authentication terminal terminal certificate. We abort the process in case there is
	    // an other role.
	    if (taCHAT.getRole() != CHAT.Role.AUTHENTICATION_TERMINAL) {
		String msg = "Unsupported terminal type in Terminal Certificate referenced. Refernced terminal type is " +
			taCHAT.getRole().toString() + ".";
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
		dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
		return response;
	    }

	    CHATVerifier.verfiy(taCHAT, requiredCHAT);
	    // remove overlapping values from optional chat
	    optionalCHAT.restrictAccessRights(taCHAT);


	    // Prepare data in DIDAuthenticate for GUI
	    final EACData eacData = new EACData();
	    eacData.didRequest = didAuthenticate;
	    eacData.certificate = certChain.getTerminalCertificate();
	    eacData.certificateDescription = certDescription;
	    eacData.rawCertificateDescription = rawCertificateDescription;
	    eacData.transactionInfo = eac1Input.getTransactionInfo();
	    eacData.requiredCHAT = requiredCHAT;
	    eacData.optionalCHAT = optionalCHAT;
	    eacData.selectedCHAT = requiredCHAT;
	    eacData.aad = aad;
	    eacData.pinID = pinID;
	    eacData.passwordType = passwordType;
	    dynCtx.put(EACProtocol.EAC_DATA, eacData);

	    // get initial pin status
	    InputAPDUInfoType input = new InputAPDUInfoType();
	    input.setInputAPDU(new byte[] {(byte) 0x00, (byte) 0x22, (byte) 0xC1, (byte) 0xA4, (byte) 0x0F, (byte) 0x80,
		(byte) 0x0A, (byte) 0x04, (byte) 0x00, (byte) 0x7F, (byte) 0x00, (byte) 0x07, (byte) 0x02, (byte) 0x02,
		(byte) 0x04, (byte) 0x02, (byte) 0x02, (byte) 0x83, (byte) 0x01, (byte) 0x03});
	    input.getAcceptableStatusCode().add(new byte[] {(byte) 0x90, (byte) 0x00}); // pin activated 3 tries left
	    input.getAcceptableStatusCode().add(new byte[] {(byte) 0x63, (byte) 0xC2}); // pin activated 2 tries left
	    input.getAcceptableStatusCode().add(new byte[] {(byte) 0x63, (byte) 0xC1}); // pin suspended 1 try left CAN
											// needs to be entered
	    input.getAcceptableStatusCode().add(new byte[] {(byte) 0x63, (byte) 0xC0}); // pin blocked 0 tries left
	    input.getAcceptableStatusCode().add(new byte[] {(byte) 0x62, (byte) 0x83}); // pin deactivated

	    Transmit transmit = new Transmit();
	    transmit.setSlotHandle(slotHandle);
	    transmit.getInputAPDUInfo().add(input);

	    TransmitResponse pinCheckResponse = (TransmitResponse) dispatcher.safeDeliver(transmit);
	    WSHelper.checkResult(pinCheckResponse);
	    byte[] output = pinCheckResponse.getOutputAPDU().get(0);
	    CardResponseAPDU outputApdu = new CardResponseAPDU(output);
	    byte[] status = outputApdu.getStatusBytes();
	    dynCtx.put(EACProtocol.PIN_STATUS_BYTES, status);

	    boolean pinUsable = ! Arrays.equals(status, new byte[]{(byte) 0x63, (byte) 0xC0});

	    // define GUI depending on the PIN status
	    final UserConsentDescription uc = new UserConsentDescription(LANG.translationForKey(TITLE));
	    uc.setDialogType("EAC");
	    if (pinUsable) {
		// create GUI and init executor
		CardMonitor cardMon = new CardMonitor();
		CardRemovedFilter filter = new CardRemovedFilter(conHandle.getIFDName(), conHandle.getSlotIndex());
		eventDispatcher.add(cardMon, filter);
		CVCStep cvcStep = new CVCStep(eacData);
		cvcStep.setBackgroundTask(cardMon);
		CVCStepAction cvcStepAction = new CVCStepAction(cvcStep);
		cvcStep.setAction(cvcStepAction);
		uc.getSteps().add(cvcStep);
		uc.getSteps().add(CHATStep.createDummy());
		uc.getSteps().add(PINStep.createDummy(passwordType));
		ProcessingStep procStep = new ProcessingStep();
		ProcessingStepAction procStepAction = new ProcessingStepAction(procStep);
		procStep.setAction(procStepAction);
		uc.getSteps().add(procStep);
	    } else {
		// ErrorStep is currently not used and needs to be reworked in 1.3.X
		// disable the step here completely to avoid flashing of the step ui.
//		String pin = langPace.translationForKey("pin");
//		String puk = langPace.translationForKey("puk");
//		String title = langPace.translationForKey("step_error_title_blocked", pin);
//		String errorMsg = langPace.translationForKey("step_error_pin_blocked", pin, pin, puk, pin);
//		ErrorStep eStep = new ErrorStep(title, errorMsg);
//		uc.getSteps().add(eStep);

		dynCtx.put(EACProtocol.PACE_EXCEPTION, WSHelper.createException(WSHelper.makeResultError(
			ECardConstants.Minor.IFD.PASSWORD_BLOCKED, "The PIN is blocked.")));
	    }

	    Thread guiThread = new Thread(new Runnable() {
		@Override
		public void run() {
		    // get context here because it is thread local
		    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
		    if (!uc.getSteps().isEmpty()) {
			UserConsentNavigator navigator = gui.obtainNavigator(uc);
			dynCtx.put(TR03112Keys.OPEN_USER_CONSENT_NAVIGATOR, navigator);
			ExecutionEngine exec = new ExecutionEngine(navigator);
			ResultStatus guiResult = exec.process();

			dynCtx.put(EACProtocol.GUI_RESULT, guiResult);

			if (guiResult == ResultStatus.CANCEL) {
			    Promise<Object> pPaceSuccessful = dynCtx.getPromise(EACProtocol.PACE_EXCEPTION);
			    if (!pPaceSuccessful.isDelivered()) {
				pPaceSuccessful.deliver(WSHelper.createException(WSHelper.makeResultError(
					ECardConstants.Minor.SAL.CANCELLATION_BY_USER, "User canceled the PACE dialog.")));
			    }
			}
		    }
		}
	    }, "EAC-GUI");
	    guiThread.start();

	    // wait for PACE to finish
	    Promise<Object> pPaceException = dynCtx.getPromise(EACProtocol.PACE_EXCEPTION);
	    Object pPaceError = pPaceException.deref();
	    if (pPaceError != null) {
		if (pPaceError instanceof WSHelper.WSException) {
		    response.setResult(((WSHelper.WSException) pPaceError).getResult());
		    return response;
		} else if (pPaceError instanceof DispatcherException | pPaceError instanceof InvocationTargetException) {
		    String msg = "Internal error while PACE authentication.";
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg);
		    response.setResult(r);
		    return response;
		} else {
		    String msg = "Unknown error while PACE authentication.";
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.App.UNKNOWN_ERROR, msg);
		    response.setResult(r);
		    return response;
		}
	    }

	    // get challenge from card
	    TerminalAuthentication ta = new TerminalAuthentication(dispatcher, slotHandle);
	    byte[] challenge = ta.getChallenge();

	    // prepare DIDAuthenticationResponse
	    DIDAuthenticationDataType data = eacData.paceResponse.getAuthenticationProtocolData();
	    AuthDataMap paceOutputMap = new AuthDataMap(data);

	    //int retryCounter = Integer.valueOf(paceOutputMap.getContentAsString(PACEOutputType.RETRY_COUNTER));
	    byte[] efCardAccess = paceOutputMap.getContentAsBytes(PACEOutputType.EF_CARD_ACCESS);
	    byte[] currentCAR = paceOutputMap.getContentAsBytes(PACEOutputType.CURRENT_CAR);
	    byte[] previousCAR = paceOutputMap.getContentAsBytes(PACEOutputType.PREVIOUS_CAR);
	    byte[] idpicc = paceOutputMap.getContentAsBytes(PACEOutputType.ID_PICC);

	    // Store SecurityInfos
	    SecurityInfos securityInfos = SecurityInfos.getInstance(efCardAccess);
	    internalData.put(EACConstants.IDATA_SECURITY_INFOS, securityInfos);
	    // Store additional data
	    internalData.put(EACConstants.IDATA_AUTHENTICATED_AUXILIARY_DATA, aad);
	    internalData.put(EACConstants.IDATA_CERTIFICATES, certChain);
	    internalData.put(EACConstants.IDATA_CURRENT_CAR, currentCAR);
	    internalData.put(EACConstants.IDATA_PREVIOUS_CAR, previousCAR);
	    internalData.put(EACConstants.IDATA_CHALLENGE, challenge);

	    // Create response
	    //eac1Output.setRetryCounter(retryCounter);
	    eac1Output.setCHAT(eacData.selectedCHAT.toByteArray());
	    eac1Output.setCurrentCAR(currentCAR);
	    eac1Output.setPreviousCAR(previousCAR);
	    eac1Output.setEFCardAccess(efCardAccess);
	    eac1Output.setIDPICC(idpicc);
	    eac1Output.setChallenge(challenge);

	    response.setResult(WSHelper.makeResultOK());
	    response.setAuthenticationProtocolData(eac1Output.getAuthDataType());

	} catch (CertificateException ex) {
	    LOG.error(ex.getMessage(), ex);
	    String msg = ex.getMessage();
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.SAL.EAC.DOC_VALID_FAILED, msg));
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	} catch (WSHelper.WSException e) {
	    LOG.error(e.getMessage(), e);
	    response.setResult(e.getResult());
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	} catch (ElementParsingException ex) {
	    LOG.error(ex.getMessage(), ex);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, ex.getMessage()));
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	    dynCtx.put(EACProtocol.AUTHENTICATION_FAILED, true);
	}

	return response;
    }

    private PACEMarkerType getPaceMarker(CardStateEntry cardState, String pinType) {
	// TODO: replace with DIDGet call
	byte[] applicationIdentifier = cardState.getCurrentCardApplication().getApplicationIdentifier();
	DIDStructureType didStructure = cardState.getDIDStructure(pinType, applicationIdentifier);
	iso.std.iso_iec._24727.tech.schema.PACEMarkerType didMarker;
	didMarker = (iso.std.iso_iec._24727.tech.schema.PACEMarkerType) didStructure.getDIDMarker();
	return new PACEMarkerType(didMarker);
    }

    private boolean convertToBoolean(Object o) {
	if (o instanceof Boolean) {
	    return ((Boolean) o);
	} else {
	    return false;
	}
    }

    /**
     * Perform all checks as described in BSI TR-03112-7 3.4.4.
     *
     * @param certDescription CertificateDescription of the eService Certificate
     * @param dynCtx Dynamic Context
     * @return a {@link Result} set according to the results of the checks
     */
    private Result performChecks(CertificateDescription certDescription, DynamicContext dynCtx) {
	Object objectActivation = dynCtx.get(TR03112Keys.OBJECT_ACTIVATION);
	Object tokenChecks = dynCtx.get(TR03112Keys.TCTOKEN_CHECKS);
	boolean checkPassed;
	// omit these checks if explicitly disabled
	if (convertToBoolean(tokenChecks)) {
	    checkPassed = checkEserviceCertificate(certDescription, dynCtx);
	    if (! checkPassed) {
		String msg = "Hash of eService certificate is NOT contained in the CertificateDescription.";
		// TODO check for the correct minor type
		Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED, msg);
		return r;
	    }

	    // only perform the following checks if new activation is used
	    if (! convertToBoolean(objectActivation)) {
		checkPassed = checkTCTokenServerCertificates(certDescription, dynCtx);
		if (! checkPassed) {
		    String msg = "Hash of the TCToken server certificate is NOT contained in the CertificateDescription.";
		    // TODO check for the correct minor type
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED, msg);
		    return r;
		}

		checkPassed = checkTCTokenAndSubjectURL(certDescription, dynCtx);
		if (! checkPassed) {
		    String msg = "TCToken does not come from the server to which the authorization certificate was issued.";
		    // TODO check for the correct minor type
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED, msg);
		    return r;
		}
	    } else {
		LOG.warn("Checks according to BSI TR03112 3.4.4 (TCToken specific) skipped.");
	    }
	} else {
	    LOG.warn("Checks according to BSI TR03112 3.4.4 skipped.");
	}

	// all checks passed
	return WSHelper.makeResultOK();
    }

    private boolean checkTCTokenAndSubjectURL(CertificateDescription certDescription, DynamicContext dynCtx) {
	URL tcTokenURL = (URL) dynCtx.get(TR03112Keys.TCTOKEN_URL);
	if (tcTokenURL != null) {
	    try {
		URL subjectURL = new URL(certDescription.getSubjectURL());
		return TR03112Utils.checkSameOriginPolicy(tcTokenURL, subjectURL);
	    } catch (MalformedURLException e) {
		LOG.error("SubjectURL in CertificateDescription is not a well formed URL.");
		return false;
	    }
	} else {
	    LOG.error("No TC Token URL set in Dynamic Context.");
	    return false;
	}
    }

    private boolean checkEserviceCertificate(CertificateDescription certDescription, DynamicContext dynCtx) {
	TlsServerCertificate certificate = (TlsServerCertificate) dynCtx.get(TR03112Keys.ESERVICE_CERTIFICATE);
	if (certificate != null) {
	    return TR03112Utils.isInCommCertificates(certificate, certDescription.getCommCertificates());
	} else {
	    LOG.error("No eService TLS Certificate set in Dynamic Context.");
	    return false;
	}
    }

    private boolean checkTCTokenServerCertificates(CertificateDescription certDescription, DynamicContext dynCtx) {
	List<Pair<URL, TlsServerCertificate>> certificates;
	certificates = (List<Pair<URL, TlsServerCertificate>>) dynCtx.get(TR03112Keys.TCTOKEN_SERVER_CERTIFICATES);
	if (certificates != null) {
	    for (Pair<URL, TlsServerCertificate> cert : certificates) {
		if (cert instanceof Pair) {
		    TlsServerCertificate bcCert = cert.p2;
		    if (!TR03112Utils.isInCommCertificates(bcCert, certDescription.getCommCertificates())) {
			return false;
		    }
		}
	    }
	    return true;
	} else {
	    LOG.error("No TC Token server certificates set in Dynamic Context.");
	    return false;
	}
    }

    /**
     * Check if the selected card reader supports PACE.
     * In that case, the reader is a standard or comfort reader.
     *
     * @param connectionHandle Handle describing the IFD and reader.
     * @return true when card reader supports genericPACE, false otherwise.
     * @throws WSHelper.WSException
     */
    private boolean genericPACESupport(ConnectionHandleType connectionHandle) throws WSHelper.WSException {
	// Request terminal capabilities
	GetIFDCapabilities capabilitiesRequest = new GetIFDCapabilities();
	capabilitiesRequest.setContextHandle(connectionHandle.getContextHandle());
	capabilitiesRequest.setIFDName(connectionHandle.getIFDName());
	GetIFDCapabilitiesResponse capabilitiesResponse = (GetIFDCapabilitiesResponse) dispatcher.safeDeliver(capabilitiesRequest);
	WSHelper.checkResult(capabilitiesResponse);

	if (capabilitiesResponse.getIFDCapabilities() != null) {
	    List<SlotCapabilityType> capabilities = capabilitiesResponse.getIFDCapabilities().getSlotCapability();
	    // Check all capabilities for generic PACE
	    final String genericPACE = PACECapabilities.PACECapability.GenericPACE.getProtocol();
	    for (SlotCapabilityType capability : capabilities) {
		if (capability.getIndex().equals(connectionHandle.getSlotIndex())) {
		    for (String protocol : capability.getProtocol()) {
			if (protocol.equals(genericPACE)) {
			    return true;
			}
		    }
		}
	    }
	}

	// No PACE capability found
	return false;
    }

}
