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
package org.openecard.client.common.apdu;

import org.openecard.client.common.apdu.common.CardCommandAPDU;


/**
 * EXTERNAL AUTHENTICATION Command
 * See ISO/IEC 7816-4 Section 7.5.4
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ExternalAuthentication extends CardCommandAPDU {

    /**
     * EXTERNAL AUTHENTICATION command instruction byte
     */
    private static final byte EXTERNAL_AUTHENTICATION_INS = (byte) 0x82;

    /**
     * Creates a new External Authenticate APDU.
     * APDU: 0x00 0x82 0x00 0x00 LC DATA
     *
     * @param data Data
     */
    public ExternalAuthentication(byte[] data) {
	super(x00, EXTERNAL_AUTHENTICATION_INS, x00, x00);
	setData(data);
    }

}
