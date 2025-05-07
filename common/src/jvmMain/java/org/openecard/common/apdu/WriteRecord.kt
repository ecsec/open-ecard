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
 * WRITE RECORD Command.
 * See ISO/IEC 7816-4 Section 7.3.4
 *
 * @author Hans-Martin Haase
 */
class WriteRecord : CardCommandAPDU {
    /**
     * Creates a new WRITE RECORD APDU.
     * APDU: 0x00 0XD2 P1 P2 Lc Data
     *
     * @param p1 The record number of the record to write. The value is "0x00" if the current record is referenced.
     * @param p2 According to ISO 7816 part 4 Table 52 without short file identifier. There are some public variables
     * for P2 in this class.
     * @param dataToWrite The data to write into the record.
     */
    constructor(p1: Byte, p2: Byte, dataToWrite: ByteArray) : super(
        x00,
        WRITE_RECORD_INS,
        p1,
        p2,
        dataToWrite
    )

    /**
     * Creates a new WRITE RECORD APDU.
     * APDU: 0x00 0XD2 P1 P2 Lc Data<br></br>
     * This constructor constructs p2 from the short file identifier and the p2Behavior variable.
     *
     * @param p1 The record number of the record to write. The value is "0x00" if the current record is referenced.
     * @param p2Behavior According to ISO 7816 part 4 Table 52 without short file identifier. There are some public
     * variables for P2 in this class.
     * @param shortEfFid Short file identifier for the file which shall contain the new record.
     * @param dataToWrite The data to write.
     */
    constructor(p1: Byte, p2Behavior: Byte, shortEfFid: Byte, dataToWrite: ByteArray) : super(
        x00,
        WRITE_RECORD_INS,
        p1,
        ((shortEfFid * 8) + p2Behavior).toByte(),
        dataToWrite
    )

    companion object {
        /**
         * Instruction byte for the WRITE RECORD command.
         */
        private const val WRITE_RECORD_INS = 0xD2.toByte()


        /**
         * P2 indicating that the first record shall be written.
         */
        const val WRITE_FIRST: Byte = 0x00.toByte()

        /**
         * P2 indicating that the last record shall be written.
         */
        const val WRITE_LAST: Byte = 0x01.toByte()

        /**
         * P2 indicating that the next record shall be written.
         */
        const val WRITE_NEXT: Byte = 0x02.toByte()

        /**
         * P2 indicating that the previous record shall be written.
         */
        const val WRITE_PREVIOUS: Byte = 0x03.toByte()

        /**
         * P2 indicating that the record mentioned in P1 shall be written.
         */
        const val WRITE_P1: Byte = 0x04.toByte()
    }
}
