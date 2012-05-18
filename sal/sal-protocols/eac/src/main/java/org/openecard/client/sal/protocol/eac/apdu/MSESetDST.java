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
 * Implements a MSE:Set DST APDU for Terminal Authentication.
 * See BSI-TR-03110, Version 2.05, Section B.11.4.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MSESetDST extends ManageSecurityEnviroment {

    /**
     * Creates a new MSE:Set DST APDU.
     */
    public MSESetDST() {
        super((byte) 0x81, (byte) 0xB6);
    }

    /**
     * Creates a new MSE:Set DST APDU.
     *
     * @param chr Certificate Holder Reference
     */
    public MSESetDST(byte[] chr) {
        super((byte) 0x81, (byte) 0xB6);

        CardAPDUOutputStream caos = new CardAPDUOutputStream();
        try {
            caos.writeTLV((byte) 0x83, chr);

            caos.flush();
        } catch (IOException ex) {
            Logger.getLogger(MSESetDST.class.getName()).log(Level.SEVERE, "Exception", ex);
        } finally {
            try {
                caos.close();
            } catch (IOException ignore) {
            }
        }

        setData(caos.toByteArray());
    }

}
