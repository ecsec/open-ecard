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

package org.openecard.client.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * A set of utility functions for Byte and Byte Array.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ByteUtils {

    /**
     * Clone a byte array.
     *
     * @param input the byte array to clone
     * @return new byte array, or null if input is null
     */
    public static byte[] clone(byte[] input) {
	if (input == null) {
	    return null;
	}
	byte[] ret = new byte[input.length];
	System.arraycopy(input, 0, ret, 0, input.length);
	return ret;
    }

    /**
     * Concatenate a byte array.
     *
     * @param x byte array
     * @param y byte array
     * @return x || y
     */
    public static byte[] concatenate(byte[] x, byte[] y) {
	if (x == null) {
	    return y;
	}
	if (y == null) {
	    return x;
	}
	byte[] ret = new byte[x.length + y.length];
	System.arraycopy(x, 0, ret, 0, x.length);
	System.arraycopy(y, 0, ret, x.length, y.length);
	return ret;
    }

    /**
     * Concatenate a byte array.
     *
     * @param x byte
     * @param y byte array
     * @return x || y
     */
    public static byte[] concatenate(byte x, byte[] y) {
	return concatenate(new byte[]{x}, y);
    }

    /**
     * Concatenate a byte array.
     *
     * @param x byte array
     * @param y byte
     * @return x || y
     */
    public static byte[] concatenate(byte[] x, byte y) {
	return concatenate(x, new byte[]{y});
    }

    /**
     * Concatenate a byte array.
     *
     * @param x byte
     * @param y byte
     * @return x || y
     */
    public static byte[] concatenate(byte x, byte y) {
	return concatenate(new byte[]{x}, new byte[]{y});
    }

    /**
     * Cut leading null bytes of a byte array.
     *
     * @param input
     * @return byte array without leading null bytes
     */
    public static byte[] cutLeadingNullBytes(byte[] input) {
	if (input == null) {
	    return null;
	}

	int i;
	for (i = 0; i < input.length - 1; i++) {
	    if (input[i] != (byte) 0x00) {
		break;
	    }
	}
	return copy(input, i, input.length - i);
    }

    /**
     * Removes leading null byte from the input byte array.
     *
     * @param input Byte array
     * @return byte array without leading null bytes
     */
    public static byte[] cutLeadingNullByte(byte[] input) {
	if (input == null) {
	    return null;
	}
	if (input[0] != (byte) 0x00) {
	    return ByteUtils.clone(input);
	}
	return copy(input, 1, input.length - 1);
    }

    /**
     * Copy of range.
     *
     * @param input the input
     * @param offset
     * @param length the length
     * @return the byte[]
     */
    public static byte[] copy(byte[] input, int offset, int length) {
	if (input == null) {
	    return null;
	}
	byte[] tmp = new byte[length];
	System.arraycopy(input, offset, tmp, 0, length);
	return tmp;
    }

    /**
     * Compare two byte arrays.
     *
     * @param x byte array
     * @param y byte array
     * @return true if x = y, otherwise false
     */
    public static boolean compare(byte[] x, byte[] y) {
	if (y == null) {
	    return false;
	} else if (x == null) {
	    return false;
	}
	if (x.length != y.length) {
	    return false;
	}
	for (int i = 0; i < x.length; i++) {
	    if (x[i] != y[i]) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Compare two byte arrays.
     *
     * @param x byte
     * @param y byte array
     * @return true if x = y, otherwise false
     */
    public static boolean compare(byte x, byte[] y) {
	return compare(new byte[]{x}, y);
    }

    /**
     * Compare two byte arrays.
     *
     * @param x byte array
     * @param y byte
     * @return true if x = y, otherwise false
     */
    public static boolean compare(byte[] x, byte y) {
	return compare(x, new byte[]{y});
    }

    /**
     * Compare two bytes.
     *
     * @param x byte
     * @param y byte
     * @return true if x = y, otherwise false
     */
    public static boolean compare(byte x, byte y) {
	return compare(new byte[]{x}, new byte[]{y});
    }

    /**
     * Convert a byte array to a hex string suitable for use as XML's hexBinary type.
     *
     * @param bytes Input
     * @return Hex string only compose of digits, no 0x and no spaces.
     */
    public static String toHexString(byte[] bytes) {
	return toHexString(bytes, "%02X", false);
    }

    /**
     * Convert a byte array to a hex string.
     *
     * @param bytes Input
     * @param formatted If true the string is formatted to 0xXX presentation
     * @return Hex string
     */
    public static String toHexString(byte[] bytes, boolean formatted) {
	return toHexString(bytes, formatted, false);
    }

    /**
     * Convert a byte array to a hex string.
     *
     * @param bytes Input
     * @param formatted If true the string is formatted to 0xXX presentation
     * @param addLinebreak If true the string is formatted to 16 value per line
     * @return Hex string
     */
    public static String toHexString(byte[] bytes, boolean formatted, boolean addLinebreak) {
	if (formatted) {
	    return toHexString(bytes, "0x%02X ", addLinebreak);
	} else {
	    return toHexString(bytes, "%02X", addLinebreak);
	}
    }

    private static String toHexString(byte[] bytes, String format, boolean addLinebreak) {
	if (bytes == null) {
	    return null;
	}

	StringWriter writer = new StringWriter(bytes.length * 2);
	PrintWriter out = new PrintWriter(writer);

	for (int i = 1; i <= bytes.length; i++) {
	    out.printf(format, bytes[i - 1]);
	    if (addLinebreak) {
		if (i % 16 == 0) {
		    out.append("\n");
		}
	    }
	}

	return writer.toString();
    }

    /**
     * Convert a byte array to an integer.<br/> Size of byte array must be between 1 and 4.
     *
     * @param bytes byte array to be converted
     * @return int
     */
    public static int toInteger(byte[] bytes) {
	if (bytes.length > 4 || bytes.length < 1) {
	    throw new IllegalArgumentException("Size of byte array must be between 1 and 4.");
	}

	return (int) toLong(bytes);
    }

    /**
     * Convert a byte array to a long integer.<br/> Size of byte array must be between 1 and 8.
     *
     * @param bytes byte array to be converted
     * @return long
     */
    public static long toLong(byte[] bytes) {
	if (bytes.length > 8 || bytes.length < 1) {
	    throw new IllegalArgumentException("Size of byte array must be between 1 and 8.");
	}

	long value = 0;

	for (int i = 0; i < bytes.length; i++) {
	    value |= ((long) 0xFF & bytes[bytes.length - 1 - i]) << i * 8;
	}

	return value;
    }

    /**
     * Convert a byte array to a short integer.<br/> Size of byte array must be between 1 and 2.
     *
     * @param bytes byte array to be converted
     * @return short
     */
    public static short toShort(byte[] bytes) {
	if (bytes.length > 2 || bytes.length < 1) {
	    throw new IllegalArgumentException("Size of byte array must be between 1 and 2.");
	}

	return (short) toLong(bytes);
    }

    /**
     * Checks if a bit in the array is set or not.
     *
     * @param position Position in array
     * @param array Array
     * @return True if the bit is set, false otherwise
     * @throws IllegalArgumentException if position is negative or greater than the number of bits in this array
     */
    public static boolean isBitSet(int position, byte[] array) throws IllegalArgumentException {
	if (position < 0 || position >= array.length * 8) {
	    throw new IllegalArgumentException("Position is invalid");
	}
	return ((array[position / 8] & (128 >> (position % 8))) > 0);
    }

    /**
     * Sets the bit in the array.
     *
     * @param position Position
     * @param array Array
     * @throws IllegalArgumentException if position is negative or greater than the number of bits in this array
     */
    public static void setBit(int position, byte[] array) throws IllegalArgumentException {
	if (position < 0 || position >= array.length * 8) {
	    throw new IllegalArgumentException("Position is invalid");
	}
	array[position / 8] |= (128 >> (position % 8));
    }

}
