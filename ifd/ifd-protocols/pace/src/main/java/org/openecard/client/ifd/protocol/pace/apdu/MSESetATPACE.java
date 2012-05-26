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
package org.openecard.client.ifd.protocol.pace.apdu;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.apdu.ManageSecurityEnviroment;
import org.openecard.client.common.apdu.common.CardAPDUOutputStream;


/**
 * Implements a MSE:Set AT APDU for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 * See ISO/IEC 7816-4, section 7.5.11.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class MSESetATPACE extends ManageSecurityEnviroment {

    /**
     * Creates a new MSE:Set AT APDU for PACE.
     */
    public MSESetATPACE() {
	super((byte) 0xC1, (byte) 0xA4);
    }

    /**
     * Creates a new MSE:Set AT APDU for PACE.
     *
     * @param oID PACE object identifier
     * @param passwordID Password type (PIN, PUK, CAN, MRZ)
     */
    public MSESetATPACE(byte[] oID, byte passwordID) {
	this(oID, passwordID, null);
    }

    /**
     * Creates a new MSE:Set AT APDU for PACE.
     *
     * @param oID PACE object identifier
     * @param passwordID Password type (PIN, PUK, CAN, MRZ)
     * @param chat Certificate Holder Authentication Template
     */
    public MSESetATPACE(byte[] oID, byte passwordID, byte[] chat) {
	super((byte) 0xC1, (byte) 0xA4);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x80, oID);
	    caos.writeTLV((byte) 0x83, passwordID);

	    if (chat != null) {
		caos.write(chat);
	    }

	    caos.flush();
	} catch (IOException ex) {
	    Logger.getLogger(MSESetATPACE.class.getName()).log(Level.SEVERE, "Exception", ex);
	} finally {
	    try {
		caos.close();
	    } catch (IOException ignore) {
	    }
	}

	setData(caos.toByteArray());
    }
}
