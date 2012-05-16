/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
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
package org.openecard.client.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.util.Map;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.ifd.anytype.PACEInputType;
import org.openecard.client.common.ifd.anytype.PACEOutputType;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.sal.anytype.AuthDataResponse;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.cvc.CHAT;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.sal.protocol.eac.anytype.EAC1InputType;
import org.openecard.client.sal.protocol.eac.anytype.EAC1OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PACEStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(PACEStep.class.getName());
    private Dispatcher dispatcher;
    private EACUserConsent gui;

    public PACEStep(Dispatcher dispatcher, UserConsent gui) {
	this.dispatcher = dispatcher;
	this.gui = new EACUserConsent(gui);
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	logger.trace(LoggingConstants.ENTER, "perform");
	// </editor-fold>

	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	byte[] slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();

	try {
	    EAC1InputType eac1Input = new EAC1InputType(didAuthenticate.getAuthenticationProtocolData());
	    EAC1OutputType eac1Output = eac1Input.getOutputType();

	    CertificateDescription description = CertificateDescription.getInstance(eac1Input.getCertificateDescription());
	    CHAT requiredCHAT = new CHAT(eac1Input.getRequiredCHAT());
	    CHAT optionalCHAT = new CHAT(eac1Input.getOptionalCHAT());
	    CHAT chosenCHAT = gui.show(eac1Input.getCertificates().get(0), description, requiredCHAT, optionalCHAT);

	    // Create PACEInputType
	    AuthDataMap paceAuthMap = new AuthDataMap(didAuthenticate.getAuthenticationProtocolData());
	    AuthDataResponse paceInputMap = paceAuthMap.createResponse(didAuthenticate.getAuthenticationProtocolData());
	    //FIXME
	    paceInputMap.addElement(PACEInputType.PIN_ID, "3");
	    paceInputMap.addElement(PACEInputType.CHAT, chosenCHAT.toString());
	    paceInputMap.addElement(PACEInputType.CERTIFICATE_DESCRIPTION, ByteUtils.toHexString(eac1Input.getCertificateDescription()));

	    // EstablishChannel
	    EstablishChannel establishChannel = new EstablishChannel();
	    establishChannel.setSlotHandle(slotHandle);
	    establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse());

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
		internalData.put(EACConstants.INTERNAL_DATA_CERTIFICATES, new CardVerifiableCertificateChain(eac1Input.getCertificates()));
		internalData.put(EACConstants.CURRENT_CAR, currentCAR);
		
		// Create response
		eac1Output.setEFCardAccess(efCardAccess);
		eac1Output.setRetryCounter(retryCounter);
		eac1Output.setIDPICC(idpicc);
		eac1Output.setCHAT(chosenCHAT.toByteArray());
		eac1Output.setCAR(currentCAR);

		response.setResult(WSHelper.makeResultOK());
		response.setAuthenticationProtocolData(eac1Output.getAuthDataType());
	    }
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    logger.trace(LoggingConstants.EXIT, "perform");
	    // </editor-fold>

	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	}

	return response;
    }
}
