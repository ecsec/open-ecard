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
     * @param numBits Number of bits
     * @return byte[]
     */
    public static byte[] toByteArray(int value, int numBits) {
	return LongUtils.toByteArray(value, numBits);
    }

    /**
     * Convert an integer to a byte array. If the resulting array contains less bytes than 4 bytes, 0 bytes are prepended if the flag is set.
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

}
