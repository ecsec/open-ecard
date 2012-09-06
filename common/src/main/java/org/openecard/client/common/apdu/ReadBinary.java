/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.common.apdu;

import iso.std.iso_iec._24727.tech.schema.Transmit;
import java.util.ArrayList;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.ShortUtils;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ReadBinary extends CardCommandAPDU {

    /**
     * READ BINARY command instruction byte. INS = 0xB0
     */
    private static final byte READ_BINARY_INS_1 = (byte) 0xB0;
    /**
     * READ BINARY command instruction byte. INS = 0xB1
     */
    private static final byte READ_BINARY_INS_2 = (byte) 0xB1;

    /**
     * Creates an new READ BINARY APDU.
     */
    public ReadBinary() {
	super(x00, READ_BINARY_INS_1, x00, xFF);
    }

    /**
     * Creates an new READ BINARY APDU.
     *
     * @param offset Offset
     */
    public ReadBinary(byte offset) {
	super(x00, READ_BINARY_INS_1, offset, xFF);
    }

    /**
     * Creates an new READ BINARY APDU.
     *
     * @param offset Offset
     * @param length Expected length
     */
    public ReadBinary(byte offset, byte length) {
	super(x00, READ_BINARY_INS_1, offset, length);
    }

    /**
     * Creates an new READ BINARY APDU.
     * Bit 8 of P1 is set to 1, bits 7 and 6 of P1 are set to 00 (RFU),
     * bits 5 to 1 of P1 encode a short EF identifier and P2 (eight bits)
     * encodes an offset from zero to 255.
     *
     * @param shortFileID Short file identifier
     * @param offset Offset
     * @param length Expected length
     */
    public ReadBinary(byte shortFileID, byte offset, short length) {
	super(x00, READ_BINARY_INS_1, (byte) ((0x1F & shortFileID) | 0x80), offset);
	setLE(length);
    }

    /**
     * Creates an new READ BINARY APDU.
     * Bit 8 of P1 is set to 0, and P1-P2 (fifteen bits) encodes an
     * offset from zero to 32 767.
     *
     * @param offset Offset
     * @param length Expected length
     */
    public ReadBinary(short offset, byte length) {
	setINS(READ_BINARY_INS_1);
	setP1((byte) (((byte) (offset >> 8)) & 0x7F));
	setP2((byte) (offset & xFF));
	setLE(length);
    }

    /**
     * Creates an new READ BINARY APDU.
     * P1-P2 set to '0000' identifies the current EF.
     * The offset data object with tag '54' is encoded in the command data field.
     *
     * @param fileID File identifier
     * @param offset Offset
     * @param length Expected length
     */
    public ReadBinary(short fileID, short offset, short length) {
	super(x00, READ_BINARY_INS_2, x00, x00);

	byte[] content = ShortUtils.toByteArray(offset);
	content = ByteUtils.concatenate((byte) 0x54, content);

	setP1P2(ShortUtils.toByteArray(fileID));
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
