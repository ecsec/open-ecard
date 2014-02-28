/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of SkIDentity.
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openecard.bouncycastle.util.Strings;
import org.openecard.bouncycastle.util.encoders.Base64Encoder;
import org.openecard.bouncycastle.util.encoders.Encoder;


/**
 * This class is a Base64 encodeDataDatar/ decoder with a filename safe alphabet.
 * The following characters are different than in the original <a href="http://tools.ietf.org/html/rfc4648">Base64
 * specification (RFC 4648)</a>. This version is described in sec. 5 of the RFC.<br/>
 * <table>
 * <tr><td>+</td><td>-&gt;</td><td>-</td></tr>
 * <tr><td>/</td><td>-&gt;</td><td>_</td></tr>
 * </table>
 *
 * <p>This file also implements static methods which are derived from the BouncyCastle Base64 class.</p>
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class FileSafeBase64 extends Base64Encoder {

    public FileSafeBase64() {
	encodingTable[62] = (byte) '-';
	encodingTable[63] = (byte) '_';
	// update decoding table
	initialiseDecodingTable();
    }


    private static final Encoder encoder = new FileSafeBase64();

    /**
     * Encode the input data producing a base 64 encoded string.
     *
     * @param data Data to be encoded as base 64.
     * @return String containing the base 64 encoded data.
     */
    public static String toBase64String(byte[] data) {
        byte[] encoded = encodeData(data);
        return Strings.fromByteArray(encoded);
    }

    /**
     * Encode the input data producing a base 64 encoded byte array.
     *
     * @param data Data to be encoded as base 64.
     * @return Byte array containing the base 64 encoded data.
     */
    public static byte[] encodeData(byte[] data) {
        try {
	    int inLength = data.length;
	    int outLength = (inLength + 2) / 3 * 4;
	    ByteArrayOutputStream bOut = new ByteArrayOutputStream(outLength);
	    encodeData(data, bOut);
	    return bOut.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write encoded data to memory.", ex);
        }
    }

    /**
     * Encode the input data to base 64 writing it to the given output stream.
     *
     * @param data Data to be encoded as base 64.
     * @param out Stream to write the data to.
     * @return The number of bytes produced.
     * @throws java.io.IOException Thrown when writing to the stream failed.
     */
    public static int encodeData(byte[] data, OutputStream out) throws IOException {
	int inLength = data.length;
	return encoder.encode(data, 0, inLength, out);
    }


    /**
     * Decode the base 64 encoded data and write it to a byte array.
     * Whitespace will be ignored.
     *
     * @param data Base 64 encoded data.
     * @return A byte array representing the decoded data.
     */
    public static byte[] decodeData(byte[] data) {
        try {
	    int outLength = data.length / 4 * 3;
	    ByteArrayOutputStream bOut = new ByteArrayOutputStream(outLength);
	    decodeData(data, bOut);
	    return bOut.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write decoded data to memory.", ex);
        }
    }

    /**
     * Decode the base 64 encoded data writing it to the given output stream.
     * Whitespace characters will be ignored.
     *
     * @param data Base 64 encoded data.
     * @param out Stream to write the data to.
     * @return The number of bytes produced.
     * @throws java.io.IOException
     */
    public static int decodeData(byte[] data, OutputStream out) throws IOException {
        return encoder.decode(data, 0, data.length, out);
    }

    /**
     * Decode the base 64 encoded string and write it to a byte array.
     * Whitespace will be ignored.
     *
     * @param data Base 64 encoded data.
     * @return A byte array representing the decoded data.
     */
    public static byte[] decodeData(String data) {
        try {
	    int outLength = data.length() / 4 * 3;
	    ByteArrayOutputStream bOut = new ByteArrayOutputStream(outLength);
	    decodeData(data, bOut);
	    return bOut.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write decoded data to memory.", ex);
        }
    }

    /**
     * Decode the base 64 encoded string writing it to the given output stream.
     * Whitespace characters will be ignored.
     *
     * @param data Base 64 encoded data.
     * @param out Stream to write the data to.
     * @return The number of bytes produced.
     * @throws java.io.IOException
     */
    public static int decodeData(String data, OutputStream out) throws IOException {
        return encoder.decode(data, out);
    }

}
