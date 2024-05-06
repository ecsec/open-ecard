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

package org.openecard.common


/**
 * Representation of a Semantic Versioning number.
 * Unparseable version strings are evaluated to 0.0.0.
 *
 * @author Tobias Wich
 */
class SemanticVersion(ver: String?) : Comparable<SemanticVersion> {
    /**
     * Get complete version string with major, minor and patch version separated by dots.
     * If available, the build ID is appended with a dash as seperator.
     * @return AppVersion string or the string UNKNOWN if version is invalid or unavailable.
     */
    val versionString: String

    /**
     * Major version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
	val major: Int

    /**
     * Minor version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
	val minor: Int

    /**
     * Patch version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
	val patch: Int

    /**
     * Build ID suffix.
     * @return Build ID without suffix or null when no build suffix is used.
     */
    val buildId: String?

    init {
        this.versionString = cleanVersionString(ver)
        val groups = splitVersion(versionString)
        this.major = groups[0]!!.toInt()
        this.minor = groups[1]!!.toInt()
        this.patch = groups[2]!!.toInt()
        this.buildId = groups[3]
    }

    private fun splitVersion(version: String): Array<String?> {
        val groups = arrayOfNulls<String>(4)
        val p = Regex("(\\d+)\\.(\\d+)\\.(\\d+)(-.+)?")
        val m = p.matchEntire(version)
        if (m != null && m.groupValues.size >= 4) {
            groups[0] = m.groupValues[1]
            groups[1] = m.groupValues[2]
            groups[2] = m.groupValues[3]
            groups[3] = m.groupValues[4].ifBlank { null }
            // correct last match (remove -)
            if (groups[3] != null) {
                groups[3] = groups[3]!!.substring(1)
            }
        } else {
            groups[0] = "0"
            groups[1] = "0"
            groups[2] = "0"
            groups[3] = null
        }

        return groups
    }

    /**
     * Checks if this version is newer than the given version.
     *
     * @param v
     * @return
     */
    fun isNewer(v: SemanticVersion): Boolean {
        // special handling when both are UNKNOWN, then nothing is newer
        if (UNKNOWN_VERSION == versionString && UNKNOWN_VERSION == v.versionString) {
            return false
        }

        return this.compareTo(v) > 0
    }

    fun isOlder(v: SemanticVersion): Boolean {
        return v.isNewer(this)
    }

    fun isSame(v: SemanticVersion): Boolean {
        return this.compareTo(v) == 0
    }

    override fun compareTo(other: SemanticVersion): Int {
        // special handling of the UNKOWN value which is always the smaller version
        // check if both are unknown
        if (UNKNOWN_VERSION == versionString && UNKNOWN_VERSION == other.versionString) {
            return 0
        }
        // see if any of the versions is unknown
        if (UNKNOWN_VERSION == this.versionString) {
            return -1
        } else if (UNKNOWN_VERSION == other.versionString) {
            return 1
        }

        if (this.major != other.major) {
            return this.major.compareTo(other.major)
        }
        if (this.minor != other.minor) {
            return this.minor.compareTo(other.minor)
        }
        if (this.patch != other.patch) {
            return this.patch.compareTo(other.patch)
        }

        return if (this.buildId == null && other.buildId == null) {
            0
        } else if (this.buildId != null && other.buildId == null) {
            -1
        } else if (this.buildId == null && other.buildId != null) {
            1
        } else {
            buildId!!.compareTo(other.buildId!!)
        }
    }

    override fun toString(): String {
        return versionString
    }

}

private fun cleanVersionString(version: String?): String {
	return version ?: UNKNOWN_VERSION
}
