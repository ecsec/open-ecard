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

package org.openecard.scio;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.util.LinuxLibraryFinder;
import org.openecard.scio.osx.SunOSXPCSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich
 * @author Benedikt Biallowons
 */
public class PCSCFactory implements org.openecard.common.ifd.scio.TerminalFactory {

    private static final Logger logger = LoggerFactory.getLogger(PCSCFactory.class);
    private static final String ALGORITHM = "PC/SC";

    private final String osName;
    private final String osVersion;
    private TerminalFactory terminalFactory;

    /**
     * Default constructor with fixes for the faulty SmartcardIO library.
     *
     * @throws FileNotFoundException if pcsclite for Linux can't be found.
     * @throws NoSuchAlgorithmException if no PC/SC provider can be found.
     */
    public PCSCFactory() throws FileNotFoundException, NoSuchAlgorithmException {
	osName = System.getProperty("os.name");
	osVersion = System.getProperty("os.version");
	if (osName.startsWith("Linux")) {
	    File libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1");
	    System.setProperty("sun.security.smartcardio.library", libFile.getAbsolutePath());
	}
	loadPCSC();
    }

    @Override
    public String getType() {
	return terminalFactory.getType();
    }

    @Override
    public SCIOTerminals terminals() {
	return new PCSCTerminals(this);
    }

    TerminalFactory getRawFactory() {
	return terminalFactory;
    }

    private static Integer versionCompare(String str1, String str2) {
	// code taken from http://stackoverflow.com/a/6702029
	String[] vals1 = str1.split("\\.");
	String[] vals2 = str2.split("\\.");
	int i = 0;

	while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
	    i++;
	}

	if (i < vals1.length && i < vals2.length) {
	    return Integer.signum(Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i])));
	}

	return Integer.signum(vals1.length - vals2.length);
    }

    final void loadPCSC() throws NoSuchAlgorithmException {
	if (osName.contains("OS X") && versionCompare(osVersion, "10.10") < 0) {
	    // see https://developer.apple.com/library/mac/technotes/tn2002/tn2110.html#FINDINGMAC
	    terminalFactory = TerminalFactory.getInstance(ALGORITHM, null, new SunOSXPCSC());
	} else {
	    terminalFactory = TerminalFactory.getInstance(ALGORITHM, null);
	}
    }

    void reloadPCSC() {
	try {
	    // code taken from http://stackoverflow.com/questions/16921785/
	    Class pcscterminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
	    Field contextId = pcscterminal.getDeclaredField("contextId");
	    contextId.setAccessible(true);

	    if (contextId.getLong(pcscterminal) != 0L) {
		// First get a new context value
		Class pcsc = Class.forName("sun.security.smartcardio.PCSC");
		Method SCardEstablishContext = pcsc.getDeclaredMethod("SCardEstablishContext", Integer.TYPE);
		SCardEstablishContext.setAccessible(true);

		Field SCARD_SCOPE_USER = pcsc.getDeclaredField("SCARD_SCOPE_USER");
		SCARD_SCOPE_USER.setAccessible(true);

		long newId = ((Long) SCardEstablishContext.invoke(pcsc, SCARD_SCOPE_USER.getInt(pcsc)));
		contextId.setLong(pcscterminal, newId);

		// Then clear the terminals in cache
		loadPCSC();
		CardTerminals terminals = terminalFactory.terminals();
		Field fieldTerminals = pcscterminal.getDeclaredField("terminals");
		fieldTerminals.setAccessible(true);
		Class classMap = Class.forName("java.util.Map");
		Method clearMap = classMap.getDeclaredMethod("clear");

		clearMap.invoke(fieldTerminals.get(terminals));
	    }
	} catch (NoSuchAlgorithmException ex) {
	    // if it worked once it will work again
	    String msg = "PCSC changed it's algorithm. There is something really wrong.";
	    logger.error(msg, ex);
	    throw new RuntimeException("PCSC changed it's algorithm. There is something really wrong.");
	} catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchFieldException
		| NoSuchMethodException | SecurityException ex) {
	    logger.error("Failed to perform reflection magic to reload TerminalFactory.", ex);
	}
    }

}
