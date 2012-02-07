/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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
package org.openecard.client.common.util;

/**
 * A set of utility functions for long integers.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class LongUtils {

    /**
     * Convert a long integer to a byte array.
     *
     * @param value long integer to be converted
     * @return byte[]
     */
    public static byte[] toByteArray(long value) {
        return toByteArray(value, 8);
    }

    /**
     * Convert a long integer to a byte array with a given bit size per byte.
     *
     * @param value - long integer to be converted
     * @return byte[]
     */
    public static byte[] toByteArray(long value, int numBits) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must not be negative.");
        }
        if (numBits <= 0 || numBits > 8) {
            throw new IllegalArgumentException("Numbits must be between 0 and 8.");
        }

        if (value == 0) {
            return new byte[1];
        }

        byte[] buffer = null;

        int numBytesInBuffer = 64 / numBits;
        int restBits = 64 - (numBytesInBuffer * numBits);

        int j = 0;
        for (int i = numBytesInBuffer - ((restBits > 0) ? 0 : 1); i >= 0; i--) {
            byte b;
            // first chunk which has uneven number of bits?
            if (i == numBytesInBuffer) {
                byte mask = numBitsToMask((byte) restBits);
                b = (byte) ((byte) (value >> (((i - 1) * numBits) + restBits)) & mask);
            } else {
                byte mask = numBitsToMask((byte) numBits);
                b = (byte) ((value >> (i * numBits)) & mask);
            }

            if (buffer == null && b != 0) {
                buffer = new byte[i + 1];
            } else if (buffer == null) {
                continue;
            }

            buffer[j] = b;
            j++;
        }

        return buffer;
    }

    private static byte numBitsToMask(byte numBits) {
        byte result = 0;
        for (byte i = 0; i < numBits; i++) {
            result = (byte) ((result << 1) | 1);
        }
        return result;
    }

    /**
     * Convert a long integer to a byte array. 
     * If the resulting array contains less bytes than 8 bytes, 0 bytes are prepended if the flag is set.
     *
     * @param value long integer to be converted
     * @param padArrayToTypeLength
     * @return byte[]
     */
    public static byte[] toByteArray(long value, boolean padArrayToTypeLength) {
        byte[] result = toByteArray(value, 8);
        if (padArrayToTypeLength && result.length < 8) {
            result = ByteUtils.concatenate(new byte[8 - result.length], result);
        }
        return result;
    }
}
