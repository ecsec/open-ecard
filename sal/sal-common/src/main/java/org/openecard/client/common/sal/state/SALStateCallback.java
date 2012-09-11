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

package org.openecard.client.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SALStateCallback implements EventCallback {

    private static final Logger _logger = LoggerFactory.getLogger(SALStateCallback.class);

    private final CardRecognition recognition;
    private final CardStateMap cardState;


    public SALStateCallback(CardRecognition recognition, CardStateMap cardState) {
	this.recognition = recognition;
	this.cardState = cardState;
    }


    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    ConnectionHandleType handle = (ConnectionHandleType) eventData;
	    switch (eventType) {
		// only add cards with a cardinfo file
		case CARD_RECOGNIZED:
		    _logger.info("Add ConnectionHandle to SAL:\n{}", HandlePrinter.printHandle(handle));
		    CardInfoType cif = recognition.getCardInfo(handle.getRecognitionInfo().getCardType());
		    if (cif != null) {
			CardStateEntry entry = new CardStateEntry(handle, cif);
			cardState.addEntry(entry);
		    } else {
			_logger.info("Not adding card to SAL, because it has no CardInfo file.");
		    }
		    break;
		case CARD_REMOVED:
		    _logger.info("Remove ConnectionHandle from SAL.\n{}", HandlePrinter.printHandle(handle));
		    cardState.removeEntry(handle);
		    break;
		default:
		    // not a relevant event
		    break;
	    }
	}
    }

}
