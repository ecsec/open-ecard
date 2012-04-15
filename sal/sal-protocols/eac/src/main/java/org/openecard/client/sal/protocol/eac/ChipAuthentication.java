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

import org.openecard.client.common.WSHelper;
import org.openecard.client.common.apdu.GeneralAuthenticate;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.sal.protocol.exception.ProtocolException;
import org.openecard.client.sal.protocol.eac.apdu.MSESetATCA;
import org.openecard.ws.IFD;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ChipAuthentication {

    private IFD ifd;
    private byte[] slotHandle;

    /**
     * Creates a new Chip Authentication.
     *
     * @param ifd IFD
     * @param slotHandle Slot handle
     */
    public ChipAuthentication(IFD ifd, byte[] slotHandle) {
	this.ifd = ifd;
	this.slotHandle = slotHandle;
    }

    /**
     * Initialize Chip Authentication. Sends an MSE:Set AT APDU. (Step 1)
     *
     * @param oid Terminal Authentication object identifier
     * @param key Key identifier
     * @throws ProtocolException
     */
    public void mseSetAT(byte[] oid, byte[] keyID) throws ProtocolException {
	try {
	    CardCommandAPDU mseSetAT = new MSESetATCA(oid, keyID);
	    mseSetAT.transmit(ifd, slotHandle);
	} catch (WSHelper.WSException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Performs a General Authenticate.
     *
     * @param key Ephemeral Public Key
     * @return Response
     * @throws ProtocolException
     */
    public byte[] generalAuthenticate(byte[] key) throws ProtocolException {
	try {
	    CardCommandAPDU generalAuthenticate = new GeneralAuthenticate((byte) 0x80, key);
	    CardResponseAPDU response = generalAuthenticate.transmit(ifd, slotHandle);

	    return response.getData();
	} catch (WSHelper.WSException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

}
