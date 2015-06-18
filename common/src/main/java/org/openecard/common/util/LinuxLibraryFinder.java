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

package org.openecard.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to find shared object (.so) libraries in linux systems.
 * The algorithm to find the objects is the same as used in the dynamic loader ld. There is one exception to this rule,
 * the ELF variables DT_RPATH and DT_RUNPATH are not evaluated, because they are not available in java.
 *
 * @author Tobias Wich
 */
public class LinuxLibraryFinder {

    private static final Logger logger = LoggerFactory.getLogger(LinuxLibraryFinder.class);

    /**
     * Gets a file object pointing to the library which has been searched.
     * On success, the file points to first file found which is readable and thus can be used.
     * <p>The algorithm to find the library can be found in the ld.so(8) manpage and is as follows:</p>
     * <ol>
     * <li>Check paths in {@code LD_LIBRARY_PATH} environment variable.</li>
     * <li>Check for library in {@code /etc/ld.so.cache} by executing {@code ldconfig -p} and searching the output.</li>
     * <li>Check the base library paths {@code /lib} and {@code /usr/lib} or {@code /lib64} and {@code /usr/lib64}
     * depending on the architecture.</li>
     * </ol>
     *
     * @param name Name of the library, such as pcsclite.
     * @param version Version suffix such as 1, 1.0 or null if no suffix is desired.
     * @return The file object to the library.
     * @throws java.io.FileNotFoundException Thrown if the requested library could not be found.
     */
    @Nonnull
    public static File getLibraryPath(@Nonnull String name, @Nullable String version) throws FileNotFoundException {
	// add version only if it has a meaningful value
	version = version == null ? "" : version;
	version = version.isEmpty() ? "" : ("." + version);
	String libname = System.mapLibraryName(name) + version;

	File result;
	// LD_LIBRARY_PATH
	result = findInEnv(libname, System.getenv("LD_LIBRARY_PATH"));
	if (result != null) {
	    return result;
	}
	// ld.so.cache
	result = findInLdCache(libname);
	if (result != null) {
	    return result;
	}
	// base lib paths
	result = findInBaseLibPaths(libname);
	if (result != null) {
	    return result;
	}

	throw new FileNotFoundException("Library " + libname + " not found on your system.");
    }

    @Nullable
    private static File findInEnv(@Nonnull String libname, @Nullable String env) {
	if (env != null) {
	    for (String path : env.split(":")) {
		// append lib to path and see if it exists
		String result = path + "/" + libname;
		File test = new File(result.trim());
		if (test.canRead()) {
		    return test;
		}
	    }
	}
	// nothing found
	return null;
    }

    @Nullable
    private static File findInLdCache(@Nonnull String libname) {
	String ldconfExec = findProgramFile("ldconfig") + " -p";
	Process p = null;
	try {
	    p = Runtime.getRuntime().exec(ldconfExec);
	    InputStream cacheData = p.getInputStream();
	    BufferedReader cacheDataReader = new BufferedReader(new InputStreamReader(cacheData));
	    String next;
	    while ((next = cacheDataReader.readLine()) != null) {
		if (next.endsWith(libname)) {
		    // extract library path from entry
		    // the line can look like this:
		    // libpcsclite.so.1 (libc6,x86-64) => /usr/lib/x86_64-linux-gnu/libpcsclite.so.1
		    int endIdx = next.lastIndexOf("=>");
		    if (endIdx != -1) {
			String result = next.substring(endIdx + 2);
			File test = new File(result.trim());
			if (test.canRead()) {
			    return test;
			}
		    }
		}
	    }
	} catch (IOException ex) {
	    logger.debug("Library {} not found in ld.so.cache.", libname);
	} finally {
	    if (p != null) {
		try {
		    p.getInputStream().close();
		} catch (IOException ex) {
		    // dafuq?!?
		}
		try {
		    p.getOutputStream().close();
		} catch (IOException ex) {
		    // dafuq?!?
		}
		try {
		    p.getErrorStream().close();
		} catch (IOException ex) {
		    // dafuq?!?
		}
	    }
	}
	return null;
    }

    @Nonnull
    private static String findProgramFile(@Nonnull String name) {
	String path = System.getenv().get("PATH");
	path = path == null ? "" : path;
	path = "/sbin:/usr/sbin:" + path;
	// loop through entries and find program
	for (String entry : path.split(":")) {
	    String fname = entry + "/" + name;
	    File f = new File(fname);
	    if (f.canExecute()) {
		return fname;
	    }
	}
	// nothing found, maybe the file is in the same path
	return name;
    }

    @Nullable
    private static File findInBaseLibPaths(@Nonnull String libname) {
	String archSuffix = "64".equals(System.getProperty("sun.arch.data.model")) ? "64" : "";
	String[] basePaths = {"/lib" + archSuffix, "/usr/lib" + archSuffix};
	// look for lib in those paths
	for (String path : basePaths) {
	    String fname = path + "/" + libname;
	    File test = new File(fname);
	    if (test.canRead()) {
		return test;
	    }
	}
	return null;
    }

}
