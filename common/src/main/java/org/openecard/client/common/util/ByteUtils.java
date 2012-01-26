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
     * Convert a byte array to a hex string.
     *
     * @param bytes
     * @return hex encoded string
     */
    public static String formatHexString(byte[] bytes) {
        return formatHexString(bytes, false);
    }

    /**
     * Convert a byte array to a hex string and formated the output to 16 values in a row if addLinebreak is set.
     *
     * @param bytes
     * @param addLinebreak
     * @return hex encoded string
     */
    public static String formatHexString(byte[] bytes, boolean addLinebreak) {
        if (bytes == null) {
            return "";
        } else if (bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int cBytes = bytes.length;
        int iByte = 0;
        while (true) {
            for (int i = 0; i < 16; i++) {
                String hex = Integer.toHexString(bytes[iByte++] & 0xff);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                sb.append("0x").append(hex.toUpperCase()).append(" ");

                if (iByte >= cBytes) {
                    return sb.toString();
                }
            }
            if (addLinebreak) {
                sb.append("\n");
            }
        }
    }


    /**
     * Convert a byte array to a hex string suitable for use as XML's hexBinary type.
     *
     * @param bytes
     * @return Hex string soley compose of digits, no 0x and no spaces.
     */
    public static String toHexString(byte[] bytes) {
	StringWriter writer = new StringWriter(bytes.length * 2);
	PrintWriter out = new PrintWriter(writer);

	for (int i = 0; i < bytes.length; i++) {
	    out.printf("%02X", bytes[i]);
	}

	return writer.toString();
    }

}
