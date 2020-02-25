/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.common.sal.util;

import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventFilter;

/**
 *
 * @author Neil Crossley
 */
public class CardRemovalFilter implements EventFilter {

    private boolean wasInserted = false;

    @Override
    public boolean matches(EventType t, EventObject o) {
	if (t == EventType.CARD_INSERTED) {
	    wasInserted = true;
	    return false;
	}
	if (t == EventType.CARD_REMOVED) {
	    return wasInserted;
	}

	return false;
    }
}
