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
 ***************************************************************************/

package org.openecard.addon;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openecard.addon.manifest.AddonSpecification;


/**
 * This class implements a SelctionStrategy which selects always the {@link AddonSpecification} with the highest version
 * number.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Hans-Maritn Haase <hans-martin.haase@ecsec.de>
 */
public class HighestVersionSelector implements SelectionStrategy {

    @Override
    public AddonSpecification select(Set<AddonSpecification> addons) {
	TreeSet<AddonSpecification> specs = new TreeSet<>(new VersionComparator());
	for (AddonSpecification spec : addons) {
	    specs.add(spec);
	}
	return specs.last();
	//return addons.iterator().next();
    }

    /**
     * This class implements a {@link Comparator} for {@link AddonSpecification} objects.
     * <br />
     * The comparison uses the contained version field.
     *
     * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
     */
    private class VersionComparator implements Comparator<AddonSpecification> {

	/**
	 * The method compares the versions contained in an {@link AddonSpecification} objects.
	 *
	 * @param o1 First {@link AddonSpecification} object to compare.
	 * @param o2 Second {@link AddonSpecification} object to compare.
	 * @return The method returns -1 if o2.version is greater than o1.version. 0 is returned if o1.version equals
	 * o2.version and 1 is returned if o2.version is less than o1.version.
	 */
	@Override
	public int compare(AddonSpecification o1, AddonSpecification o2) {
	    int result = 0;

	    if (o1 == null && o2 == null) {
		throw new NullPointerException("Can't compare two NULL elements.");
	    } else if (o1 == null && o2 != null) {
		return -1;
	    } else if (o1 != null && o2 == null) {
		return 1;
	    } else {
		// try to get a semantic version
		SemanticVersion semVer1 = new SemanticVersion(o1.getVersion());
		SemanticVersion semVer2 = new SemanticVersion(o2.getVersion());

		if (semVer1.isSemanticVersion() && semVer2.isSemanticVersion()) {
		    result = compareSemanticVersions(semVer1, semVer2);
		} else {
		    if (semVer1.isSemanticVersion()) {
			result = 1;
		    } else if (semVer2.isSemanticVersion()) {
			result = -1;
		    } else {
			result = o1.getVersion().compareTo(o2.getVersion());
		    }
		}
	    }

	    return result;
	}

	/**
	 * The method compares two {@link SemanticVersion} object.
	 *
	 * @param v1 First version to compare.
	 * @param v2 Second version to compare.
	 * @return The method returns 0 if the two versions are equal. If v1 is greater than v2 1 is returned. If v1 is
	 * less than v2 -1 is returned.
	 */
	private int compareSemanticVersions(SemanticVersion v1, SemanticVersion v2) {
	    if (compareIntNums(v1.getMajor(), v2.getMajor()) != 0) {
		return compareIntNums(v1.getMajor(), v2.getMajor());
	    }

	    if (compareIntNums(v1.getMinor(), v2.getMinor()) != 0) {
		return compareIntNums(v1.getMinor(), v2.getMinor());
	    }

	    if (compareIntNums(v1.getPatch(), v2.getPatch()) != 0) {
		return compareIntNums(v1.getPatch(), v2.getPatch());
	    }

	    // now it gets a bit difficult, for the label we repect the folowing rules
	    // - no label has a higher priority than a label
	    // - if both versions have a label than the alphabetical order is rated
	    if (v1.getAddLabel() != null && v2.getAddLabel() == null) {
		return -1;
	    } else if (v1.getAddLabel() == null && v2.getAddLabel() != null) {
		return 1;
	    } else if (v1.getAddLabel() == null && v2.getAddLabel() == null) {
		return 0;
	    } else {
		return v1.getAddLabel().compareTo(v2.getAddLabel());
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
	private int compareIntNums(int value1, int value2) {
	    int result = -2;

	    if (value1 == value2) {
		result = 0;
	    } else if (value1 > value2) {
		result = 1;
	    } else {
		result = -1;
	    }

	    return result;
	}
    }

    /**
     * The class models a Semantic Versioning String.
     *
     * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
     */
    private class SemanticVersion {
	/**
	 * Major version number (for incompatible API changes).
	 */
	private final int major;

	/**
	 * Minor version number (for functionality additions in a backwards-compatible manner).
	 */
	private final int minor;

	/**
	 * Patch version number (for backwards-compatible bug fixes).
	 */
	private final int patch;

	/**
	 * Additional label for pre-releases and build meta data.
	 */
	private final String addLabel;

	/**
	 * Indicates whether the version string which was given in the constructor is a semantic version or not.
	 */
	private final boolean isSemVersion;

	/**
	 * The constructor parses a string as semantic version.
	 * <br />
	 * If the string does not contain a semantic version major, minor and patch version are set to zero and the
	 *
	 *
	 * @param version The version string which should be parsed as semantic version.
	 */
	private SemanticVersion(String version) {
	    String[] groups = new String[4];
	    Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-.+)?");
	    Matcher m = p.matcher(version);
	    if (m.matches() && m.groupCount() >= 3) {
		groups[0] = m.group(1);
		groups[1] = m.group(2);
		groups[2] = m.group(3);
		groups[3] = m.group(4);
		// correct last match (remove -)
		if (groups[3] != null) {
		    groups[3] = groups[3].substring(1);
		}

		major = Integer.parseInt(groups[0]);
		minor = Integer.parseInt(groups[1]);
		patch = Integer.parseInt(groups[2]);
		addLabel = groups[3];
		isSemVersion = true;
	    } else {
		major = 0;
		minor = 0;
		patch = 0;
		addLabel = null;
		isSemVersion = false;
	    }
	}

	/**
	 * Get the major version number as integer value.
	 *
	 * @return The major version as integer.
	 */
	private int getMajor() {
	    return this.major;
	}

	/**
	 * Get the minor version number as integer value.
	 *
	 * @return The minor version number as integer.
	 */
	private int getMinor() {
	    return minor;
	}

	/**
	 * Get the patch version number as integer value.
	 *
	 * @return The patch version number as integer.
	 */
	private int getPatch() {
	    return patch;
	}

	/**
	 * Get the additional label.
	 * <br />
	 * This value might be null because it is optional.
	 *
	 * @return The additional label.
	 */
	private String getAddLabel() {
	    return addLabel;
	}

	/**
	 * Get a boolean value which indicates whether the version number given in the constructor is a semantic version
	 * or not.
	 *
	 * @return The method returns true if the given version number is a semantic version else false.
	 */
	private boolean isSemanticVersion() {
	    return isSemVersion;
	}
    }

}
