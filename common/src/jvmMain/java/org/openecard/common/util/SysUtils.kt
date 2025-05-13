/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

import java.io.File
import java.util.Locale
import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * Utility class for system related tasks.
 *
 * @author Tobias Wich
 */
object SysUtils {
	private val name: String
		get() = System.getProperty("os.name").lowercase(Locale.getDefault())

	@JvmStatic
	val isWin: Boolean
		get() = name.contains("win")

	@JvmStatic
	val isMacOSX: Boolean
		get() = name.contains("mac")

	@JvmStatic
	val isUnix: Boolean
		get() {
			val os = name
			return os.contains("nux") || os.contains("nix") || os.contains("aix")
		}

	@JvmStatic
	val isDebianOrDerivate: Boolean
		get() = File("/etc/debian_version").exists()

	@JvmStatic
	val isRedhatOrDerivate: Boolean
		get() = File("/etc/redhat-release").exists()

	@JvmStatic
	val isArchLinuxOrDerivate: Boolean
		get() = File("/etc/arch-release").exists()

	@JvmStatic
	val isSuSEOrDerivate: Boolean
		get() = File("/etc/SuSE-release").exists()

	@JvmStatic
	val isMobileDevice: Boolean
		get() = isAndroid || isIOS

	private val CURLY_VAR_ENV: Pattern = Pattern.compile("(\\\\)?(\\$\\{([A-Za-z0-9_]+)\\})")
	private val PLAIN_VAR_ENV: Pattern = Pattern.compile("(\\\\)?(\\$([A-Za-z0-9_]+))")
	private val CURLY_VAR_SYS: Pattern = Pattern.compile("(\\\\)?(\\$\\{([A-Za-z0-9_\\.]+)\\})")

	@JvmStatic
	var isAndroid: Boolean = false
		private set

	@JvmStatic
	var isIOS: Boolean = false
		private set

	@JvmStatic
	fun setIsIOS() {
		isIOS = true
	}

	@JvmStatic
	fun setIsAndroid() {
		isAndroid = true
	}

	/**
	 * Expands environment variables in the given text.
	 * The environment variables can be in the form of $VAR or ${VAR}. Unset variables will be replaced with the empty
	 * string. To prevent the evaluation of a variable it must be prepended with a \ (e.g. \${VAR}). The \ gets removed
	 * leaving only the text after it in place.
	 *
	 * @param s The text in which the variables should be expanded.
	 * @return The expanded text.
	 */
	fun expandEnvVars(s: String): String {
		// expand and clear unset vars
		var s = s
		s = expandVars(s, System.getenv(), CURLY_VAR_ENV, true)
		s = expandVars(s, System.getenv(), PLAIN_VAR_ENV, true)
		// remove quote symbols
		s = removeQuote(s, CURLY_VAR_ENV)
		s = removeQuote(s, PLAIN_VAR_ENV)
		return s
	}

	/**
	 * Expands system properties in the given text.
	 * The system property can be in the form of ${VAR}. Unset variables will be replaced with the empty
	 * string. To prevent the evaluation of a variable it must be prepended with a \ (e.g. \${VAR}). The \ gets removed
	 * leaving only the text after it in place.
	 *
	 * @param s The text in which the variables should be expanded.
	 * @return The expanded text.
	 */
	fun expandSysProps(
		@Nonnull s: String,
	): String {
		// expand and clear unset vars
		var s = s
		s = expandVars(s, System.getProperties(), CURLY_VAR_SYS, true)
		// remove quote symbols
		s = removeQuote(s, CURLY_VAR_SYS)
		return s
	}

	/**
	 * Expands environment variables and system properties in the given text.
	 * The environment variables can be in the form of $VAR or ${VAR} and the system properties can be in the form of
	 * ${VAR}. Unset variables will be replaced with the empty string. To prevent the evaluation of a variable it must
	 * be prepended with a \ (e.g. \${VAR}). The \ gets removed leaving only the text after it in place.<br></br>
	 * The evaluation order is:
	 *
	 *  1. Environment variables
	 *  1. System Properties
	 *
	 *
	 * @param s The text in which the variables should be expanded.
	 * @return The expanded text.
	 */
	fun expandEnvVarsAndSysProps(s: String): String {
		// expand curly env without clearing vars so system properties have a chance to set values too
		var s = s
		s = expandVars(s, System.getenv(), CURLY_VAR_ENV, false)
		s = expandVars(s, System.getenv(), PLAIN_VAR_ENV, true)
		s = expandVars(s, System.getProperties(), CURLY_VAR_SYS, true)
		// remove quote symbols
		s = removeQuote(s, CURLY_VAR_ENV)
		s = removeQuote(s, PLAIN_VAR_ENV)
		s = removeQuote(s, CURLY_VAR_SYS)
		return s
	}

	private fun expandVars(
		s: String,
		env: Map<out Any, Any>,
		p: Pattern,
		clear: Boolean,
	): String {
		var s = s
		val m = p.matcher(s)

		// Look up all variables in the string
		while (m.find()) {
			val quote = "\\" == m.group(1)
			val varName = m.group(3)
			val envValueObj = env[varName]
			val replacement =
				envValueObj?.toString()
					?: if (clear) {
						""
					} else {
						null
					}

			// perform the replace (variable -> value) only if there is a value
			if (!quote && replacement != null) {
				val target = m.group(2)
				s = s.replace(target, replacement)
			}
		}

		return s
	}

	private fun removeQuote(
		s: String,
		p: Pattern,
	): String {
		var s = s
		val m = p.matcher(s)
		s = m.replaceAll("$2")
		return s
	}

	fun is64bit(): Boolean {
		val arch = System.getProperty("os.arch", "").lowercase(Locale.getDefault())

		return when (arch) {
			"x86_64", "amd64", "ppc64" -> true
			else -> false
		}
	}
}
