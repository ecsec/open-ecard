/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
package org.openecard.addon

import org.openecard.addon.manifest.AddonSpecification
import java.util.SortedSet
import java.util.regex.Pattern

/**
 * This class implements a SelctionStrategy which selects always the [AddonSpecification] with the highest version
 * number.
 *
 * @author Tobias Wich
 * @author Hans-Maritn Haase
 */
class HighestVersionSelector : SelectionStrategy {
	override fun select(addons: MutableSet<AddonSpecification>): AddonSpecification {
		val specs: SortedSet<AddonSpecification> =
			sortedSetOf<AddonSpecification>(this.VersionComparator())
		for (spec in addons) {
			specs.add(spec)
		}
		return specs.last()
		// return addons.iterator().next();
	}

	/**
	 * This class implements a [Comparator] for [AddonSpecification] objects.
	 * <br></br>
	 * The comparison uses the contained version field.
	 *
	 * @author Hans-Martin Haase
	 */
	private inner class VersionComparator : Comparator<AddonSpecification?> {
		/**
		 * The method compares the versions contained in an [AddonSpecification] objects.
		 *
		 * @param o1 First [AddonSpecification] object to compare.
		 * @param o2 Second [AddonSpecification] object to compare.
		 * @return The method returns -1 if o2.version is greater than o1.version. 0 is returned if o1.version equals
		 * o2.version and 1 is returned if o2.version is less than o1.version.
		 */
		override fun compare(
			o1: AddonSpecification?,
			o2: AddonSpecification?,
		): Int {
			var result: Int

			if (o1 == null && o2 == null) {
				throw NullPointerException("Can't compare two NULL elements.")
			} else if (o1 == null && o2 != null) {
				return -1
			} else if (o1 != null && o2 == null) {
				return 1
			} else {
				// try to get a semantic version
				val semVer1 = SemanticVersion(o1!!.getVersion())
				val semVer2 = SemanticVersion(o2!!.getVersion())

				result =
					if (semVer1.isSemanticVersion && semVer2.isSemanticVersion) {
						compareSemanticVersions(semVer1, semVer2)
					} else {
						if (semVer1.isSemanticVersion) {
							1
						} else if (semVer2.isSemanticVersion) {
							-1
						} else {
							o1.getVersion().compareTo(o2.getVersion())
						}
					}
			}

			return result
		}

		/**
		 * The method compares two [SemanticVersion] object.
		 *
		 * @param v1 First version to compare.
		 * @param v2 Second version to compare.
		 * @return The method returns 0 if the two versions are equal. If v1 is greater than v2 1 is returned. If v1 is
		 * less than v2 -1 is returned.
		 */
		fun compareSemanticVersions(
			v1: SemanticVersion,
			v2: SemanticVersion,
		): Int {
			if (compareIntNums(v1.major, v2.major) != 0) {
				return compareIntNums(v1.major, v2.major)
			}

			if (compareIntNums(v1.minor, v2.minor) != 0) {
				return compareIntNums(v1.minor, v2.minor)
			}

			if (compareIntNums(v1.patch, v2.patch) != 0) {
				return compareIntNums(v1.patch, v2.patch)
			}

			// now it gets a bit difficult, for the label we repect the folowing rules
			// - no label has a higher priority than a label
			// - if both versions have a label than the alphabetical order is rated
			if (v1.addLabel != null && v2.addLabel == null) {
				return -1
			} else if (v1.addLabel == null && v2.addLabel != null) {
				return 1
			} else if (v1.addLabel == null && v2.addLabel == null) {
				return 0
			} else {
				return v1.addLabel!!.compareTo(v2.addLabel!!)
			}
		}

		/**
		 * The method compares two integers.
		 *
		 * @param value1 First integer value to compare.
		 * @param value2 Second integer value to compare.
		 * @return The method returns 0 of the two values are equals. If value1 is greater than value2 1 is returned.
		 * If value2 is greater than value1 then -1 is returned.
		 */
		fun compareIntNums(
			value1: Int,
			value2: Int,
		): Int {
			var result = -2

			result =
				if (value1 == value2) {
					0
				} else if (value1 > value2) {
					1
				} else {
					-1
				}

			return result
		}
	}

	/**
	 * The class models a Semantic Versioning String.
	 *
	 * @author Hans-Martin Haase
	 */
	private inner class SemanticVersion(
		version: String,
	) {
		/**
		 * Get the major version number as integer value.
		 *
		 * @return The major version as integer.
		 * Major version number (for incompatible API changes).
		 */
		val major: Int

		/**
		 * Get the minor version number as integer value.
		 *
		 * @return The minor version number as integer.

		 * Minor version number (for functionality additions in a backwards-compatible manner).
		 */
		val minor: Int

		/**
		 * Get the patch version number as integer value.
		 *
		 * @return The patch version number as integer.

		 * Patch version number (for backwards-compatible bug fixes).
		 */
		val patch: Int

		/**
		 * Get the additional label.
		 * <br></br>
		 * This value might be null because it is optional.
		 *
		 * @return The additional label.

		 * Additional label for pre-releases and build meta data.
		 */
		val addLabel: String?

		/**
		 * Get a boolean value which indicates whether the version number given in the constructor is a semantic version
		 * or not.
		 *
		 * @return The method returns true if the given version number is a semantic version else false.

		 * Indicates whether the version string which was given in the constructor is a semantic version or not.
		 */
		val isSemanticVersion: Boolean

		/**
		 * The constructor parses a string as semantic version.
		 * <br></br>
		 * If the string does not contain a semantic version major, minor and patch version are set to zero and the
		 *
		 *
		 * @param version The version string which should be parsed as semantic version.
		 */
		init {
			val groups = arrayOfNulls<String>(4)
			val p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-.+)?")
			val m = p.matcher(version)
			if (m.matches() && m.groupCount() >= 3) {
				groups[0] = m.group(1)
				groups[1] = m.group(2)
				groups[2] = m.group(3)
				groups[3] = m.group(4)
				// correct last match (remove -)
				if (groups[3] != null) {
					groups[3] = groups[3]!!.substring(1)
				}

				major = groups[0]!!.toInt()
				minor = groups[1]!!.toInt()
				patch = groups[2]!!.toInt()
				addLabel = groups[3]
				this.isSemanticVersion = true
			} else {
				major = 0
				minor = 0
				patch = 0
				addLabel = null
				this.isSemanticVersion = false
			}
		}
	}
}
