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

package org.openecard.common.event;

import java.util.ArrayList;
import java.util.Arrays;
import org.openecard.common.interfaces.EventFilter;

/**
 *
 * @author Tobias Wich
 */
class EventTypeFilter implements EventFilter {

    private final ArrayList<EventType> eventType;

    public EventTypeFilter(EventType ... eventType) {
	if (eventType.length == 0) {
	    this.eventType = new ArrayList<>(Arrays.asList(EventType.values()));
	} else {
	    this.eventType = new ArrayList<>(Arrays.asList(eventType));
	}
    }


    @Override
    public boolean matches(EventType t, EventObject o) {
	for (EventType next : eventType) {
	    if (t.equals(next)) {
		return true;
	    }
	}
	return false;
    }

}
