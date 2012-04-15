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
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ResponseAPDU;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.EAC2OutputType;
import org.openecard.client.common.sal.anytype.EACAdditionalInputType;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.iso7816.FCP;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.crypto.common.asn1.eac.oid.CAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ChipAuthenticationStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private static final Logger _logger = LogManager.getLogger(ChipAuthenticationStep.class.getName());

    private Dispatcher dispatcher;
    private byte[] slotHandle;

    public ChipAuthenticationStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData)",
		    new Object[]{didAuthenticate, internalData});
	} // </editor-fold>

	DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();

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

	    byte[] oid = ObjectIdentifierUtils.getValue(cas.getCAInfo().getProtocol());
	    byte[] keyID = IntegerUtils.toByteArray(cas.getCAInfo().getKeyID());
	    ca.mseSetAT(oid, keyID);

	    // CA: Step 2 - General Authenticate
	    byte[] key = ByteUtils.concatenate((byte) 0x04, (byte[]) internalData.get(EACConstants.INTERNAL_DATA_PK_PCD));
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

	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (logger.isLoggable(Level.FINER)) {
		logger.exiting(this.getClass().getName(), "perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData)", response);
	    } // </editor-fold>

	    return response;
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "Exception", ex);
	    response.setResult(WSHelper.makeResultUnknownError(ex.getMessage()));
	}

	return response;
    }

}
