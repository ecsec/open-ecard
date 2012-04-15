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
package org.openecard.client.sal.protocol.eac.apdu;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.apdu.ManageSecurityEnviroment;
import org.openecard.client.common.apdu.common.CardAPDUOutputStream;


/**
 * Implements a new MSE:Set AT APDU for Terminal Authentication.
 * See BSI-TR-03110, Version 2.05, Section B.11.1.
 * See ISO/IEC 7816-4, Section 7.5.11.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MSESetATTA extends ManageSecurityEnviroment {

    /**
     * Creates a new MSE:Set AT for Terminal Authentication.
     */
    public MSESetATTA() {
	super((byte) 0x81, (byte) 0xA4);
    }

    /**
     * Creates a new MSE:Set AT for Terminal Authentication.
     *
     * @param oid Terminal Authentication object identifier
     * @param chr Certificate Holder Reference
     * @param pkPCD Ephemeral Public Key
     * @param aad Auxiliary Data Verification
     */
    public MSESetATTA(byte[] oid, byte[] chr, byte[] pkPCD, byte[] aad) {
	super((byte) 0x81, (byte) 0xA4);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x80, oid);

	    if (chr != null) {
		caos.writeTLV((byte) 0x83, chr);
	    }
	    if (pkPCD != null) {
		caos.writeTLV((byte) 0x91, pkPCD);
	    }
	    if (aad != null) {
		caos.write(aad);
	    }

	    caos.flush();
	} catch (IOException ex) {
	    Logger.getLogger(MSESetATTA.class.getName()).log(Level.SEVERE, "Exception", ex);
	} finally {
	    try {
		caos.close();
	    } catch (IOException ignore) {
	    }
	}

	setData(caos.toByteArray());
    }

}
