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


/**
 * A set of utility functions for integers.
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class IntegerUtils {

    /**
     * Convert an integer to a byte array.
     *
     * @param value integer to be converted
     * @return byte[]
     */
    public static byte[] toByteArray(int value) {
	return toByteArray(value, 8);
    }

    /**
     * Convert an integer to a byte array with a given bit size per byte.
     *
     * @param value integer to be converted
     * @return byte[]
     */
    public static byte[] toByteArray(int value, int numBits) {
	return LongUtils.toByteArray(value, numBits);
    }

    /**
     * Convert an integer to a byte array.<br/>
     * If the resulting array contains less bytes than 4 bytes, 0 bytes are prepended if the flag is set.
     *
     * @param value integer to be converted
     * @param padArrayToTypeLength
     * @return byte[]
     */
    public static byte[] toByteArray(int value, boolean padArrayToTypeLength) {
	byte[] result = toByteArray(value, 8);
	if (padArrayToTypeLength && result.length < 4) {
	    result = ByteUtils.concatenate(new byte[4 - result.length], result);
	}
	return result;
    }

    /**
     * Convert a byte array to an  integer.<br/>
     * Size of byte array must be between 1 and 4.
     *
     * @param bytes byte array to be converted
     * @return int
     */
    public static int toInteger(byte[] bytes) {
	if (bytes.length > 4 || bytes.length < 1) {
	    throw new IllegalArgumentException("Size of byte array must be between 1 and 4.");
	}

	return (int) LongUtils.toLong(bytes);
    }

}
