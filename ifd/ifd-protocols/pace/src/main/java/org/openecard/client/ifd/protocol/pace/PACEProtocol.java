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

import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.io.UnsupportedEncodingException;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.apdu.utils.CardUtils;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.ifd.anytype.PACEInputType;
import org.openecard.client.common.ifd.anytype.PACEOutputType;
import org.openecard.client.common.ifd.protocol.exception.ProtocolException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.ifd.protocol.pace.gui.GUIContentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEProtocol implements Protocol {

    private static final Logger logger = LoggerFactory.getLogger(PACEProtocol.class.getName());
    private SecureMessaging sm;

    @Override
    public EstablishChannelResponse establish(EstablishChannel req, Dispatcher dispatcher, UserConsent gui) {

	EstablishChannelResponse response = new EstablishChannelResponse();

	try {
	    // Get parameters for the PACE protocol
	    PACEInputType paceInput = new PACEInputType(req.getAuthenticationProtocolData());

	    String pin;
	    byte pinID = paceInput.getPINID();
	    byte[] chat = paceInput.getCHAT();

	    if (paceInput.getPIN() == null || paceInput.getPIN().isEmpty()) {
		// GUI request
		GUIContentMap content = new GUIContentMap();
		content.add(GUIContentMap.ELEMENT.PIN_TYPE, pinID);
		PACEUserConsent paceUserConsent = new PACEUserConsent(gui);
		paceUserConsent.show(content);
		pin = (String) content.get(GUIContentMap.ELEMENT.PIN);
	    } else {
		pin = paceInput.getPIN();
	    }
	    if (pin == null || pin.isEmpty()) {
		response.setResult(WSHelper.makeResultError(
			ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
			"No PIN was entered."));
		return response;
	    }

	    // Read EF.CardAccess from card
	    byte[] slotHandle = req.getSlotHandle();
	    CardUtils cardUtils = new CardUtils(dispatcher);
	    byte[] efcadata = cardUtils.readFile(slotHandle, PACEConstants.EF_CARDACCESS_FID);

	    // Parse SecurityInfos and get PACESecurityInfos
	    SecurityInfos sis = SecurityInfos.getInstance(efcadata);
	    EFCardAccess efca = new EFCardAccess(sis);
	    PACESecurityInfos psi = efca.getPACESecurityInfos();

	    // Start PACE
	    PACEImplementation pace = new PACEImplementation(dispatcher, slotHandle, psi);
	    pace.execute(pin.getBytes(PACEConstants.PIN_CHARSET), pinID, chat);

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
	    response.setResult(WSHelper.makeResultOK());
	    response.setAuthenticationProtocolData(paceOutput.getAuthDataType());

	} catch (UnsupportedEncodingException ex) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // </editor-fold>
	    response.setResult(WSHelper.makeResultError(
		    ECardConstants.Minor.IFD.UNKNOWN_PIN_FORMAT,
		    "Cannot encode the PIN in " + PACEConstants.PIN_CHARSET + " charset."));
	} catch (ProtocolException ex) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // </editor-fold>
	    response.setResult(WSHelper.makeResult(ex));
	} catch (Throwable ex) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // </editor-fold>
	    response.setResult(WSHelper.makeResult(ex));
	}

	return response;
    }

    @Override
    public byte[] applySM(byte[] commandAPDU) {
	try {
	    if (sm != null) {
		return sm.encrypt(commandAPDU);
	    } else {
		throw new RuntimeException("No established Secure Messaging channel available");
	    }
	} catch (Exception ex) {
	    sm = null;
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // </editor-fold>
	    throw new RuntimeException(ex);
	}
    }

    @Override
    public byte[] removeSM(byte[] responseAPDU) {
	try {
	    if (sm != null) {
		return sm.decrypt(responseAPDU);
	    } else {
		throw new RuntimeException("No established Secure Messaging channel available");
	    }
	} catch (Exception ex) {
	    sm = null;
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // </editor-fold>
	    throw new RuntimeException(ex);
	}
    }
}
