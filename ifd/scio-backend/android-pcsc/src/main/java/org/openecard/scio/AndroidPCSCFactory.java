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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.smartcardio.CardTerminals;
import org.openecard.common.ifd.AndroidTerminalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.smartcardio.PCSC;
import sun.security.smartcardio.PCSCException;
import sun.security.smartcardio.PCSCTerminals;


/**
 * TerminalFactory for PC/SC on Android.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class AndroidPCSCFactory implements AndroidTerminalFactory {

    private static Logger logger = LoggerFactory.getLogger(AndroidPCSCFactory.class);

    private PCSCDRunner runner;
    private Context ctx;

    @Override
    public String getType() {
	return "Android PC/SC factory";
    }

    @Override
    public CardTerminals terminals() {
	PCSC.checkAvailable();
	try {
	    PCSCTerminals.initContext();
	} catch (PCSCException e) {
	    logger.error("Init PCSC Context failed.", e);
	}
	return new PCSCTerminals();
    }

    @Override
    public void stop() {
	runner.stop();
	kill_pcscd();
    }

    private void kill_pcscd() {
	File f = new File(ctx.getFilesDir() + "/pcscd/pcscd.pid");
	FileInputStream fis = null;
	if (f.exists()) {
	    try {
		// read pid
		fis = new FileInputStream(f);
		byte[] pid = new byte[fis.available()];
		int num = fis.read(pid);

		if (num > 0) {
		    // kill pcsc daemon
		    ProcessBuilder pb = new ProcessBuilder("kill", "-9", new String(pid, "UTF-8"));
		    pb.start();
		}

		// cleanup files
		String del = ctx.getFilesDir() + "/pcscd/*";
		ProcessBuilder pb = new ProcessBuilder("rm", "-r", del);
		pb.start();
	    } catch (IOException e) {
		logger.error("Killing the pcsc daemon or cleanup failed.", e);
	    } finally {
		try {
		    if (fis != null) {
			fis.close();
		    }
		} catch (IOException e) {
		    // ignore
		}
	    }
	}
    }

    @Override
    public void start(Object o) {
	ctx = (Context) o;
	ResourceUnpacker.unpackResources(ctx);
	kill_pcscd();
	try {
	    runner = new PCSCDRunner(ctx);
	    runner.start();
	} catch (IOException e) {
	    logger.error("Starting the pcsc daemon failed.", e);
	}
    }

}
