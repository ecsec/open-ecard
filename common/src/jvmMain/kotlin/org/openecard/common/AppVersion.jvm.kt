/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

import org.openecard.common.util.FileUtils.resolveResourceAsStream
import java.io.IOException
import java.io.InputStream
import java.util.*

private const val APPNAME_FILE = "openecard/APPNAME"
private const val APPVERSION_FILE = "openecard/VERSION"
private const val SPECNAME_FILE = "openecard/EID_CLIENT_SPECIFICATION"
private const val SPECVERSION_FILE = "openecard/SUPPORTED_EID_CLIENT_SPEC_VERSIONS"

private val INST: Version = loadAppVersion()

private fun loadAppVersion(): Version {
	var inName =
		try {
			resolveResourceAsStream(AppVersion::class.java, APPNAME_FILE)
		} catch (ex: IOException) {
			null
		}
	val name = readStream(inName)
	var inVer =
		try {
			resolveResourceAsStream(AppVersion::class.java, APPVERSION_FILE)
		} catch (ex: IOException) {
			null
		}
	val ver = readStream(inVer)
	var inSpecName =
		try {
			resolveResourceAsStream(AppVersion::class.java, SPECNAME_FILE)
		} catch (ex: IOException) {
			null
		}
	val specName = readStream(inSpecName)
	var inSpecVer =
		try {
			resolveResourceAsStream(AppVersion::class.java, SPECVERSION_FILE)
		} catch (ex: IOException) {
			null
		}
	val specVer = readStream(inSpecVer)

	return Version(name, ver, specName, specVer)
}

private fun readStream(inStream: InputStream?): String? {
	if (inStream == null) {
		return null
	} else {
		val s = Scanner(inStream, "UTF-8").useDelimiter("\\A")
		try {
			val nameStr = s.next()
			return nameStr.trim { it <= ' ' }
		} catch (ex: NoSuchElementException) {
			// empty file
			return null
		}
	}
}

/**
 * Version of the Open eCard Framework.
 * The version is loaded from the file VERSION in this module when the class is loaded.
 * The version string follows [semantic versioning](http://semver.org).
 *
 * @author Tobias Wich
 */
object AppVersion {
	@JvmStatic
	val name: String
		/**
		 * Gets the name of the application.
		 * @return Name of the app or the UNKNOWN if the name is unavailable.
		 */
		get() = INST.name

	@JvmStatic
	val version: SemanticVersion
		/**
		 * Gets the version of the application.
		 *
		 * @return The version of the application.
		 */
		get() = INST.version

	@JvmStatic
	val versionString: String
		/**
		 * Get complete version string with major, minor and patch version separated by dots.
		 * If available, the build ID is appended with a dash as seperator.
		 *
		 * @return AppVersion string or the string UNKNOWN if version is invalid or unavailable.
		 */
		get() = INST.version.versionString

	@JvmStatic
	val major: Int
		/**
		 * Major version.
		 * @return Major version number or 0 if version is invalid or unavailable.
		 */
		get() = INST.version.major

	@JvmStatic
	val minor: Int
		/**
		 * Minor version.
		 * @return Major version number or 0 if version is invalid or unavailable.
		 */
		get() = INST.version.minor

	@JvmStatic
	val patch: Int
		/**
		 * Patch version.
		 * @return Major version number or 0 if version is invalid or unavailable.
		 */
		get() = INST.version.patch

	@JvmStatic
	val buildId: String?
		/**
		 * Build ID suffix.
		 * @return Build ID without suffix or null when no build suffix is used.
		 */
		get() = INST.version.buildId

	@JvmStatic
	val specName: String
		/**
		 * Get the name of the specification.
		 *
		 * @return The name of the specification which is `BSI-TR-03124`.
		 */
		get() = INST.specName

	@JvmStatic
	val specVersions: List<String>
		/**
		 * Get the versions of specification this application is compatible to.
		 *
		 * @return A unmodifiable list containing all version this application is compatible to.
		 */
		get() = INST.specVersions

	val latestSpecVersion: String
		/**
		 * Get the latest version of the specification which is compatible to the application.
		 *
		 * @return Latest compatible specification version.
		 */
		get() = INST.latestSpecVersion
}
