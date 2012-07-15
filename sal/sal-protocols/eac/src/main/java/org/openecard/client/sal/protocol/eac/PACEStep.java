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

package org.openecard.client.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.ifd.PACECapabilities;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.sal.anytype.AuthDataResponse;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.cvc.CHAT;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.sal.protocol.eac.anytype.EAC1InputType;
import org.openecard.client.sal.protocol.eac.anytype.EAC1OutputType;
import org.openecard.client.sal.protocol.eac.anytype.PACEInputType;
import org.openecard.client.sal.protocol.eac.anytype.PACEOutputType;
import org.openecard.client.sal.protocol.eac.common.PasswordID;
import org.openecard.client.sal.protocol.eac.gui.GUIContentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements PACE protocol step according to BSI-TR-03112-7.
 * See BSI-TR-03112, version 1.1.2., part 7, section 4.6.5.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACEStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(PACEStep.class.getName());
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
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	CardStateEntry cardState = (CardStateEntry) internalData.get("cardState");
	byte[] slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();

	try {
	    boolean nativePace = genericPaceSupport(cardState.handleCopy());
	    EACUserConsent uc = new EACUserConsent(gui, !nativePace);

	    EAC1InputType eac1Input = new EAC1InputType(didAuthenticate.getAuthenticationProtocolData());
	    EAC1OutputType eac1Output = eac1Input.getOutputType();

	    CardVerifiableCertificateChain certChain = new CardVerifiableCertificateChain(eac1Input.getCertificates());
	    CertificateDescription certDescription = CertificateDescription.getInstance(eac1Input.getCertificateDescription());
	    CHAT requiredCHAT = new CHAT(eac1Input.getRequiredCHAT());
	    CHAT optionalCHAT = new CHAT(eac1Input.getOptionalCHAT());
	    byte pinID = PasswordID.valueOf(didAuthenticate.getDIDName()).getByte();

	    // GUI request
	    GUIContentMap content = new GUIContentMap();
	    content.add(GUIContentMap.ELEMENT.CERTIFICATE, certChain.getTerminalCertificate());
	    content.add(GUIContentMap.ELEMENT.CERTIFICATE_DESCRIPTION, certDescription);
	    content.add(GUIContentMap.ELEMENT.REQUIRED_CHAT, requiredCHAT);
	    content.add(GUIContentMap.ELEMENT.OPTIONAL_CHAT, optionalCHAT);
	    content.add(GUIContentMap.ELEMENT.SELECTED_CHAT, requiredCHAT);
	    content.add(GUIContentMap.ELEMENT.PIN_ID, pinID);
	    ResultStatus guiStatus = uc.show(content);

	    if (guiStatus.equals(ResultStatus.CANCEL)) {
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.SAL.CANCELLATION_BY_USER, "User Consent was cancelled by the user."));
		return response;
	    }

	    // GUI response
	    CHAT selectedCHAT = (CHAT) content.get(GUIContentMap.ELEMENT.SELECTED_CHAT);

	    // Create PACEInputType
	    AuthDataMap paceAuthMap = new AuthDataMap(didAuthenticate.getAuthenticationProtocolData());
	    AuthDataResponse paceInputMap = paceAuthMap.createResponse(didAuthenticate.getAuthenticationProtocolData());

	    if (!nativePace) {
		String pin = (String) content.get(GUIContentMap.ELEMENT.PIN);
		paceInputMap.addElement(PACEInputType.PIN, pin);
	    }
	    paceInputMap.addElement(PACEInputType.PIN_ID, PasswordID.parse(pinID).getByteAsString());
	    paceInputMap.addElement(PACEInputType.CHAT, selectedCHAT.toString());
	    paceInputMap.addElement(PACEInputType.CERTIFICATE_DESCRIPTION, ByteUtils.toHexString(eac1Input.getCertificateDescription()));

	    // EstablishChannel
	    EstablishChannel establishChannel = new EstablishChannel();
	    establishChannel.setSlotHandle(slotHandle);
	    establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());
	    establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE);

	    EstablishChannelResponse establishChannelResponse = (EstablishChannelResponse) dispatcher.deliver(establishChannel);

	    if (!establishChannelResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
		// TODO inform user an error happened while establishment of pace channel
	    } else {
		DIDAuthenticationDataType data = establishChannelResponse.getAuthenticationProtocolData();
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

		// Create response
		eac1Output.setEFCardAccess(efCardAccess);
		eac1Output.setRetryCounter(retryCounter);
		eac1Output.setIDPICC(idpicc);
		eac1Output.setCHAT(selectedCHAT.toByteArray());
		eac1Output.setCAR(currentCAR);

		response.setResult(WSHelper.makeResultOK());
		response.setAuthenticationProtocolData(eac1Output.getAuthDataType());
	    }
	} catch (Exception e) {
	    logger.error("Exception", e);
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	}

	return response;
    }


    /**
     * Check if the selected reader supports genericPACE.
     * In that case, the reader is a standard or comfort reader.
     * @param cHandle Handle describing the IFD and reader.
     * @return true when card reader supports genericPACE, false otherwise.
     * @throws org.openecard.client.common.WSHelper.WSException
     * @throws IllegalAccessException Dispatcher error.
     * @throws NoSuchMethodException Dispatcher error.
     * @throws InvocationTargetException Dispatcher error.
     */
    private boolean genericPaceSupport(ConnectionHandleType cHandle) throws WSException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	// request terminal capabilities
	GetIFDCapabilities capReq = new GetIFDCapabilities();
	capReq.setContextHandle(cHandle.getContextHandle());
	capReq.setIFDName(cHandle.getIFDName());
	GetIFDCapabilitiesResponse capRes = (GetIFDCapabilitiesResponse) dispatcher.deliver(capReq);
	WSHelper.checkResult(capRes);

	if (capRes.getIFDCapabilities() != null) {
	    List<SlotCapabilityType> capabilities = capRes.getIFDCapabilities().getSlotCapability();
	    // check all capabilities for generic PACE
	    final String genericPace = PACECapabilities.PACECapability.GenericPACE.getProtocol();
	    for (SlotCapabilityType cap : capabilities) {
		if (cap.getIndex().equals(cHandle.getSlotIndex())) {
		    for (String proto : cap.getProtocol()) {
			if (proto.equals(genericPace)) {
			    return true;
			}
		    }
		}
	    }
	}

	// no PACE capability found
	return false;
    }

}
