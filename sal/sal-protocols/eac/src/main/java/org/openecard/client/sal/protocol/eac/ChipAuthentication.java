/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
