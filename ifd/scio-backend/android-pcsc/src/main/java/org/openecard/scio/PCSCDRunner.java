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

package org.openecard.scio;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Runs the pcsc daemon and monitors its output.
 * Provides functions to start and stop the pcscd daemon and redirects it output to the logger.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PCSCDRunner {

    private static Logger logger = LoggerFactory.getLogger(PCSCDRunner.class);
    private static final String LIBPCSCD = "/lib/libpcscd.so";

    private final String pathToExecutable;
    private final List<String> args = new ArrayList<String>();
    private final Object lock = new Object();

    private Process pcscdProcess;
    private String last_stdout_line;

    /**
     * Runs a binary from the assets folder and controls it.
     * 
     * @param ctx the application context to get the files directory
     * @throws IOException executing 'chmod' failed
     */
    public PCSCDRunner(final Context ctx) throws IOException {
	pathToExecutable = ctx.getFilesDir().getParent() + LIBPCSCD;
	args.add(pathToExecutable);
	args.add("-f"); // run pcsc daemon in foreground
	args.add("--info"); // set debug level to info

	Runtime.getRuntime().exec("chmod 777 " + pathToExecutable);
	Runtime.getRuntime().exec("chmod +X " + pathToExecutable);
    }

    /**
     * Start the pcsc daemon if it is not running.
     * 
     * @throws IOException if the pcsc daemon could not be started due to an I/O error
     */
    public void start() throws IOException {
	Integer ev = null;
	try {
	    ev = pcscdProcess.exitValue();
	} catch (IllegalThreadStateException e) {
	    // ignore
	} catch (NullPointerException e) {
	    // ignore
	}

	if (pcscdProcess == null || ev != null) {
	    final ProcessBuilder pb = new ProcessBuilder(args);
	    pb.redirectErrorStream(true);
	    pcscdProcess = pb.start();
	    startOutputRedirection(pcscdProcess.getInputStream());
	}
    }

    /**
     * Return the running status of the pcsc daemon.
     * 
     * @return {@code true} if the pcsc daemon is running, otherwise {@code false}
     */
    public boolean isRunning() {
	Integer ev = null;
	try {
	    ev = pcscdProcess.exitValue();
	} catch (IllegalThreadStateException e) {
	    // ignore
	} catch (NullPointerException e) {
	    // ignore
	}

	return pcscdProcess != null && ev == null;
    }

    /**
     * Redirect the output of the pcsc daemon to the logger.
     * 
     * @param in InputStream containing the output from the pcsc daemon
     */
    private void startOutputRedirection(final InputStream in) {
	Thread t = new Thread() {
	    public void run() {
		try {
		    final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

		    while (true) {
			synchronized (lock) {
			    if ((last_stdout_line = reader.readLine()) == null) {
				break;
			    }
			}

			logger.debug(last_stdout_line);
		    }

		    reader.close();
		} catch (IOException e) {
		    // ignore
		}

		if (pcscdProcess != null) {
		    try {
			pcscdProcess.waitFor();
		    } catch (InterruptedException e) {
			// ignore
		    }

		    logger.debug("exit(" + pcscdProcess.exitValue() + ")");
		}
	    }
	};
	t.start();
    }

    /**
     * Stop the pcscd process.
     */
    public void stop() {
	pcscdProcess.destroy();
    }

}
