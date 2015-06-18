/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

package org.openecard.scio.osx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * OS X specific PlatformPCSC.
 * For more information see {@link org.openecard.scio.osx}.
 *
 * @author Benedikt Biallowons
 */
public class PlatformPCSC {

    private static final String PCSC_JNI_LIBRARY_PATH = "/osx-pcsc-jni/";
    private static final String PCSC_JNI_LIBRARY_NAME = "libosxj2pcsc.dylib";
    private static final String PCSC_FRAMEWORK = "/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC";

    public static final int SCARD_PROTOCOL_T0  = 0x0001;
    public static final int SCARD_PROTOCOL_T1  = 0x0002;
    public static final int SCARD_PROTOCOL_RAW = 0x0004;
    public static final int SCARD_UNKNOWN      = 0x0001;
    public static final int SCARD_ABSENT       = 0x0002;
    public static final int SCARD_PRESENT      = 0x0004;
    public static final int SCARD_SWALLOWED    = 0x0008;
    public static final int SCARD_POWERED      = 0x0010;
    public static final int SCARD_NEGOTIABLE   = 0x0020;
    public static final int SCARD_SPECIFIC     = 0x0040;

    public static final Exception INIT_EXCEPTION;

    static {
	INIT_EXCEPTION = AccessController.doPrivileged(new PrivilegedAction<Exception>() {
	    @Override
	    public Exception run() {
		try {
		    String[] parts = PCSC_JNI_LIBRARY_NAME.split("\\.");

		    File tempFile = File.createTempFile(parts[0], parts[1]);
		    tempFile.deleteOnExit();

		    InputStream is = PlatformPCSC.class.getResourceAsStream(PCSC_JNI_LIBRARY_PATH + getLibrary());

		    if (is == null) {
			return new FileNotFoundException(getLibrary() + " not found.");
		    }

		    OutputStream os = new FileOutputStream(tempFile);

		    try {
			byte[] buffer = new byte[1024];
			int readBytes;

			while ((readBytes = is.read(buffer)) != -1) {
			    os.write(buffer, 0, readBytes);
			}

			System.load(tempFile.getAbsolutePath());
			initialize(PCSC_FRAMEWORK);
		    } finally {
			os.close();
			is.close();
		    }

		    return null;
		} catch (Exception e) {
		    return e;
		}
	    }
	});
    }

    /**
     * Chooses a suitable JNI library depending the current JRE.
     * 
     * @return library filename
     */
    private static String getLibrary() {
	String javaVersion = System.getProperty("java.version");

	if (javaVersion.startsWith("1.6")) {
	    return "jre6." + PCSC_JNI_LIBRARY_NAME;
	} else if (javaVersion.startsWith("1.7") || javaVersion.startsWith("1.8")) {
	    return "jre7_8." + PCSC_JNI_LIBRARY_NAME;
	}

	return null;
    }

    private static native void initialize(String libraryName);

}
