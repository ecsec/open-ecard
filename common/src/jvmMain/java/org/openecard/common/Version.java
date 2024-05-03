/****************************************************************************
 * Copyright (C) 2016-2017 ecsec GmbH.
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
import javax.annotation.Nullable;


/**
 * Version class capable of parsing given name and version strings.
 *
 * @author Tobias Wich
 */
public class Version {

    static final String UNKNOWN_NAME = "UNKNOWN";
    static final String UNKNOWN_VERSION = "UNKNOWN";

    private final SemanticVersion version;
    private final String name;
    private final String specName;
    private final ArrayList<String> specVersions;

    public Version(@Nullable String name, @Nullable String ver, @Nullable String specName, @Nullable String specVer) {
	this.specName = fixName(specName);
	this.specVersions = loadVersionLine(specVer);
	this.name = fixName(name);
	this.version = new SemanticVersion(ver);
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

    public SemanticVersion getVersion() {
	return version;
    }


    /**
     * Gets the name of the application.
     * @return Name of the app or the UNKNOWN if the name is unavailable.
     */
    public String getName() {
	return name;
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
