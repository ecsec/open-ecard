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

import java.util.regex.Pattern;


/**
 * A set of utility functions for Strings.
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StringUtils {

    private static final Pattern wsPattern = Pattern.compile("\\s");

    /**
     * Convert a hex string to a byte array.
     *
     * @param hex string
     * @return Byte array
     */
    public static byte[] toByteArray(String hex) {
        if ((hex.length() % 2) != 0) {
            hex = "0" + hex;
        }
        byte[] ret = new byte[hex.length() / 2];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) Integer.parseInt(hex.substring(i*2, (i*2) + 2), 16);
        }
        return ret;
    }

    /**
     * Convert a hex string to a byte array.<br/>
     * Remove all whitespace characters if flag is set.
     *
     * @param hex string
     * @param removeWhitespace
     * @return Byte array
     */
    public static byte[] toByteArray(String hex, boolean removeWhitespace) {
	if (removeWhitespace) {
	    hex = wsPattern.matcher(hex).replaceAll("");
	}
        return toByteArray(hex);
    }

}
