/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

package org.openecard.richclient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a fix that sets the JNA runtime directory to an executable directory, because the default value
 * /tmp may be mounted as 'noexec' on some systems, which will prevent the startup of the app.
 *
 * @author Sebastian Schuberth
 */
public class JnaRuntimeDirectoryFix {

    private static final Logger LOG = LoggerFactory.getLogger(JnaRuntimeDirectoryFix.class);

    /**
     * Sets the runtime directory for JNA to an executable directory
     * <p>
     * The directory will be set to one of the following, in descending order of priority:
     * <ul>
     * <li>the directory pointed to by the System property <em>jna.tmpdir</em>, if this value is set</li>
     * <li>the <em>/tmp</em> directory, if it is not mounted as 'noexec'. This is also the
     * default for JNA.</li>
     * <li>the directory pointed to by the enviroment variable <em>XDG_RUNTIME_DIR</em>, if this value is set and the
     * referenced directory is not mounted as 'noexec'.</li>
     * <li><em>~/.openecard/run</em> as a fallback if none of the above work
     * </ul>
     *
     * @throws IOException if there is an error reading the "/proc/mounts" file, which contains information about 
     * whether a path is mounted as 'noexec' or not
     */
    public static void setJnaRuntimeDirectory() throws IOException {
	// read value of jna.tmpdir property
	Properties properties = new Properties(System.getProperties());
	String propJnaTmpDir = properties.getProperty("jna.tmpdir");

	// if the property has been set externally don't change it
	if (propJnaTmpDir != null) {
	    return;
	}

	//check if we are on Linux
	String osName = properties.getProperty("os.name");
	if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {

	    // we are on linux, first read "XDG_RUNTIME_DIR" to see if a user run dir is set
	    String userRuntimeDir = System.getenv("XDG_RUNTIME_DIR");
	    boolean canUseNormalTempDir = true;
	    // if it is not set, we cannot use it
	    boolean canUseUserRunDir = (userRuntimeDir != null);

	    // then read "/proc/mounts"
	    try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/mounts"))) {
		String line;

		while ((line = bufferedReader.readLine()) != null) {
		    // split by whitespace to get the 6 parts individually
		    String[] parts = line.split(" ");
		    String mountPath = parts[1]; // get the path where the file system is mounted
		    String mountOptions = parts[3]; // get the mount options

		    if (mountPath.equals("/tmp")) {
			// we can only use /tmp if the mountOptions do not contain "noexec"
			canUseNormalTempDir = (!mountOptions.contains("noexec"));
		    } else if (mountPath.equals(userRuntimeDir)) {
			// same for the user run dir; if the user run dir is not set, then the equals above will be false
			canUseUserRunDir = (!mountOptions.contains("noexec"));
		    }
		}
	    }

	    if (canUseNormalTempDir) {
		// we can use /tmp directly and as JNA uses it as default anyway, nothing more to do here
		return;
	    }

	    if (canUseUserRunDir) {
		// the user run dir is set and executable, set jna.tempdir to XDG_RUNTIME_DIR
		LOG.debug("setting jna.tmpdir to user run dir at {}", userRuntimeDir);
		System.getProperties().putIfAbsent("jna.tmpdir", userRuntimeDir);
	    } else {
		// user run dir is not set or noexec as well, use '~/.openecard/run' as last ressort
		LOG.debug("setting jna.tmpdir to be '~/.openecard/run' as last ressort");
		System.getProperties().putIfAbsent("jna.tmpdir", "~/.openecard/run");
	    }
	}
    }

}
