/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.openecard.common.util.FileUtils;


/**
 * Version of the Open eCard Framework.
 * The version is loaded from the file VERSION in this module when the class is loaded.
 * The version string follows <a href="http://semver.org">semantic versioning</a>.
 *
 * @author Tobias Wich
 */
public class AppVersion {

    private static final String APPNAME_FILE = "openecard/APPNAME";
    private static final String APPVERSION_FILE = "openecard/VERSION";
    private static final String SPECNAME_FILE = "openecard/EID_CLIENT_SPECIFICATION";
    private static final String SPECVERSION_FILE = "openecard/SUPPORTED_EID_CLIENT_SPEC_VERSIONS";

    private static final Version INST;

    static {
	InputStream inName, inVer, inSpecName, inSpecVer;
	try {
	    inName = FileUtils.resolveResourceAsStream(AppVersion.class, APPNAME_FILE);
	} catch (IOException ex) {
	    inName = null;
	}
	String name = readStream(inName);
	try {
	    inVer = FileUtils.resolveResourceAsStream(AppVersion.class, APPVERSION_FILE);
	} catch (IOException ex) {
	    inVer = null;
	}
	String ver = readStream(inVer);
	try {
	    inSpecName = FileUtils.resolveResourceAsStream(AppVersion.class, SPECNAME_FILE);
	} catch (IOException ex) {
	    inSpecName = null;
	}
	String specName = readStream(inSpecName);
	try {
	    inSpecVer = FileUtils.resolveResourceAsStream(AppVersion.class, SPECVERSION_FILE);
	} catch (IOException ex) {
	    inSpecVer = null;
	}
	String specVer = readStream(inSpecVer);

	INST = new Version(name, ver, specName, specVer);
    }

    private static String readStream(InputStream in) {
	if (in == null) {
	    return null;
	} else {
	    Scanner s = new Scanner(in, "UTF-8").useDelimiter("\\A");
	    try {
		String nameStr = s.next();
		return nameStr.trim();
	    } catch (NoSuchElementException ex) {
		// empty file
		return null;
	    }
	}
    }


    /**
     * Gets the name of the application.
     * @return Name of the app or the UNKNOWN if the name is unavailable.
     */
    public static String getName() {
	return INST.getName();
    }

    /**
     * Get complete version string with major, minor and patch version separated by dots.
     * If available, the build ID is appended with a dash as seperator.
     * @return AppVersion string or the string UNKNOWN if version is invalid or unavailable.
     */
    public static String getVersion() {
	return INST.getVersion();
    }

    /**
     * Major version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
    public static int getMajor() {
	return INST.getMajor();
    }

    /**
     * Minor version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
    public static int getMinor() {
	return INST.getMinor();
    }

    /**
     * Patch version.
     * @return Major version number or 0 if version is invalid or unavailable.
     */
    public static int getPatch() {
	return INST.getPatch();
    }

    /**
     * Build ID suffix.
     * @return Build ID without suffix or null when no build suffix is used.
     */
    public static String getBuildId() {
	return INST.getBuildId();
    }

    /**
     * Get the name of the specification.
     *
     * @return The name of the specification which is {@code BSI-TR-03124}.
     */
    public static String getSpecName() {
	return INST.getSpecName();
    }

    /**
     * Get the versions of specification this application is compatible to.
     *
     * @return A unmodifiable list containing all version this application is compatible to.
     */
    public static List<String> getSpecVersions() {
	return INST.getSpecVersions();
    }

    /**
     * Get the latest version of the specification which is compatible to the application.
     *
     * @return Latest compatible specification version.
     */
    public static String getLatestSpecVersion() {
	return INST.getLatestSpecVersion();
    }

}
