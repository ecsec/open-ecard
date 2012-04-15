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
package org.openecard.client.common.apdu.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.openecard.client.common.util.IntegerUtils;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
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
