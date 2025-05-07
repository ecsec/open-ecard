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
 */
package org.openecard.common.apdu

import org.openecard.common.apdu.common.CardCommandAPDU

/**
 * GET DATA command
 * See ISO/IEC 7816-4 Section 7.4.2.
 *
 * @author Hans-Martin Haase
 */
class GetData
/**
 * Creates a new GET DATA command.
 * This constructor creates a Get Data command apdu according to ISO 7816-4 section 7.4 Table 62.<br></br>
 * The class provides two public variables for the instruction byte: <br></br>
 * INS_DATA = 0xCA <br></br>
 * INS_TAG_LIST = 0xCB <br></br><br></br>
 * Furthermore there are two public variables for P1 if P2 encodes a Simple-TLV or an one byte BER-TLV tag:<br></br>
 * SIMPLE_TLV = 0x02 <br></br>
 * BER_TLV_ONE_BYTE = 0x00 <br></br> <br></br>
 * <br></br>
 * APDU: 0x00 INS P1 P2 0XFF
 *
 * @param ins Instruction byte for the Get Data command. According to ISO 7816-4 section 7.4.2 table 63 this value is
 * 0xCA or 0xCB.
 * @param p1 see ISO 7816-4 section 7.4.2.
 * @param p2 see ISO 7816-4 section 7.4.2.
 */
    (ins: Byte, p1: Byte, p2: Byte) : CardCommandAPDU(x00, ins, p1, p2, xFF) {
    companion object {
        /**
         * GET DATA instruction byte for the case of a complete file dump or card-originated byte strings.
         */
        var INS_DATA: Byte = 0xCA.toByte()

        /**
         * GET DATA instruction byte for the request of a tag list data object, header list data object or extended header
         * list data object.
         */
        var INS_TAG_LIST: Byte = 0xCB.toByte()

        /**
         * P1 byte for a Simple-TLV tag in P2.
         */
        var SIMPLE_TLV: Byte = 0x02.toByte()

        /**
         * P1 byte for a one byte BER-TLV tag in P2.
         */
        var BER_TLV_ONE_BYTE: Byte = 0x00.toByte()
    }
}
