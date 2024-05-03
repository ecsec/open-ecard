/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * A set of utility functions for Strings.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class StringUtils {

    private static final Pattern WS_PATTERN = Pattern.compile("\\s");

    /**
     * Convert a hex string to a byte array.
     *
     * @param hex string
     * @return Byte array
     * @throws NumberFormatException Thrown in case the hex string contains invalid characters.
     */
    public static byte[] toByteArray(@Nonnull String hex) {
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
     * Convert a hex string to a byte array.<br>
     * Remove all whitespace characters if flag is set.
     *
     * @param hex string
     * @param removeWhitespace
     * @return Byte array
     */
    public static byte[] toByteArray(@Nonnull String hex, boolean removeWhitespace) {
	if (removeWhitespace) {
	    hex = WS_PATTERN.matcher(hex).replaceAll("");
	}
	return toByteArray(hex);
    }

    /**
     * Checks if the given string is null or empty.
     *
     * @param value String to check.
     * @return {@code true} if string is {@code null} or empty, {@code false} otherwise.
     */
    public static boolean isNullOrEmpty(@Nullable String value) {
	return value == null || value.isEmpty();
    }

    /**
     * Converts the given string to the empty string if it is null.
     *
     * @param s String to normalize.
     * @return The empty string if the given string was {@code null} or empty, the given string otherwise.
     */
    @Nonnull
    public static String nullToEmpty(@Nullable String s) {
	return s == null ? "" : s;
    }

    /**
     * Converts the given string to null if it is empty.
     *
     * @param s String to normalize.
     * @return {@code null} if the given string was {@code null} or empty, the given string otherwise.
     */
    @Nullable
    public static String emptyToNull(@Nullable String s) {
	return s != null && s.isEmpty() ? null : s;
    }

}
