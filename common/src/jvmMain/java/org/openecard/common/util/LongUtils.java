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

package org.openecard.common.util;


/**
 * A set of utility functions for long integers.
 *
 * @author Tobias Wich
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
     * @param numBits Number of bits to use per byte.
     * @return byte[]
     */
    public static byte[] toByteArray(long value, int numBits) {
	return toByteArray(value, numBits, true);
    }
    /**
     * Convert a long integer to a byte array with a given bit size per byte.
     *
     * @param value Long integer to be converted.
     * @param numBits Number of bits to use per byte.
     * @param bigEndian {@code true} when output should be in Big Endian, {@code false} for Little Endian.
     * @return byte[]
     * @throws IllegalArgumentException Thrown in case the numBits value is not within the permitted range.
     */
    public static byte[] toByteArray(long value, int numBits, boolean bigEndian) {
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

	// when emitting little endian, reverse the array
	if (! bigEndian) {
	    buffer = ByteUtils.reverse(buffer);
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
	return toByteArray(value, padArrayToTypeLength, true);
    }
    /**
     * Convert a long integer to a byte array.
     * If the resulting array contains less bytes than 8 bytes, 0 bytes are prepended if the flag is set.
     *
     * @param value long integer to be converted
     * @param padArrayToTypeLength
     * @param bigEndian {@code true} when output should be in Big Endian, {@code false} for Little Endian.
     * @return byte[]
     */
    public static byte[] toByteArray(long value, boolean padArrayToTypeLength, boolean bigEndian) {
	byte[] result = toByteArray(value, 8, bigEndian);
	if (padArrayToTypeLength && result.length < 8) {
	    result = ByteUtils.concatenate(new byte[8 - result.length], result);
	}
	return result;
    }

}
