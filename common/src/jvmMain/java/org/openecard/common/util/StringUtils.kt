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
 */
package org.openecard.common.util

import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * A set of utility functions for Strings.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
object StringUtils {
	private val WS_PATTERN: Pattern = Pattern.compile("\\s")

	/**
	 * Convert a hex string to a byte array.
	 *
	 * @param hex string
	 * @return Byte array
	 * @throws NumberFormatException Thrown in case the hex string contains invalid characters.
	 */
	@JvmStatic
	fun toByteArray(
		hex: String,
	): ByteArray {
		var hex = hex
		if ((hex.length % 2) != 0) {
			hex = "0$hex"
		}
		val ret = ByteArray(hex.length / 2)
		for (i in ret.indices) {
			ret[i] = hex.substring(i * 2, (i * 2) + 2).toInt(16).toByte()
		}
		return ret
	}

	/**
	 * Convert a hex string to a byte array.<br></br>
	 * Remove all whitespace characters if flag is set.
	 *
	 * @param hex string
	 * @param removeWhitespace
	 * @return Byte array
	 */
	@JvmStatic
	fun toByteArray(
		hex: String,
		removeWhitespace: Boolean,
	): ByteArray {
		var hex = hex
		if (removeWhitespace) {
			hex = WS_PATTERN.matcher(hex).replaceAll("")
		}
		return toByteArray(hex)
	}

	/**
	 * Checks if the given string is null or empty.
	 *
	 * @param value String to check.
	 * @return `true` if string is `null` or empty, `false` otherwise.
	 */
	@JvmStatic
	fun isNullOrEmpty(value: String?): Boolean = value.isNullOrEmpty()

	/**
	 * Converts the given string to the empty string if it is null.
	 *
	 * @param s String to normalize.
	 * @return The empty string if the given string was `null` or empty, the given string otherwise.
	 */
	@JvmStatic
	fun nullToEmpty(s: String?): String = s ?: ""

	/**
	 * Converts the given string to null if it is empty.
	 *
	 * @param s String to normalize.
	 * @return `null` if the given string was `null` or empty, the given string otherwise.
	 */
	@JvmStatic
	fun emptyToNull(s: String?): String? = if (s != null && s.isEmpty()) null else s
}
