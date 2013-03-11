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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ValueValidator {

    /**
     * Check if the value contains at least the given number of bytes as hex digits.
     *
     * @deprecated Renamed to {@link #checkHexByteStrength(java.lang.String, int)
     * @param value Value to test for hex digits.
     * @param numBytes Number of bytes that must be present in the value.
     * @return true if enough bytes are present, false otherwise.
     */
    @Deprecated
    public static boolean checkHexStrength(String value, int numBytes) {
	return checkHexNibbleStrength(value, numBytes * 2);
    }
    /**
     * Check if the value contains at least the given number of bytes as hex digits.
     *
     * @param value Value to test for hex digits.
     * @param numBytes Number of bytes that must be present in the value.
     * @return true if enough bytes are present, false otherwise.
     */
    public static boolean checkHexByteStrength(String value, int numBytes) {
	return checkHexNibbleStrength(value, numBytes * 2);
    }

    /**
     * Check if the value contains at least the given number of half-bytes as hex digits.
     *
     * @param value Value to test for hex digits.
     * @param numNibbles  Number of half-bytes that must be present in the value.
     * @return true if enough bytes are present, false otherwise.
     */
    public static boolean checkHexNibbleStrength(String value, int numNibbles) {
	Pattern p = Pattern.compile("\\p{XDigit}{1}");
	Matcher m = p.matcher(value);

	int count = 0;
	while (m.find()) {
	    count++;
	}

	return count >= numNibbles;
    }

    /**
     * Check if the psk value is strong enough.
     * PSKs must at least contain 16 bytes as hex digits.
     *
     * @param psk PSK value to test.
     * @return true if psk is strong enough, false otherwise.
     */
    public static boolean checkPSKStrength(String psk) {
	return checkHexByteStrength(psk, 16);
    }
    /**
     * Check if the session ID value is strong enough.
     * Session IDs must at least contain 25 half-bytes as hex digits. BSI TR-03112 sec. 3.7.1 demands that session IDs
     * must have at least 100 bits of entropy which is exactly 25 nibbles.
     *
     * @param session Session ID value to test.
     * @return true if session ID is strong enough, false otherwise.
     */
    public static boolean checkSessionStrength(String session) {
	return checkHexNibbleStrength(session, 25);
    }

}
