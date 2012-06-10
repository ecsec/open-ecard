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
     * @param value Value to test for hex digits.
     * @param numBytes Number of bytes that must be present in the value.
     * @return true if enough bytes are present, false otherwise.
     */
    public static boolean checkHexStrength(String value, int numBytes) {
	Pattern p = Pattern.compile("\\p{XDigit}{2}");
	Matcher m = p.matcher(value);

	int count = 0;
	while (m.find()) {
	    count++;
	}

	return count >= numBytes;
    }

    /**
     * Check if the psk value is strong enough.
     * PSKs must at least contain 16 bytes as hex digits.
     *
     * @param psk PSK value to test.
     * @return true if psk is strong enough, false otherwise.
     */
    public static boolean checkPSKStrength(String psk) {
	return checkHexStrength(psk, 16);
    }
    /**
     * Check if the session ID value is strong enough.
     * Session IDs must at least contain 16 bytes as hex digits. Usually UUIDs are used.
     *
     * @param session Session ID value to test.
     * @return true if session ID is strong enough, false otherwise.
     */
    public static boolean checkSessionStrength(String session) {
	return checkHexStrength(session, 16);
    }

}
