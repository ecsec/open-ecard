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

package org.openecard.transport.paos;

import org.openecard.common.util.ValueGenerators;


/**
 * The MessageIdGenerator keeps track of the message IDs of the PAOS messages.
 * The ID from the remote message is set and after that a new ID for the reply message can be obtained.
 *
 * @author Tobias Wich
 */
final class MessageIdGenerator {

    private String otherMsg;
    private String myMsg;

    /**
     * Gets the last remote message ID of this instance.
     *
     * @return The last remote message ID.
     */
    public String getRemoteID() {
	return otherMsg;
    }

    /**
     * Sets the remote message ID of this instance.
     * This function does nothing if the last ID does not match the given ID.
     *
     * @param newID The new remote message ID.
     * @return {@code true} if the last ID matches, {@code false} otherwise.
     */
    public boolean setRemoteID(String newID) {
	if (myMsg != null && newID.equals(myMsg)) {
	    // messages don't fit together
	    return false;
	}
	otherMsg = newID;
	return true;
    }

    /**
     * Create a new message ID for the local message that should be sent.
     * This function also saves the new ID in order to match it in the next {@link #setRemoteID(java.lang.String)}
     * invocation.
     *
     * @return The new ID for the message that should be sent.
     */
    public String createNewID() {
	myMsg = ValueGenerators.generateUUID();
	return myMsg;
    }

}
