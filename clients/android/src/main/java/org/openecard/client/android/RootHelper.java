/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class provides helper functions for executing commands with root rights
 * on android.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class RootHelper {

    // logwrapper redirects output from stdout to logcat
    private static final String prefix = "logwrapper ";

    /**
     * Executes a command as root and uses logwrapper to redirect output to
     * logcat.
     * 
     * @param command the command that should be executed as root
     * @return the exit value of the native process
     */
    public static int executeAsRoot(String command) {
	try {
	    Process sh = Runtime.getRuntime().exec("su", null, null);
	    OutputStream os = sh.getOutputStream();
	    // prefix the command to see output in logcat
	    writeCommand(os, prefix + command);
	    writeCommand(os, "exit");
	    int exitValue = sh.waitFor();
	    return exitValue;
	} catch (IOException e) {
	    // TODO log error
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    // TODO log error
	    e.printStackTrace();
	}
	return -1;
    }

    private static void writeCommand(OutputStream os, String command) throws IOException {
	os.write((command + "\n").getBytes("ASCII"));
    }

    public static void startPCSCD(File filesDir) {
	// kill old instances of pcsc daemon if any
	killPCSCD();
	String pcscd_exec = filesDir.getParent() + "/lib/libpcscd.so";
	File f = new File(pcscd_exec);
	String cmd[] = { "chmod", "777", f.getAbsolutePath() };
	try {
	    Process p = Runtime.getRuntime().exec(cmd);

	    p.waitFor();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	RootHelper.executeAsRoot(pcscd_exec);
    }

    public static void killPCSCD() {
	File f = new File("/data/pcscd/pcscd.pid");
	if (f.exists()) {
	    try {
		FileInputStream fis = new FileInputStream(f);
		byte[] pid = new byte[fis.available()];
		fis.read(pid);
		RootHelper.executeAsRoot("kill -9 " + new String(pid));
	    } catch (Exception e) {
		e.printStackTrace();
		// TODO
	    }
	}
    }

}
