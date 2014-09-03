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

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import java.util.Map;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.utils.CardUtils;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.IntegerUtils;
import org.openecard.common.util.ShortUtils;
import org.openecard.crypto.common.asn1.eac.CASecurityInfos;
import org.openecard.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.sal.protocol.eac.anytype.EAC2OutputType;
import org.openecard.sal.protocol.eac.anytype.EACAdditionalInputType;
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
	    CardResponseAPDU resp = CardUtils.selectFileWithOptions(dispatcher, slotHandle,
		    ShortUtils.toByteArray(EACConstants.EF_CARDSECURITY_FID), null, CardUtils.FCP_RESPONSE_DATA);
	    FCP efCardSecurityFCP = new FCP(TLV.fromBER(resp.getData()));
	    byte[] efCardSecurity = CardUtils.readFile(efCardSecurityFCP, dispatcher, slotHandle);

	    // CA: Step 1 - MSE:SET AT
	    SecurityInfos securityInfos = (SecurityInfos) internalData.get(EACConstants.IDATA_SECURITY_INFOS);
	    EFCardAccess efca = new EFCardAccess(securityInfos);
	    CASecurityInfos cas = efca.getCASecurityInfos();

	    byte[] oID = ObjectIdentifierUtils.getValue(cas.getCAInfo().getProtocol());
	    byte[] keyID = IntegerUtils.toByteArray(cas.getCAInfo().getKeyID());
	    ca.mseSetAT(oID, keyID);

	    // CA: Step 2 - General Authenticate
	    byte[] key = (byte[]) internalData.get(EACConstants.IDATA_PK_PCD);
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
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	}

	return response;
    }

}
