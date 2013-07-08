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

import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.ShortUtils;


/**
 * UPDATE RECORD Command.
 * See ISO/IEC 7816-4 Section 7.3.5
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 */
public class UpdateRecord extends CardCommandAPDU {

    /**
     * UPDATE RECORD command instruction byte. INS = 0xDC
     */
    private static final byte UPDATE_RECORD_INS_1 = (byte) 0xDC;

    /**
     * UPDATE RECORD command instruction byte. INS = 0xDD
     */
    private static final byte UPDATE_RECORD_INS_2 = (byte) 0xDD;

    /**
     * Creates an new UPDATE RECORD APDU that updates the first record of the current selected file.
     *
     * @param updatingData update data for the record
     */
    public UpdateRecord(byte[] updatingData) {
	super(x00, UPDATE_RECORD_INS_1, (byte) 0x01, (byte) 0x04);
	setData(updatingData);
    }

    /**
     * Creates an new UPDATE RECORD APDU.
     *
     * @param recordNumber Number of the record
     * @param updatingData update data for the record
     */
    public UpdateRecord(byte recordNumber, byte[] updatingData) {
	super(x00, UPDATE_RECORD_INS_1, recordNumber, (byte) 0x04);
	setData(updatingData);
    }

    /**
     * Creates an new UPDATE RECORD APDU.
     * Bits 8 to 4 of P2 encode a short EF identifier.
     *
     * @param shortFileID Short file identifier
     * @param recordNumber Number of the record
     * @param updatingData update data for the record
     */
    public UpdateRecord(byte shortFileID, byte recordNumber, byte[] updatingData) {
	super(x00, UPDATE_RECORD_INS_1, recordNumber, (byte) ((shortFileID << 3) | 0x04));
	setData(updatingData);
    }

    /**
     * Creates an new UPDATE RECORD APDU.
     * The offset data object with tag '54' and
     * the update data object with tag '53' is encoded in the command data field.
     *
     * @param recordNumber Number of the record
     * @param offset Offset
     * @param updatingData update data for the record
     */
    public UpdateRecord(byte recordNumber, short offset, byte[] updatingData) {
	super(x00, UPDATE_RECORD_INS_2, recordNumber, (byte) 0x04);

	byte[] offsetDataObject = ShortUtils.toByteArray(offset);
	offsetDataObject = ByteUtils.concatenate((byte) 0x54, offsetDataObject);
	byte[] updatingDataObject =  ByteUtils.concatenate((byte) 0x53, updatingData);

	byte[] content = ByteUtils.concatenate(offsetDataObject, updatingDataObject);

	setData(content);
    }

    /**
     * Creates an new UPDATE RECORD APDU.
     * Bits 8 to 4 of P2 encode a short EF identifier.
     * The offset data object with tag '54' and
     * the update data object with tag '53' is encoded in the command data field.
     *
     * @param shortFileID Short file identifier
     * @param recordNumber Number of the record
     * @param offset Offset
     * @param updatingData update data for the record
     */
    public UpdateRecord(byte shortFileID, byte recordNumber, short offset, byte[] updatingData) {
	super(x00, UPDATE_RECORD_INS_2, recordNumber, (byte) ((shortFileID << 3) | 0x04));

	byte[] offsetDataObject = ShortUtils.toByteArray(offset);
	offsetDataObject = ByteUtils.concatenate((byte) 0x54, offsetDataObject);
	byte[] updatingDataObject =  ByteUtils.concatenate((byte) 0x53, updatingData);

	byte[] content = ByteUtils.concatenate(offsetDataObject, updatingDataObject);

	setData(content);
    }

}
