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

package org.openecard.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import java.util.List;
import java.util.Map;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.FunctionType;
import org.openecard.common.sal.ProtocolStep;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.crypto.common.asn1.cvc.CHAT;
import org.openecard.crypto.common.asn1.cvc.CHATVerifier;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateVerifier;
import org.openecard.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;
import org.openecard.sal.protocol.eac.actions.CHATStepAction;
import org.openecard.sal.protocol.eac.actions.PINStepAction;
import org.openecard.sal.protocol.eac.anytype.EAC1InputType;
import org.openecard.sal.protocol.eac.anytype.EAC1OutputType;
import org.openecard.sal.protocol.eac.anytype.PACEOutputType;
import org.openecard.sal.protocol.eac.anytype.PasswordID;
import org.openecard.sal.protocol.eac.gui.CHATStep;
import org.openecard.sal.protocol.eac.gui.CVCStep;
import org.openecard.sal.protocol.eac.gui.PINStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements PACE protocol step according to BSI-TR-03112-7.
 *
 * @see "BSI-TR-03112, version 1.1.2., part 7, section 4.6.5."
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PACEStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(PACEStep.class.getName());

    // GUI translation constants
    private static final String TITLE = "eac_user_consent_title";

    private final I18n lang = I18n.getTranslation("eac");

    private final Dispatcher dispatcher;
    private final UserConsent gui;

    /**
     * Creates a new PACE protocol step.
     *
     * @param dispatcher Dispatcher
     * @param gui GUI
     */
    public PACEStep(Dispatcher dispatcher, UserConsent gui) {
	this.dispatcher = dispatcher;
	this.gui = gui;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate request, Map<String, Object> internalData) {
	DIDAuthenticate didAuthenticate = request;
	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	byte[] slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();

	try {
	    EAC1InputType eac1Input = new EAC1InputType(didAuthenticate.getAuthenticationProtocolData());
	    EAC1OutputType eac1Output = eac1Input.getOutputType();

	    // determine PACE capabilities of the terminal
	    CardStateEntry cardState = (CardStateEntry) internalData.get(EACConstants.INTERNAL_DATA_CARD_STATE_ENTRY);
	    boolean nativePace = genericPACESupport(cardState.handleCopy());

	    // Certificate chain
	    CardVerifiableCertificateChain certChain = new CardVerifiableCertificateChain(eac1Input.getCertificates());
	    byte[] rawCertificateDescription = eac1Input.getCertificateDescription();
	    CertificateDescription certDescription = CertificateDescription.getInstance(rawCertificateDescription);
	    CHAT requiredCHAT = new CHAT(eac1Input.getRequiredCHAT());
	    CHAT optionalCHAT = new CHAT(eac1Input.getOptionalCHAT());
	    byte pinID = PasswordID.valueOf(didAuthenticate.getDIDName()).getByte();
	    String passwordType = PasswordID.parse(pinID).getString();

	    // Verify that the certificate description matches the terminal certificate
	    CardVerifiableCertificate taCert = certChain.getTerminalCertificates().get(0);
	    CardVerifiableCertificateVerifier.verify(taCert, certDescription);
	    // Verify that the required CHAT matches the terminal certificate's CHAT
	    CHATVerifier.verfiy(taCert.getCHAT(), optionalCHAT);
	    // Verify that the required and optional CHAT has no more rights then defined in the certificate
	    CHATVerifier.verfiy(taCert.getCHAT(), requiredCHAT);
	    CHATVerifier.verfiy(taCert.getCHAT(), optionalCHAT);


	    // Prepare data in DIDAuthenticate for GUI
	    EACData eacData = new EACData();
	    eacData.didRequest = didAuthenticate;
	    eacData.certificate = certChain.getTerminalCertificates().get(0);
	    eacData.certificateDescription = certDescription;
	    eacData.rawCertificateDescription = rawCertificateDescription;
	    eacData.requiredCHAT = requiredCHAT;
	    eacData.optionalCHAT = optionalCHAT;
	    eacData.selectedCHAT = requiredCHAT;
	    eacData.pinID = pinID;
	    eacData.passwordType = passwordType;

	    // create GUI and init executor
	    UserConsentDescription uc = new UserConsentDescription(lang.translationForKey(TITLE));
	    CVCStep cvcStep = new CVCStep(eacData);
	    CHATStep chatStep = new CHATStep(eacData);
	    PINStep pinStep = new PINStep(eacData, ! nativePace);

	    uc.getSteps().add(cvcStep);
	    uc.getSteps().add(chatStep);
	    uc.getSteps().add(pinStep);

	    StepAction chatAction = new CHATStepAction(eacData, chatStep);
	    chatStep.setAction(chatAction);
	    StepAction pinAction = new PINStepAction(eacData, ! nativePace, slotHandle, dispatcher, pinStep);
	    pinStep.setAction(pinAction);

	    // execute GUI
	    UserConsentNavigator navigator = gui.obtainNavigator(uc);
	    ExecutionEngine exec = new ExecutionEngine(navigator);
	    ResultStatus guiResult = exec.process();

	    if (guiResult == ResultStatus.CANCEL) {
		String protocol = didAuthenticate.getAuthenticationProtocolData().getProtocol();
		cardState.removeProtocol(protocol);
		String msg = "User Consent was cancelled by the user.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.CANCELLATION_BY_USER, msg);
		response.setResult(r);
		return response;
	    }


	    // prepare DIDAuthenticationResponse
	    DIDAuthenticationDataType data = eacData.paceResponse.getAuthenticationProtocolData();
	    AuthDataMap paceOutputMap = new AuthDataMap(data);

	    int retryCounter = Integer.valueOf(paceOutputMap.getContentAsString(PACEOutputType.RETRY_COUNTER));
	    byte[] efCardAccess = paceOutputMap.getContentAsBytes(PACEOutputType.EF_CARD_ACCESS);
	    byte[] currentCAR = paceOutputMap.getContentAsBytes(PACEOutputType.CURRENT_CAR);
	    byte[] previousCAR = paceOutputMap.getContentAsBytes(PACEOutputType.PREVIOUS_CAR);
	    byte[] idpicc = paceOutputMap.getContentAsBytes(PACEOutputType.ID_PICC);

	    // Store SecurityInfos
	    SecurityInfos securityInfos = SecurityInfos.getInstance(efCardAccess);
	    internalData.put(EACConstants.INTERNAL_DATA_SECURITY_INFOS, securityInfos);
	    // Store additional data
	    internalData.put(EACConstants.INTERNAL_DATA_AUTHENTICATED_AUXILIARY_DATA, eac1Input.getAuthenticatedAuxiliaryData());
	    internalData.put(EACConstants.INTERNAL_DATA_CERTIFICATES, certChain);
	    internalData.put(EACConstants.INTERNAL_DATA_CURRENT_CAR, currentCAR);

	    // Create response
	    eac1Output.setEFCardAccess(efCardAccess);
	    eac1Output.setRetryCounter(retryCounter);
	    eac1Output.setIDPICC(idpicc);
	    eac1Output.setCHAT(eacData.selectedCHAT.toByteArray());
	    eac1Output.setCAR(currentCAR);

	    response.setResult(WSHelper.makeResultOK());
	    response.setAuthenticationProtocolData(eac1Output.getAuthDataType());

	} catch (WSHelper.WSException e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	}

	return response;
    }

    /**
     * Check if the selected card reader supports PACE.
     * In that case, the reader is a standard or comfort reader.
     *
     * @param connectionHandle Handle describing the IFD and reader.
     * @return true when card reader supports genericPACE, false otherwise.
     * @throws Exception
     */
    private boolean genericPACESupport(ConnectionHandleType connectionHandle) throws Exception {
	// Request terminal capabilities
	GetIFDCapabilities capabilitiesRequest = new GetIFDCapabilities();
	capabilitiesRequest.setContextHandle(connectionHandle.getContextHandle());
	capabilitiesRequest.setIFDName(connectionHandle.getIFDName());
	GetIFDCapabilitiesResponse capabilitiesResponse = (GetIFDCapabilitiesResponse) dispatcher.deliver(capabilitiesRequest);
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
