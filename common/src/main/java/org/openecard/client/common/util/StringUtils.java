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
