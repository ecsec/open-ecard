/****************************************************************************
 * Copyright (C) 2016-2024 ecsec GmbH.
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

package org.openecard.common

import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.*

const val UNKNOWN_NAME: String = "UNKNOWN"
const val UNKNOWN_VERSION: String = "UNKNOWN"

/**
 * Version class capable of parsing given name and version strings.
 *
 * @author Tobias Wich
 */
class Version(
	name: String?,
	ver: String?,
	specName: String?,
	specVer: String?,
) {
	@JvmField
	val version: SemanticVersion

	/**
	 * Gets the name of the application.
	 * @return Name of the app or the UNKNOWN if the name is unavailable.
	 */
	val name: String

	/**
	 * Get the name of the specification.
	 *
	 * @return The name of the specification which is `BSI-TR-03124`.
	 */
	val specName: String

	/**
	 * Get the versions of specification this application is compatible to.
	 */
	val specVersions: List<String>

	init {
		this.specName = fixName(specName)
		this.specVersions = loadVersionLine(specVer)
		this.name = fixName(name)
		this.version = SemanticVersion(ver)
	}

	private fun fixName(name: String?): String = name ?: UNKNOWN_NAME

	val latestSpecVersion: String
		/**
		 * Get the latest version of the specification which is compatible to the application.
		 *
		 * @return Latest compatible specification version.
		 */
		get() = specVersions[specVersions.size - 1]

	companion object {
		private fun loadVersionLine(inStream: String?): List<String> {
			val versions = ArrayList<String>()
			if (inStream == null) {
				versions.add(UNKNOWN_VERSION)
			} else {
				val r = BufferedReader(StringReader(inStream))
				try {
					var line = r.readLine()
					do {
						if (line == null) {
							versions.add(UNKNOWN_VERSION)
						} else {
							versions.add(line)
						}

						line = r.readLine()
					} while (line != null)
				} catch (ex: IOException) {
					versions.clear()
					versions.add(UNKNOWN_VERSION)
				}
			}
			return versions
		}
	}
}
