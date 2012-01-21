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
package org.openecard.client.crypto.common.asn1.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
@Deprecated
public class TLV {

    private byte[] type;
    private byte[] value;
    private int length;
    private boolean constructed;

    /**
     * Instantiates a new tLV.
     *
     * @param encoded the encoded
     */
    protected TLV(byte[] encoded) {
        /*
         * Decode type
         */
        // B6=1 introduces a constructed data object
        if ((encoded[0] & 0x20) == 0x20) {
            constructed = true;
        }

        // B5-B1 set to 1 in the leading byte
        if ((encoded[0] & 0x1F) == 0x1F) {
            int i = 0;
            do {
                i++;
            } while ((encoded[i] & 0x80) == 0x80);

            type = new byte[i + 1];
            System.arraycopy(encoded, 0, type, 0, type.length);
        }

        /*
         * Decode length
         */
        if ((encoded[type.length] & 0x7F) == 0x7F) {
            length = encoded[type.length] & 0xFF;
        } else if ((encoded[type.length] & 0x81) == 0x81) {
            length = encoded[type.length + 1] & 0xFF;

        } else if ((encoded[type.length] & 0x82) == 0x82) {
            length = (encoded[type.length + 1] & 0xFF) << 8;
            length += encoded[type.length + 2] & 0xFF;
        }

        value = new byte[length];
        System.arraycopy(encoded, encoded.length - length, value, 0, length);
    }

    /**
     * Constructs a new TLV object.
     * 
     * @param type Type
     * @param value Value
     */
    public TLV(byte type, byte value) {
        this(new byte[]{type}, new byte[]{value});
    }

    /**
     * Constructs a new TLV object.
     * 
     * @param type Type
     * @param value Value
     */
    public TLV(byte type, byte[] value) {
        this(new byte[]{type}, value);
    }

    /**
     * Constructs a new TLV object.
     * 
     * @param type Type
     * @param value Value
     */
    public TLV(byte[] type, byte value) {
        this(type, new byte[]{value});
    }

    /**
     * Constructs a new TLV object.
     * 
     * @param type Type
     * @param value Value
     */
    public TLV(byte[] type, byte[] value) {
        this.type = type;
        this.value = value;
        this.length = value.length;
    }

    /**
     * Returns the value.
     *
     * @return Value
     */
    public byte[] getValue() {
        return this.value;
    }

    /**
     * Returns the type.
     *
     * @return Type
     */
    public byte[] getType() {
        return this.type;
    }

    /**
     * Checks if is constructed.
     *
     * @return true, if is constructed
     */
    public boolean isConstructed() {
        return constructed;
    }

    /**
     * Creates a TLV object and converts it to a byte array.
     *
     * @param type Type
     * @param value Value
     * @return Encoded TLV object
     */
    public static byte[] encode(byte type, byte value) {
        return new TLV(type, value).encode();
    }

    /**
     * Creates a TLV object and converts it to a byte array.
     *
     * @param type Type
     * @param value Value
     * @return Encoded TLV object
     */
    public static byte[] encode(byte type, byte[] value) {
        return new TLV(type, value).encode();
    }

    /**
     * Creates a TLV object and converts it to a byte array.
     *
     * @param type Type
     * @param value Value
     * @return Encoded TLV object
     */
    public static byte[] encode(byte[] type, byte value) {
        return new TLV(type, value).encode();
    }

    /**
     * Creates a TLV object and converts it to a byte array.
     *
     * @param type Type
     * @param value Value
     * @return Encoded TLV object
     */
    public static byte[] encode(byte[] type, byte[] value) {
        return new TLV(type, value).encode();
    }

    /**
     * Converts the TLV to a byte array.
     *
     * @return Encoded TLV object
     */
    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length + 3);

        try {
            baos.write(type);
            if (length > 0x7F && length <= 0xFF) {
                baos.write((byte) 0x81);
            } else if (length == 0xFF) {
                baos.write((byte) 0x00);
            } else if (length > 0xFF) {
                baos.write((byte) 0x82);
                baos.write((byte) ((length >> 8) & 0xFF));
            }
            baos.write(length & 0xFF);
            baos.write(value);
        } catch (IOException e) {
            Logger.getLogger("TLV").log(Level.SEVERE, "Exception", e);
        }

        return baos.toByteArray();
    }

    /**
     * Decode.
     *
     * @param encoded the encoded
     * @return the tLV
     */
    public static TLV decode(byte[] encoded) {
        return new TLV(encoded);
    }
}
