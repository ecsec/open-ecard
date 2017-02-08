/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.event;

import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.mdlw.sal.MwSlot;
import org.openecard.mdlw.sal.MwToken;


/**
 *
 * @author Jan Mannsbart
 */
public class MwStateCallback implements EventCallback {

    private static final Logger LOG = LoggerFactory.getLogger(MwStateCallback.class);

    private final Environment env;
    private final CardStateMap states;

    public MwStateCallback(Environment env, CardStateMap cardState) {
        this.env = env;
        this.states = cardState;
    }

    @Override
    public void signalEvent(EventType eventType, EventObject o) {
	try {
	    if (o instanceof MwEventObject) {
		ConnectionHandleType handle = ((MwEventObject) o).getHandle();
		MwSlot slot = ((MwEventObject) o).getMwSlot();

		switch (eventType) {
		    case CARD_RECOGNIZED:
			MwToken token = ((MwSlot) slot).getTokenInfo();
			String cardType = String.format("%s_%s", token.getManufacturerID(), token.getModel());
			cardType = CardConfig.mapMiddlewareType(cardType);
			CardInfoType cif = null;
			if (cardType != null) {
			    cif = env.getCIFProvider().getCardInfo(handle, cardType);
			}
			if (cif == null) {
			    LOG.warn("Unknown card recognized by Middleware.");
			    return;
			}

			// create new entry in card states
			CardStateEntry entry = new CardStateEntry(handle, cif);
			states.addEntry(entry);

			break;

		    case CARD_REMOVED:
			LOG.info("Remove card");
			states.removeEntry(handle);
			break;

		    default:
			LOG.debug("No relevant event received.");
			break;
		}
	    }
	} catch (CryptokiException ex) {
	    LOG.info("Cryptoki Token invalid.", ex);
	} catch (RuntimeException ex) {
	    LOG.error("Error in CIF augmentation process.", ex);
	}
    }

}
