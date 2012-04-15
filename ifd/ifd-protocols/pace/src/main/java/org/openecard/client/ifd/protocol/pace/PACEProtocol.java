/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.apdu.utils.CardUtils;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.ifd.anytype.PACEInputType;
import org.openecard.client.common.ifd.anytype.PACEOutputType;
import org.openecard.client.common.ifd.protocol.exception.ProtocolException;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.client.gui.UserConsent;
import org.openecard.ws.IFD;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEProtocol implements Protocol {

    private static final Logger logger = LogManager.getLogger(PACEProtocol.class.getName());
    private SecureMessaging sm;

    @Override
    public EstablishChannelResponse establish(EstablishChannel req, IFD ifd, UserConsent gui) {
	DIDAuthenticationDataType authData = req.getAuthenticationProtocolData();

	try {
	    // Get parameters for the PACE protocol
	    PACEInputType paceInput = new PACEInputType(authData);
	    byte passwordType = paceInput.getPINID();
	    byte[] chat = paceInput.getCHAT();
	    byte[] pin;

	    if (paceInput.getPIN() == null || paceInput.getPIN().isEmpty()) {
		// No PIN is given, ask user for PIN
		PACEUserConsent paceUserConsent = new PACEUserConsent();
		pin = paceUserConsent.getPINFromUser(gui);
	    } else {
		// PIN must be encoded in ISO/IEC 8859 encoding
		pin = paceInput.getPIN().getBytes("ISO-8859-1");
	    }

	    // Read EF.CardAccess from card
	    byte[] slotHandle = req.getSlotHandle();
	    CardUtils cardUtils = new CardUtils(ifd);
	    byte[] efcadata = cardUtils.readFile(slotHandle, PACEConstants.EF_CARDACCESS_FID);

	    // Parse SecurityInfos and get PACESecurityInfos
	    SecurityInfos sis = SecurityInfos.getInstance(efcadata);
	    EFCardAccess efca = new EFCardAccess(sis);
	    PACESecurityInfos psi = efca.getPACESecurityInfos();

	    // Start PACE
	    PACEImplementation pace = new PACEImplementation(ifd, slotHandle, psi);
	    pace.execute(pin, passwordType, chat);

	    // Establish Secure Messaging channel
	    sm = new SecureMessaging(pace.getKeyMAC(), pace.getKeyENC());

	    // Create AuthenticationProtocolData (PACEOutputType)
	    PACEOutputType paceOutput = paceInput.getOutputType();
	    paceOutput.setEFCardAccess(efcadata);
	    paceOutput.setCurrentCAR(pace.getCurrentCAR());
	    paceOutput.setPreviousCAR(pace.getPreviousCAR());
	    paceOutput.setIDPICC(pace.getIDPICC());
	    paceOutput.setRetryCounter(pace.getRetryCounter());

	    // Create EstablishChannelResponse
	    EstablishChannelResponse response = new EstablishChannelResponse();
	    response.setResult(WSHelper.makeResultOK());
	    response.setAuthenticationProtocolData(paceOutput.getAuthDataType());

	    return response;
	} catch (UnsupportedEncodingException ex) {
	    logger.logp(Level.FINER, this.getClass().getName(), "", "Exception", ex);
	    return WSHelper.makeResponse(
		    EstablishChannelResponse.class,
		    WSHelper.makeResultError(ECardConstants.Minor.IFD.UNKNOWN_PIN_FORMAT, "Cannot encode the PIN in ISO-8859-1 charset."));
	} catch (ProtocolException ex) {
	    logger.logp(Level.FINER, this.getClass().getName(), "", "Exception", ex);
	    return WSHelper.makeResponse(
		    EstablishChannelResponse.class,
		    WSHelper.makeResult(ex));
	} catch (Throwable t) {
	    logger.logp(Level.FINER, this.getClass().getName(), "", "Exception", t);
	    return WSHelper.makeResponse(
		    EstablishChannelResponse.class,
		    WSHelper.makeResult(t));
	}
    }

    @Override
    public byte[] applySM(byte[] commandAPDU) {
	try {
	    return sm.encrypt(commandAPDU);
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "Exception", ex);
	}
	return null;
    }

    @Override
    public byte[] removeSM(byte[] responseAPDU) {
	try {
	    return sm.decrypt(responseAPDU);
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "Exception", ex);
	}
	return null;
    }

}
