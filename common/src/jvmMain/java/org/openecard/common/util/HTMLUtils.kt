/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

/**
 * Class with utility functions for working with HTML values.
 * It contains encoding functions for strings to prevent injection attacks.
 *
 * @author Tobias Wich
 */
object HTMLUtils {
	private val HTML_ESCAPES: Map<Char, String> =
		mapOf(
			'&' to "&amp;",
			'<' to "&lt;",
			'>' to "&gt;",
			'"' to "&quot;",
			'\'' to "&#x27;",
			'/' to "&#x2F;",
		)

	private val NEEDS_ATTRIBUTE_ESCAPE: Pattern = Pattern.compile("\\p{Alnum}")

	/**
	 * Escape HTML entities in the input string.
	 *
	 * @param input String to encode, may be `null`.
	 * @return The encoded string, or `null` if `null` was given as input.
	 * @see [OWASP XSS Cheat Sheet](https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet.RULE_.231_-_HTML_Escape_Before_Inserting_Untrusted_Data_into_HTML_Element_Content)
	 */
	@JvmStatic
	fun escapeHtml(input: String?): String? {
		// shortcut if the string does not contain anything interesting
		if (input == null || input.isEmpty()) {
			return input
		}

		val result = StringBuilder(input.length)
		for (element in input) {
			result.append(encodeChar(element, HTML_ESCAPES))
		}
		return result.toString()
	}

	/**
	 * Escape the string for safe use in HTML attributes.
	 *
	 * @param input String to encode, may be `null`.
	 * @return The encoded string, or `null` if `null` was given as input.
	 * @see [OWASP XSS Cheat Sheet](https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet.RULE_.232_-_Attribute_Escape_Before_Inserting_Untrusted_Data_into_HTML_Common_Attributes)
	 */
	@JvmStatic
	fun escapeAttribute(input: String?): String? {
		// shortcut if the string does not contain anything interesting
		if (input == null || input.isEmpty()) {
			return input
		}

		val result = StringBuilder(input.length)
		for (element in input) {
			val c = element
			if (c.code < 256 && NEEDS_ATTRIBUTE_ESCAPE.matcher(c.toString()).matches()) {
				result.append("&#x")
				result.append(String.format("%02x", c.code.toByte()))
				result.append(";")
			} else {
				result.append(c)
			}
		}
		return result.toString()
	}

	private fun encodeChar(
		c: Char,
		mapping: Map<Char, String>,
	): String {
		val encoded = mapping[c]
		return encoded ?: c.toString()
	}
}
