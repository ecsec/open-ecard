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
 * GET CHALLENGE command
 * See ISO/IEC 7816-4 Section 7.5.3
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GetChallenge extends CardCommandAPDU {

    /**
     * GET CHALLENGE command instruction byte
     */
    private static final byte GET_CHALLENGE_INS = (byte) 0x84;

    /**
     * Creates a new GET CHALLENGE command.
     * APDU: 0x00 0x84 0x00 0x00 0x08
     */
    public GetChallenge() {
	this((byte) 0x08);
    }

    /**
     * Creates a new GET CHALLENGE command.
     * APDU: 0x00 0x84 0x00 0x00 0xLC
     *
     * @param length Expected length of the challenge
     *
     */
    public GetChallenge(byte length) {
	super(x00, GET_CHALLENGE_INS, x00, x00);
	setLE(length);
    }

    /**
     * Creates a new GET CHALLENGE command.
     * APDU: 0x00 0x84 0x00 0x00 0xLC
     *
     * @param length Expected length of the challenge
     */
    public GetChallenge(int length) {
	super(x00, GET_CHALLENGE_INS, x00, x00);
	setLE(length);
    }

}
