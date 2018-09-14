/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

package org.openecard.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.addon.sal.CredentialManager;
import org.openecard.common.event.EventType;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.event.EventObject;
import org.openecard.common.interfaces.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class SALStateCallback implements EventCallback {

    private static final Logger LOG = LoggerFactory.getLogger(SALStateCallback.class);

    private final Environment env;
    private final CredentialManager manager;


    public SALStateCallback(Environment env, CardStateMap cardState) {
	this.env = env;
	this.manager = new CredentialManager(cardState);
    }


    @Override
    public void signalEvent(EventType eventType, EventObject eventData) {
	if (eventData instanceof IfdEventObject) {
	    IfdEventObject ifdEvtData = (IfdEventObject) eventData;
	    ConnectionHandleType handle = ifdEvtData.getHandle();
	    switch (eventType) {
		// only add cards with a cardinfo file
		case CARD_RECOGNIZED:
		    LOG.info("Add ConnectionHandle to SAL:\n{}", HandlePrinter.printHandle(handle));
		    String cardType = handle.getRecognitionInfo().getCardType();
		    CardInfoType cif = env.getCIFProvider().getCardInfo(handle, cardType);
		    if (cif != null) {
			manager.addCredential(handle, ifdEvtData.getIfaceProtocol(), cif);
			// notify everyone that the card is now available in the SAL
			env.getEventDispatcher().notify(EventType.RECOGNIZED_CARD_ACTIVE, eventData);
		    } else {
			LOG.info("Not adding card to SAL, because it has no CardInfo file.");
		    }
		    break;
		case CARD_REMOVED:
		    LOG.info("Remove ConnectionHandle from SAL.\n{}", HandlePrinter.printHandle(handle));
		    manager.removeCredential(handle);
		    break;
		default:
		    // not a relevant event
		    break;
	    }
	}
    }

}
