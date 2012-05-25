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
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import java.util.Map;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.apdu.utils.CardUtils;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.util.IntegerUtils;
import org.openecard.client.crypto.common.asn1.eac.CASecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.client.sal.protocol.eac.anytype.EAC2OutputType;
import org.openecard.client.sal.protocol.eac.anytype.EACAdditionalInputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements Chip Authentication protocol step according to BSI-TR-03112-7.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ChipAuthenticationStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ChipAuthenticationStep.class.getName());
    private final Dispatcher dispatcher;

    /**
     * Creates a new Chip Authentication step.
     *
     * @param dispatcher Dispatcher
     */
    public ChipAuthenticationStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	byte[] slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();

	try {
	    EACAdditionalInputType eacAdditionalInput = new EACAdditionalInputType(didAuthenticate.getAuthenticationProtocolData());
	    EAC2OutputType eac2Output = eacAdditionalInput.getOutputType();

	    TerminalAuthentication ta = new TerminalAuthentication(dispatcher, slotHandle);
	    ChipAuthentication ca = new ChipAuthentication(dispatcher, slotHandle);

	    // TA: Step 4 - External Authentication
	    ta.externalAuthentication(eacAdditionalInput.getSignature());

	    // Read EF.CardSecurity
	    CardUtils cardUtils = new CardUtils(dispatcher);
	    byte[] efCardSecurity = cardUtils.readFile(slotHandle, EACConstants.EF_CARDSECURITY_FID);

	    // CA: Step 1 - MSE:SET AT
	    SecurityInfos securityInfos = (SecurityInfos) internalData.get(EACConstants.INTERNAL_DATA_SECURITY_INFOS);
	    EFCardAccess efca = new EFCardAccess(securityInfos);
	    CASecurityInfos cas = efca.getCASecurityInfos();

	    byte[] oID = ObjectIdentifierUtils.getValue(cas.getCAInfo().getProtocol());
	    byte[] keyID = IntegerUtils.toByteArray(cas.getCAInfo().getKeyID());
	    ca.mseSetAT(oID, keyID);

	    // CA: Step 2 - General Authenticate
	    byte[] key = (byte[]) internalData.get(EACConstants.INTERNAL_DATA_PK_PCD);
	    byte[] responseData = ca.generalAuthenticate(key);

	    TLV tlv = TLV.fromBER(responseData);
	    byte[] nonce = tlv.findChildTags(0x81).get(0).getValue();
	    byte[] token = tlv.findChildTags(0x82).get(0).getValue();

	    // Disable Secure Messaging
	    DestroyChannel destroyChannel = new DestroyChannel();
	    destroyChannel.setSlotHandle(didAuthenticate.getConnectionHandle().getSlotHandle());
	    dispatcher.deliver(destroyChannel);

	    // Create response
	    eac2Output.setEFCardSecurity(efCardSecurity);
	    eac2Output.setNonce(nonce);
	    eac2Output.setToken(token);

	    response.setResult(WSHelper.makeResultOK());
	    response.setAuthenticationProtocolData(eac2Output.getAuthDataType());

	    return response;
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	}

	return response;
    }
}
