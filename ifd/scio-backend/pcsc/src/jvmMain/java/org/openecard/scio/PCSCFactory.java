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

package org.openecard.scio;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import javax.smartcardio.TerminalFactory;
import jnasmartcardio.Smartcardio;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.util.LinuxLibraryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich
 * @author Benedikt Biallowons
 */
public class PCSCFactory implements org.openecard.common.ifd.scio.TerminalFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PCSCFactory.class);
    private static final String ALGORITHM = "PC/SC";

    private final String osName;
    private TerminalFactory terminalFactory;

    /**
     * Default constructor with fixes for the faulty SmartcardIO library.
     *
     * @throws FileNotFoundException if pcsclite for Linux can't be found.
     * @throws NoSuchAlgorithmException if no PC/SC provider can be found.
     */
    public PCSCFactory() throws FileNotFoundException, NoSuchAlgorithmException {
	osName = System.getProperty("os.name");
	if (osName.startsWith("Linux")) {
	    File libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1");
	    System.setProperty("sun.security.smartcardio.library", libFile.getAbsolutePath());
	}

	try {
	    LOG.info("Trying to initialize PCSC subsystem.");
	    terminalFactory = TerminalFactory.getInstance(ALGORITHM, null, new Smartcardio());
	    LOG.info("Successfully initialized PCSC subsystem.");
	} catch (NoSuchAlgorithmException ex) {
	    LOG.error("Failed to initialize smartcard system.");
	    throw ex;
	}
    }

    @Override
    public String getType() {
	if (terminalFactory != null) {
	    return terminalFactory.getType();
	} else {
	    return "PC/SC";
	}
    }

    @Override
    public SCIOTerminals terminals() {
	return new PCSCTerminals(this);
    }

    TerminalFactory getRawFactory() {
	return terminalFactory;
    }

}
