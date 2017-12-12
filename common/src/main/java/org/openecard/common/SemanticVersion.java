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
 ***************************************************************************/

package org.openecard.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.openecard.common.Version.UNKNOWN_VERSION;


/**
 * Representation of a Semantic Versioning number.
 * Unparseable version strings are evaluated to 0.0.0.
 *
 * @author Tobias Wich
 */
public class SemanticVersion {

    private final String version;
    private final int major;
    private final int minor;
    private final int patch;
    private final String buildId;

    public SemanticVersion(@Nullable String ver) {
	this.version = cleanVersionString(ver);
	String[] groups = splitVersion(version);
	this.major = Integer.parseInt(groups[0]);
	this.minor = Integer.parseInt(groups[1]);
	this.patch = Integer.parseInt(groups[2]);
	this.buildId = groups[3];
    }

    private static String cleanVersionString(@Nullable String in) {
	if (in == null) {
	    return Version.UNKNOWN_VERSION;
	} else {
	    return in;
	}
    }

    private String[] splitVersion(@Nonnull String version) {
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
	} else {
	    groups[0] = "0";
	    groups[1] = "0";
	    groups[2] = "0";
	    groups[3] = null;
	}

	return groups;
    }

    /**
     * Get complete version string with major, minor and patch version separated by dots.
     * If available, the build ID is appended with a dash as seperator.
     * @return AppVersion string or the string UNKNOWN if version is invalid or unavailable.
     */
    public String getVersionString() {
	return version;
    }

    /**
     * Major version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
    public int getMajor() {
	return major;
    }

    /**
     * Minor version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
    public int getMinor() {
	return minor;
    }

    /**
     * Patch version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
    public int getPatch() {
	return patch;
    }

    /**
     * Build ID suffix.
     * @return Build ID without suffix or null when no build suffix is used.
     */
    public String getBuildId() {
	return buildId;
    }

    /**
     * Checks if this version is newer than the given version.
     *
     * @param v
     * @return
     */
    public boolean isNewer(@Nonnull SemanticVersion v) {
	// check if both are unknown or equal
	if (UNKNOWN_VERSION.equals(getVersionString()) && UNKNOWN_VERSION.equals(v.getVersionString())) {
	    return false;
	} else if (getVersionString().equals(v.getVersionString())) {
	    return false;
	}

	// see if any of the versions is unknown
	if (UNKNOWN_VERSION.equals(getVersionString())) {
	    return false;
	} else if (UNKNOWN_VERSION.equals(v.getVersionString())) {
	    return true;
	}

	// compare major
	if (getMajor() > v.getMajor()) {
	    return true;
	} else if (getMajor() < v.getMajor()) {
	    return false;
	}

	// compare minor
	if (getMinor() > v.getMinor()) {
	    return true;
	} else if (getMinor() < v.getMinor()) {
	    return false;
	}

	// compare patch
	if (getPatch() > v.getPatch()) {
	    return true;
	} else if (getPatch() < v.getPatch()) {
	    return false;
	}

	// compare build
	if (getBuildId() == null && v.getBuildId() == null) {
	    return false;
	} else if (getBuildId() == null && v.getBuildId() != null) {
	    return true;
	} else if (getBuildId() != null && v.getBuildId() == null) {
	    return false;
	} else {
	    return getBuildId().compareTo(v.getBuildId()) < 0;
	}
    }

    public boolean isOlder(@Nonnull SemanticVersion v) {
	return v.isNewer(this);
    }

    public boolean isSame(@Nonnull SemanticVersion v) {
	return ! this.isNewer(v) && ! v.isNewer(this);
    }

}
