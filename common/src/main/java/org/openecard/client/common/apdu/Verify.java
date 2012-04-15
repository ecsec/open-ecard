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
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Verify extends CardCommandAPDU {

    /**
     * VERIFY command instruction byte
     */
    private static final byte VERIFY_INS_1 = (byte) 0x20;
    private static final byte VERIFY_INS_2 = (byte) 0x21;

    /**
     * Creates a new VERIFY APDU.
     */
//    public Verify(byte p1, byte p2, byte[] data) {
//        super(x00, VERIFY_INS, p1, p2);
//        setData(data);
//    }

}
