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

package org.openecard.common.apdu.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.openecard.common.util.IntegerUtils;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CardAPDUOutputStream extends ByteArrayOutputStream {

    /**
     * Creates a new byte array stream for APDUs.
     */
    public CardAPDUOutputStream() {
	super();
    }

    /**
     * Creates a new byte array stream for APDUs, with a buffer capacity of the specified size.
     *
     * @param size Initial size.
     */
    public CardAPDUOutputStream(int size) {
	super(size);
    }

    /**
     * Writes TLV encoded data to the stream.
     *
     * @param type Type
     * @param value Value
     * @throws IOException if an I/O error occurs
     */
    public void writeTLV(byte type, byte value) throws IOException {
	write(type);
	write((byte) 0x01);
	write(value);
    }

    /**
     * Writes TLV encoded data to the stream.
     *
     * @param type Type
     * @param value Value
     * @throws IOException if an I/O error occurs
     */
    public void writeTLV(byte type, byte[] value) throws IOException {
	final int length = value.length;

	write(type);
	if (length > 0x7F && length <= 0xFF) {
	    write((byte) 0x81);
	} else if (length == 0xFF) {
	    write((byte) 0x00);
	} else if (length > 0xFF) {
	    write((byte) 0x82);
	}
	write(IntegerUtils.toByteArray(length));
	write(value);
    }

}
