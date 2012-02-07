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
package org.openecard.client.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * A set of utility functions for Byte and Byte Array.
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ByteUtils {

    /**
     * Clone a byte array.
     *
     * @param input
     * @return new byte array
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
     * Copy of range.
     *
     * @param input the input
     * @param offset
     * @param length the length
     * @return the byte[]
     */
    public static byte[] cut(byte[] input, int offset, int length) {
        if (input == null) {
            return null;
        }
        byte[] tmp = new byte[input.length - offset - length];
        System.arraycopy(input, length, tmp, offset, tmp.length);
        return tmp;
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
        if (input[0] != (byte) 0x00) {
            return input;
        }

        int i;
        for (i = 1; i < input.length - 1; i++) {
            if (input[i] != (byte) 0x00) {
                break;
            }
        }
        return cut(input, 0, i);
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
            return input;
        }
        return cut(input, 0, 1);
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
            value |= (0xFF & bytes[bytes.length - 1 - i]) << i * 8;
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
}
