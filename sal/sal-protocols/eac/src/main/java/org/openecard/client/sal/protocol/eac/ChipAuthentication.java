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

package org.openecard.client.sal.protocol.eac;

import org.openecard.client.common.apdu.GeneralAuthenticate;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.apdu.exception.APDUException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.protocol.exception.ProtocolException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.sal.protocol.eac.apdu.MSESetATCA;


/**
 * Implements the Chip Authentication protocol.
 * See BSI-TR-03110, version 2.10, part 2, Section B.3.3.
 * See BSI-TR-03110, version 2.10, part 3, Section B.2.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ChipAuthentication {

    private final Dispatcher dispatcher;
    private final byte[] slotHandle;

    /**
     * Creates a new Chip Authentication.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     */
    public ChipAuthentication(Dispatcher dispatcher, byte[] slotHandle) {
	this.dispatcher = dispatcher;
	this.slotHandle = slotHandle;
    }

    /**
     * Initializes the Chip Authentication protocol.
     * Sends an MSE:Set AT APDU. (Protocol step 1)
     * See BSI-TR-03110, version 2.10, part 3, B.11.1.
     *
     * @param oID Chip Authentication object identifier
     * @param keyID Key identifier
     * @throws ProtocolException
     */
    public void mseSetAT(byte[] oID, byte[] keyID) throws ProtocolException {
	try {
	    CardCommandAPDU mseSetAT = new MSESetATCA(oID, keyID);
	    mseSetAT.transmit(dispatcher, slotHandle);
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Performs a General Authenticate.
     * Sends an General Authenticate APDU. (Protocol step 2)
     * See BSI-TR-03110, version 2.10, part 3, B.11.2.
     *
     * @param key Ephemeral Public Key
     * @return Response APDU
     * @throws ProtocolException
     */
    public byte[] generalAuthenticate(byte[] key) throws ProtocolException {
	try {
	    if (key[0] != (byte) 0x04) {
		key = ByteUtils.concatenate((byte) 0x04, key);
	    }
	    CardCommandAPDU generalAuthenticate = new GeneralAuthenticate((byte) 0x80, key);
	    CardResponseAPDU response = generalAuthenticate.transmit(dispatcher, slotHandle);

	    return response.getData();
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

}
