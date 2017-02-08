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
 ***************************************************************************/

package org.openecard.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Version class capable of parsing given name and version strings.
 *
 * @author Tobias Wich
 */
public class Version {

    private static final String UNKNOWN_NAME = "UNKNOWN";
    private static final String UNKNOWN_VERSION = "UNKNOWN";

    private final String version;
    private final String name;
    private final String specName;
    private final ArrayList<String> specVersions;
    private final int major;
    private final int minor;
    private final int patch;
    private final String buildId;

    public Version(@Nullable String name, @Nullable String ver, @Nullable String specName, @Nullable String specVer) {
	this.specName = fixName(specName);
	this.specVersions = loadVersionLine(specVer);
	this.name = fixName(name);
	this.version = loadVersionLine(ver).get(0);
	String[] groups = splitVersion(version);
	this.major = Integer.parseInt(groups[0]);
	this.minor = Integer.parseInt(groups[1]);
	this.patch = Integer.parseInt(groups[2]);
	this.buildId = groups[3];
    }

    private String fixName(String name) {
	return name == null ? UNKNOWN_NAME : name;
    }

    private static ArrayList<String> loadVersionLine(String in) {
	ArrayList<String> versions = new ArrayList<>();
	if (in == null) {
	    versions.add(UNKNOWN_VERSION);
	} else {
	    BufferedReader r = new BufferedReader(new StringReader(in));
	    try {
		String line = r.readLine();
		do {
		    if (line == null) {
			versions.add(UNKNOWN_VERSION);
		    } else {
			versions.add(line);
		    }

		    line = r.readLine();
		} while (line != null);
	    } catch (IOException ex) {
		versions.clear();
		versions.add(UNKNOWN_VERSION);
	    }
	}
	return versions;
    }

    private String[] splitVersion(String version) {
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
     * Gets the name of the application.
     * @return Name of the app or the UNKNOWN if the name is unavailable.
     */
    public String getName() {
	return name;
    }

    /**
     * Get complete version string with major, minor and patch version separated by dots.
     * If available, the build ID is appended with a dash as seperator.
     * @return AppVersion string or the string UNKNOWN if version is invalid or unavailable.
     */
    public String getVersion() {
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
    public boolean isNewer(@Nonnull Version v) {
	// check if both are unknown or equal
	if (UNKNOWN_VERSION.equals(getVersion()) && UNKNOWN_VERSION.equals(v.getVersion())) {
	    return false;
	} else if (getVersion().equals(v.getVersion())) {
	    return false;
	}

	// see if any of the versions is unknown
	if (UNKNOWN_VERSION.equals(getVersion())) {
	    return false;
	} else if (UNKNOWN_VERSION.equals(v.getVersion())) {
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

    public boolean isOlder(@Nonnull Version v) {
	return v.isNewer(this);
    }

    public boolean isSame(@Nonnull Version v) {
	return ! this.isNewer(v) && ! v.isNewer(this);
    }

    /**
     * Get the name of the specification.
     *
     * @return The name of the specification which is {@code BSI-TR-03124}.
     */
    public String getSpecName() {
	return specName;
    }

    /**
     * Get the versions of specification this application is compatible to.
     *
     * @return A unmodifiable list containing all version this application is compatible to.
     */
    public List<String> getSpecVersions() {
	return Collections.unmodifiableList(specVersions);
    }

    /**
     * Get the latest version of the specification which is compatible to the application.
     *
     * @return Latest compatible specification version.
     */
    public String getLatestSpecVersion() {
	return specVersions.get(specVersions.size() - 1);
    }

}
