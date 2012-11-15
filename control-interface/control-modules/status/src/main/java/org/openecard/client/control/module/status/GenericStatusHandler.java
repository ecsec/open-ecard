/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.control.module.status;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.Set;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.ws.schema.Status;


/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class GenericStatusHandler {

    private CardStateMap cardStates;

    /**
     * Create a new GenericStatusHandler.
     * 
     * @param cardStates CardStateMap of the client for querying all ConnectionHandles
     */
    public GenericStatusHandler(CardStateMap cardStates) {
	this.cardStates = cardStates;
    }

    /**
     * Handles a Status-Request by returning a status including list of all known ConnectionHandles (including
     * unrecognized cards).
     * 
     * @return Status including list of all known ConnectionHandles
     */
    public Status handleRequest() {
	Status status = new Status();
	ConnectionHandleType handle = new ConnectionHandleType();
	Set<CardStateEntry> entries = this.cardStates.getMatchingEntries(handle);

	for (CardStateEntry entry : entries) {
	    status.getConnectionHandle().add(entry.handleCopy());
	}

	return status;
    }

}
