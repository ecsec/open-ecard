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

package org.openecard.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import org.openecard.common.enums.EventType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Recognizer implements Runnable {

    private final EventManager manager;
    private final EventType[] events;
    private final String ifdName;
    private final SlotStatusType status;

    public Recognizer(EventManager manager, String ifdName, SlotStatusType status, EventType... events) {
	this.manager = manager;
	this.events = events;
	this.ifdName = ifdName;
	this.status = status;
    }

    @Override
    public void run() {
	if (events.length > 0) {
	    ConnectionHandleType conHandle = manager.recognizeSlot(ifdName, status, false);
	    ConnectionHandleType conHandleRecog = null;
	    for (EventType type : events) {
		// let's hope, that CARD_RECOGNIZED comes last
		if (type.equals(EventType.CARD_RECOGNIZED)) {
		    if (conHandleRecog == null) {
			conHandleRecog = manager.recognizeSlot(ifdName, status, true);
			manager.notify(type, conHandleRecog);
		    }
		} else {
		    manager.notify(type, conHandle);
		}
	    }
	}
    }

}
