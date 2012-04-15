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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.tlv.TLV;


/**
 * GENERAL AUTHENTICATION Command
 * See ISO/IEC 7816-4 Section 7.5.2
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GeneralAuthenticate extends CardCommandAPDU {

    /**
     * GENERAL AUTHENTICATION command instruction byte
     */
    private static final byte GENERAL_AUTHENTICATION_INS = (byte) 0x86;

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x00 0x86 0x00 0x00 0x02 0x7C 0x00 0x00
     */
    public GeneralAuthenticate() {
	super(x00, GENERAL_AUTHENTICATION_INS, x00, x00);
	setLC((byte) 0x02);
	setData(new byte[]{(byte) 0x7C, x00});
	setLE(x00);
    }

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x10 0x86 0x00 0x00 LC 0x7C DATA 0x00
     *
     * @param data Data
     */
    public GeneralAuthenticate(byte[] data) {
	super(x00, GENERAL_AUTHENTICATION_INS, x00, x00);
	setData(data);
	setLE(x00);
    }

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x00 0x86 0x00 0x00 LC 0x7C DATA 0x00
     * Tag should be one of:
     * '80' Witness
     * '81' Challenge
     * '82' Response
     * '83' Committed challenge
     * '84' Authentication code
     * '85' Exponential
     * 'A0' Identification data template
     *
     * @param tag Authentication data tag. 0x7C is omitted!
     * @param data Authentication data objects
     */
    public GeneralAuthenticate(byte tag, byte[] authData) {
	super(x00, GENERAL_AUTHENTICATION_INS, x00, x00);

	try {
	    TLV tag7c = new TLV();
	    TLV tagData = new TLV();

	    tag7c.setTagNumWithClass((byte) 0x7C);
	    tag7c.setChild(tagData);
	    tagData.setTagNumWithClass((byte) tag);
	    tagData.setValue(authData);

	    setData(tag7c.toBER());
	} catch (Exception ex) {
	    Logger.getLogger(GeneralAuthenticate.class.getName()).log(Level.SEVERE, "Exception", ex);
	}

	setLE(x00);
    }

}
