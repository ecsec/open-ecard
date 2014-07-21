/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SALStateCallback implements EventCallback {

    private static final Logger logger = LoggerFactory.getLogger(SALStateCallback.class);

    private final CardRecognition recognition;
    private final CredentialManager manager;


    public SALStateCallback(CardRecognition recognition, CardStateMap cardState) {
	this.recognition = recognition;
	this.manager = new CredentialManager(cardState);
    }


    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    ConnectionHandleType handle = (ConnectionHandleType) eventData;
	    switch (eventType) {
		// only add cards with a cardinfo file
		case CARD_RECOGNIZED:
		    logger.info("Add ConnectionHandle to SAL:\n{}", HandlePrinter.printHandle(handle));
		    CardInfoType cif = recognition.getCardInfo(handle.getRecognitionInfo().getCardType());
		    if (cif != null) {
			manager.addCredential(handle, cif);
		    } else {
			logger.info("Not adding card to SAL, because it has no CardInfo file.");
		    }
		    break;
		case CARD_REMOVED:
		    logger.info("Remove ConnectionHandle from SAL.\n{}", HandlePrinter.printHandle(handle));
		    manager.removeCredential(handle);
		    break;
		default:
		    // not a relevant event
		    break;
	    }
	}
    }

}
