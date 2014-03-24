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
 * WRITE RECORD Command.
 * See ISO/IEC 7816-4 Section 7.3.4
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class WriteRecord extends CardCommandAPDU {

    /**
     * Instruction byte for the WRITE RECORD command.
     */
    private static final byte WRITE_RECORD_INS = (byte) 0xD2;


    /**
     * P2 indicating that the first record shall be written.
     */
    public static final byte WRITE_FIRST = (byte) 0x00;

    /**
     * P2 indicating that the last record shall be written.
     */
    public static final byte WRITE_LAST = (byte) 0x01;

    /**
     * P2 indicating that the next record shall be written.
     */
    public static final byte WRITE_NEXT = (byte) 0x02;

    /**
     * P2 indicating that the previous record shall be written.
     */
    public static final byte WRITE_PREVIOUS = (byte) 0x03;

    /**
     * P2 indicating that the record mentioned in P1 shall be written.
     */
    public static final byte WRITE_P1 = (byte) 0x04;

    /**
     * Creates a new WRITE RECORD APDU.
     * APDU: 0x00 0XD2 P1 P2 Lc Data
     *
     * @param p1 The record number of the record to write. The value is "0x00" if the current record is referenced.
     * @param p2 According to ISO 7816 part 4 Table 52 without short file identifier. There are some public variables
     * for P2 in this class.
     * @param dataToWrite The data to write into the record.
     */
    public WriteRecord(byte p1, byte p2, byte[] dataToWrite) {
	super(x00, WRITE_RECORD_INS, p1, p2, dataToWrite);
    }

    /**
     * Creates a new WRITE RECORD APDU.
     * APDU: 0x00 0XD2 P1 P2 Lc Data<br>
     * This constructor constructs p2 from the short file identifier and the p2Behavior variable.
     *
     * @param p1 The record number of the record to write. The value is "0x00" if the current record is referenced.
     * @param p2Behavior According to ISO 7816 part 4 Table 52 without short file identifier. There are some public
     * variables for P2 in this class.
     * @param shortEfFid Short file identifier for the file which shall contain the new record.
     * @param dataToWrite The data to write.
     */
    public WriteRecord(byte p1, byte p2Behavior, byte shortEfFid, byte[] dataToWrite) {
	super(x00, WRITE_RECORD_INS, p1, (byte) (((byte) shortEfFid * 8) + p2Behavior), dataToWrite);
    }

}
