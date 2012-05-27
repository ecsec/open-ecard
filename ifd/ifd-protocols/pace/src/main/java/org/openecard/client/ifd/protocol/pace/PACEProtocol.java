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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

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

	    byte[] pin;
	    byte pinID = paceInput.getPINID();
	    byte[] chat = paceInput.getCHAT();

	    if (paceInput.getPIN() == null || paceInput.getPIN().isEmpty()) {
		// GUI request
		GUIContentMap content = new GUIContentMap();
		content.add(GUIContentMap.ELEMENT.PIN_ID, pinID);
		PACEUserConsent paceUserConsent = new PACEUserConsent(gui);
		paceUserConsent.show(content);
		pin = ((String) content.get(GUIContentMap.ELEMENT.PIN)).getBytes(PACEConstants.PIN_CHARSET);
	    } else {
		pin = paceInput.getPIN().getBytes(PACEConstants.PIN_CHARSET);
	    }
	    if (pin == null || pin.length == 0) {
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
	    pace.execute(pin, pinID, chat);

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
