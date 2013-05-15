/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

import iso.std.iso_iec._24727.tech.schema.Transmit;
import java.util.ArrayList;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.ShortUtils;


/**
 * READ RECORD Command.
 * See ISO/IEC 7816-4 Section 7.3.3
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 */
public class ReadRecord extends CardCommandAPDU {

    /**
     * READ RECORD command instruction byte. INS = 0xB2
     */
    private static final byte READ_RECORD_INS_1 = (byte) 0xB2;

    /**
     * READ RECORD command instruction byte. INS = 0xB3
     */
    private static final byte READ_RECORD_INS_2 = (byte) 0xB3;

    /**
     * Creates an new READ RECORD APDU that reads the first record of the current selected file.
     */
    public ReadRecord() {
	super(x00, READ_RECORD_INS_1, (byte) 0x01, (byte) 0x04, 0xFF);
    }

    /**
     * Creates an new READ RECORD APDU.
     *
     * @param recordNumber Number of the record
     */
    public ReadRecord(byte recordNumber) {
	super(x00, READ_RECORD_INS_1, recordNumber, (byte) 0x04, 0xFF);
    }

    /**
     * Creates an new READ RECORD APDU.
     * Bits 8 to 4 of P2 encode a short EF identifier.
     *
     * @param shortFileID Short file identifier
     * @param recordNumber Number of the record
     */
    public ReadRecord(byte shortFileID, byte recordNumber) {
	super(x00, READ_RECORD_INS_1, recordNumber, (byte) ((shortFileID << 3) | 0x04), 0xFF);
    }

    /**
     * Creates an new READ RECORD APDU.
     * The offset data object with tag '54' is encoded in the command data field.
     *
     * @param recordNumber Number of the record
     * @param offset Offset
     * @param length Expected length
     */
    public ReadRecord(byte recordNumber, short offset, short length) {
	super(x00, READ_RECORD_INS_2, recordNumber, (byte) 0x04);

	byte[] content = ShortUtils.toByteArray(offset);
	content = ByteUtils.concatenate((byte) 0x54, content);

	setData(content);
	setLE(length);
    }

    /**
     * Creates an new READ RECORD APDU.
     * Bits 8 to 4 of P2 encode a short EF identifier.
     * The offset data object with tag '54' is encoded in the command data field.
     *
     * @param shortFileID Short file identifier
     * @param recordNumber Number of the record
     * @param offset Offset
     * @param length Expected length
     */
    public ReadRecord(byte shortFileID, byte recordNumber, short offset, short length) {
	super(x00, READ_RECORD_INS_2, recordNumber, (byte) ((shortFileID << 3) | 0x04));

	byte[] content = ShortUtils.toByteArray(offset);
	content = ByteUtils.concatenate((byte) 0x54, content);

	setData(content);
	setLE(length);
    }

    /**
     * Creates a new Transmit message.
     *
     * @param slotHandle Slot handle
     * @return Transmit
     */
    @Override
    public Transmit makeTransmit(byte[] slotHandle) {
	ArrayList<byte[]> defaultResponses = new ArrayList<byte[]>() {
	    {
		add(new byte[]{(byte) 0x90, (byte) 0x00});
		add(new byte[]{(byte) 0x62, (byte) 0x82});
	    }
	};
	return makeTransmit(slotHandle, defaultResponses);
    }

}
