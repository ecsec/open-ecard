/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.releases

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.constraints.satisfiedBy

fun ReleaseInfo.checkVersion(versionString: String): Result<UpdateAdvice> =
	runCatching {
		val version = Version.parse(versionString)
		checkVersion(version).getOrThrow()
	}

fun ReleaseInfo.checkVersion(version: Version): Result<UpdateAdvice> =
	runCatching {
		if (version >= this.latestVersion.version) {
			// check if the version is newer than the release
			
			UpdateAdvice.NO_UPDATE
		} else {
			if (this.versionStatus.security.any { it.satisfiedBy(version) }) {
				// check if the version has a security problem
				
				UpdateAdvice.SECURITY_UPDATE
			} else if (this.versionStatus.maintained.any { it.satisfiedBy(version) }) {
				// check if the version is still maintained

				// check if we are older than the latest maintenance version
				val latestMaintVersion =
					this.maintenanceVersions.find {
						it.version.major == version.major && it.version.minor == version.minor
					} ?: throw IllegalArgumentException("No maintenance version defined for $version")
				if (latestMaintVersion.version > version) {
					UpdateAdvice.MAINTAINED_UPDATE
				} else {
					UpdateAdvice.MAINTAINED_NO_UPDATE
				}
			} else if (version.major == this.latestVersion.version.major && version.minor == this.latestVersion.version.minor) {
				// we have a noncritical update

				UpdateAdvice.UPDATE
			} else {
				// we are not maintained anymore

				UpdateAdvice.UNMAINTAINED
			}
		}
	}

fun ReleaseInfo.getMaintainedVersionData(version: Version): VersionData? {
	val maintVersion =
		this.maintenanceVersions.find {
			it.version.major == version.major && it.version.minor == version.minor
		}
	return maintVersion
}

fun ReleaseInfo.getUpdateData(version: Version): VersionData =
	this.getMaintainedVersionData(version)
		?: this.latestVersion

enum class UpdateAdvice {
	NO_UPDATE,
	UNMAINTAINED,
	MAINTAINED_NO_UPDATE,
	MAINTAINED_UPDATE,
	UPDATE,
	SECURITY_UPDATE,
}
