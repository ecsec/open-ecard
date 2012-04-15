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
 * INTERNAL AUTHENTICATE command
 * See ISO/IEC 7816-4 Section 7.5.2
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class InternalAuthenticate extends CardCommandAPDU {

    /**
     * INTERNAL AUTHENTICATION command instruction byte
     */
    private static final byte INTERNAL_AUTHENTICATE_INS = (byte) 0x88;

    /**
     * Creates a new INTERNAL AUTHENTICATE command.
     * APDU: 0x00 0x88 0x00 0x00
     */
    public InternalAuthenticate() {
	super(x00, INTERNAL_AUTHENTICATE_INS, x00, x00);
    }

    /**
     * Creates a new INTERNAL AUTHENTICATE command.
     * APDU: 0x00 0x88 0x00 0x00 LC DATA
     *
     * @param data Data
     */
    public InternalAuthenticate(byte[] data) {
	super(x00, INTERNAL_AUTHENTICATE_INS, x00, x00);
	setData(data);
    }

}
