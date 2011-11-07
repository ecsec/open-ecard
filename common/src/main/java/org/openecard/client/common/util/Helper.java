package org.openecard.client.common.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;


/**
 * Simple Helper class.
 * @author Johannes.Schmoelz, Tobias Wich <tobias.wich@ecsec.de>
 */
public class Helper {

    /**
     * Concatenates two byte arrays.
     * @param b1 - first byte array
     * @param b2 - second byte array
     * @return byte[]
     */
    public static byte[] concatenate(byte[] b1, byte[] b2) {
        byte[] result = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, result, 0, b1.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);
        return result;
    }

    /**
     * Concatenates a byte array and one byte.
     * @param b1 - byte array
     * @param b2 - byte
     * @return byte[]
     */
    public static byte[] concatenate(byte[] b1, byte b2) {
	byte[] result = new byte[b1.length + 1];
	System.arraycopy(b1, 0, result, 0, b1.length);
	result[b1.length] = b2;
	return result;
    }

    /**
     * Concatenates one byte and a byte array.
     * @param b1 - byte
     * @param b2 - byte array
     * @return byte[]
     */
    public static byte[] concatenate(byte b1, byte[] b2) {
        byte[] result = new byte[b2.length + 1];
        result[0] = b1;
        System.arraycopy(b2, 0, result, 1, b2.length);
        return result;
    }

    /**
     * Converts a byte array to an integer. Size of byte array must be between 1 and 4.
     * @param buffer - byte array to be converted
     * @return int
     */
    public static int convertByteArrayToInt(byte[] buffer) {
	if (buffer.length > 4 || buffer.length < 1) {
	    throw new IllegalArgumentException("Size of byte array must be between 1 and 4.");
	}

	int value = 0;

	for(int i = 0; i < buffer.length; i++) {
	    value |= (0xFF & buffer[buffer.length - 1 - i]) << i * 8;
	}

	return value;
    }
    /**
     * Converts a byte array to a long integer. Size of byte array must be between 1 and 8.
     * @param buffer - byte array to be converted
     * @return int
     */
    public static long convertByteArrayToLong(byte[] buffer) {
	if (buffer.length > 8 || buffer.length < 1) {
	    throw new IllegalArgumentException("Size of byte array must be between 1 and 8.");
	}

	long value = 0;

	for(int i = 0; i < buffer.length; i++) {
	    value |= (0xFF & buffer[buffer.length - 1 - i]) << i * 8;
	}

	return value;
    }

    /**
     * Converts a positive integer to a byte array.
     * @param value - positive integer to be converted
     * @return byte[]
     */
    public static byte[] convertPosIntToByteArray(long value) {
	return convertPosIntToByteArray(value, 8);
    }
    public static byte[] convertPosIntToByteArray(int value) {
	return convertPosIntToByteArray(value, 8);
    }

    /**
     * Converts a positive long integer to a byte array with a given bit size per byte.
     * @param value - positive integer to be converted
     * @return byte[]
     */
    public static byte[] convertPosIntToByteArray(long value, int numBits) {
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
		byte mask = numBitsToMask((byte)restBits);
		b = (byte) ((byte) (value >> (((i-1) * numBits) + restBits)) & mask);
	    } else {
		byte mask = numBitsToMask((byte)numBits);
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
	for (byte i=0; i < numBits; i++) {
	    result = (byte) ((result << 1) | 1);
	}
	return result;
    }

    /**
     * Converts a byte array to a hex string.
     * @param buffer - byte array to be converted
     * @return Hex String
     */
    public static String convByteArrayToString(byte[] buffer) {
	StringWriter writer = new StringWriter(buffer.length);
	PrintWriter out = new PrintWriter(writer);

	for (int i = 0; i < buffer.length; i++) {
	    out.printf("%02X", buffer[i]);
	}

	return writer.toString();
    }

    /**
     * Converts a hex string to a byte array.
     * The string must contain only hex characters.
     * @param s - hex string to be converted
     * @return byte[]
     */
    public static byte[] convStringToByteArray(String s) {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	int value = 0;
        // if length of s is odd, add a leading 0
        if ((s.length() % 2) != 0) {
            s = "0" + s;
        }
        for (int i = 0; i < s.length(); i += 2) {
	    value = Integer.parseInt(s.substring(i, i + 2), 16);
	    baos.write(value);
	}
	return baos.toByteArray();
    }
    private static final Pattern wsPattern = Pattern.compile("\\s");
    public static byte[] convStringWithWSToByteArray(String s) {
        s = wsPattern.matcher(s).replaceAll("");
        return convStringToByteArray(s);
    }


    /**
     * Dumps a byte array as hex characters.
     * @param buffer - byte array to be printed
     */
    public static void dumpAPDU(byte[] buffer) {
	for (int i = 0; i < buffer.length; i++) {
	    System.out.printf("%02X", buffer[i]);
	}
	System.out.println();
    }

}
