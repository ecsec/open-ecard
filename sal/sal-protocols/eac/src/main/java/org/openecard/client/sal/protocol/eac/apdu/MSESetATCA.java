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
import org.openecard.client.common.apdu.ManageSecurityEnviroment;
import org.openecard.client.common.apdu.common.CardAPDUOutputStream;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.util.ByteUtils;
import org.slf4j.LoggerFactory;


/**
 * Implements a MSE:Set AT APDU for Chip Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 * See ISO/IEC 7816-4, section 7.5.11.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MSESetATCA extends ManageSecurityEnviroment {

    /**
     * Creates a MSE:Set AT APDU for Chip Authentication.
     */
    public MSESetATCA() {
	super((byte) 0x41, (byte) 0xA4);
    }

    /**
     * Creates a MSE:Set AT APDU for Chip Authentication.
     *
     * @param oid Chip Authentication object identifier
     */
    public MSESetATCA(byte[] oid) {
	this(oid, null);
    }

    /**
     * Creates a MSE:Set AT APDU for Chip Authentication.
     *
     * @param oID Chip Authentication object identifier
     * @param keyID Reference of a private key
     */
    public MSESetATCA(byte[] oID, byte[] keyID) {
	super((byte) 0x41, (byte) 0xA4);

	CardAPDUOutputStream caos = new CardAPDUOutputStream();
	try {
	    caos.writeTLV((byte) 0x80, oID);

	    if (keyID != null) {
		caos.writeTLV((byte) 0x84, ByteUtils.cutLeadingNullBytes(keyID));
	    }

	    caos.flush();
	} catch (IOException ex) {
	    LoggerFactory.getLogger(MSESetATCA.class).error(LoggingConstants.THROWING, "Exception", ex);
	} finally {
	    try {
		caos.close();
	    } catch (IOException ignore) {
	    }
	}

	setData(caos.toByteArray());
    }

}