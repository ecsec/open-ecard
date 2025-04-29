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

import org.openecard.common.apdu.common.CardAPDU
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.*

/**
 * ERASE BINARY command.
 * See ISO/IEC 7816-4 Section 7.2.7.
 *
 * @author Hans-Martin Haase
 */
class EraseBinary : CardCommandAPDU {
    /**
     * Creates a new ERASE BINARY command.
     * APDU: 0x00 0E P1 P2 Lc offset
     *
     * @param p1 P1 according to ISO 7816 part 4 section 7.2.2.
     * @param p2 P2 according to ISO 7816 part 4 section 7.2.2.
     * @param offset Offset of the first data unit not to be erased. This value shall be higher than the on encoded in
     * P1P2.
     */
    constructor(p1: Byte, p2: Byte, offset: ByteArray) : super(CardAPDU.Companion.x00, WITH_OFFSET, p1, p2, offset)

    /**
     * Creates a new ERASE BINARY command.
     *
     * @param p1 P1 according to ISO 7816 part 4 section 7.2.2.
     * @param p2 P2 according to ISO 7816 part 4 section 7.2.2.
     * @param offsetDataObjects A list of TLVs representing OffsetDataObjects. The max size of the list is two.
     * @throws APDUException Thrown if the offsetDataObjects list contains more than two objects or if one of the objects
     * does not start with tag 0x54.
     * @throws TLVException Thrown if the TLVs from the offsetDataObjects list can't be converted into a byte array.
     */
    constructor(p1: Byte, p2: Byte, offsetDataObjects: List<TLV>) {
        if (offsetDataObjects.size > 2) {
            throw APDUException("The maximum number of offset data objects for a ERASE BINARY command is 2.")
        }

        var dataField: ByteArray? = ByteArray(0)
        for (offSetObj in offsetDataObjects) {
            if (offSetObj.tagNumWithClass != 0x54L) {
                throw APDUException("The offset data object does not start with tag 0x54")
            } else {
                dataField = ByteUtils.concatenate(dataField, offSetObj.toBER())
            }
        }

        ins = WITH_OFFSET_DATA_OBJECT
        setP1(p1)
        setP2(p2)
        cla = CardAPDU.Companion.x00
        setData(dataField!!)
        lc = dataField.size
    }

    companion object {
        /**
         * Instruction byte for the ERASE BINARY command which indicates the data field contains a offset.
         */
        private const val WITH_OFFSET = 0x0E.toByte()

        /**
         * Instruction byte for the ERASE BINARY command which indicates that the data field contains zero, one or two DATA OFFSET OBJECTS (Tag 54).
         */
        private const val WITH_OFFSET_DATA_OBJECT = 0x0F.toByte()
    }
}
