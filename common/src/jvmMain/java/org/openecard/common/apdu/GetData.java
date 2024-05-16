/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 * 
 * This file is part of the Open eCard App.
 * 
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 * 
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 * 
 ***************************************************************************/

package org.openecard.common.apdu;

import org.openecard.common.apdu.common.CardCommandAPDU;


/**
 * GET DATA command
 * See ISO/IEC 7816-4 Section 7.4.2.
 *
 * @author Hans-Martin Haase
 */
public class GetData extends CardCommandAPDU {

    /**
     * GET DATA instruction byte for the case of a complete file dump or card-originated byte strings.
     */
    public static byte INS_DATA = (byte) 0xCA;

    /**
     * GET DATA instruction byte for the request of a tag list data object, header list data object or extended header
     * list data object.
     */
    public static byte INS_TAG_LIST = (byte) 0xCB;

    /**
     * P1 byte for a Simple-TLV tag in P2.
     */
    public static byte SIMPLE_TLV = (byte) 0x02;

    /**
     * P1 byte for a one byte BER-TLV tag in P2.
     */
    public static byte BER_TLV_ONE_BYTE = (byte) 0x00;

    /**
     * Creates a new GET DATA command.
     * This constructor creates a Get Data command apdu according to ISO 7816-4 section 7.4 Table 62.<br>
     * The class provides two public variables for the instruction byte: <br>
     * INS_DATA = 0xCA <br>
     * INS_TAG_LIST = 0xCB <br><br>
     * Furthermore there are two public variables for P1 if P2 encodes a Simple-TLV or an one byte BER-TLV tag:<br>
     * SIMPLE_TLV = 0x02 <br>
     * BER_TLV_ONE_BYTE = 0x00 <br> <br>
     * <br>
     * APDU: 0x00 INS P1 P2 0XFF
     *
     * @param ins Instruction byte for the Get Data command. According to ISO 7816-4 section 7.4.2 table 63 this value is
     * 0xCA or 0xCB.
     * @param p1 see ISO 7816-4 section 7.4.2.
     * @param p2 see ISO 7816-4 section 7.4.2.
     */
    public GetData(byte ins, byte p1, byte p2) {
	super(x00, ins, p1, p2, xFF);
    }
}
