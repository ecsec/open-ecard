/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.transport.paos

import org.openecard.common.util.ValueGenerators.generateUUID

/**
 * The MessageIdGenerator keeps track of the message IDs of the PAOS messages.
 * The ID from the remote message is set and after that a new ID for the reply message can be obtained.
 *
 * @author Tobias Wich
 */
class MessageIdGenerator {
    /**
     * Gets the last remote message ID of this instance.
     *
     * @return The last remote message ID.
     */
    var remoteID: String? = null
        private set
    private var myMsg: String? = null

    /**
     * Sets the remote message ID of this instance.
     * This function does nothing if the last ID does not match the given ID.
     *
     * @param newID The new remote message ID.
     * @return `true` if the last ID matches, `false` otherwise.
     */
    fun setRemoteID(newID: String): Boolean {
        if (myMsg != null && newID == myMsg) {
            // messages don't fit together
            return false
        }
        remoteID = newID
        return true
    }

    /**
     * Create a new message ID for the local message that should be sent.
     * This function also saves the new ID in order to match it in the next [.setRemoteID]
     * invocation.
     *
     * @return The new ID for the message that should be sent.
     */
    fun createNewID(): String {
		val newId = generateUUID()
        myMsg = newId
        return newId
    }
}
