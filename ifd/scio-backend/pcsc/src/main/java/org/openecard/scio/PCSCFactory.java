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

package org.openecard.scio;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.openecard.common.util.LinuxLibraryFinder;
import org.openecard.scio.osx.SunOSXPCSC;


/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class PCSCFactory implements org.openecard.common.ifd.TerminalFactory {

    private static final String ALGORITHM = "PC/SC";

    private final TerminalFactory terminalFactory;

    /**
     * Default constructor with fixes for the faulty SmartcardIO library.
     *
     * @throws FileNotFoundException if pcsclite for Linux can't be found.
     * @throws NoSuchAlgorithmException if no PC/SC provider can be found.
     */
    public PCSCFactory() throws FileNotFoundException, NoSuchAlgorithmException {
	String osName = System.getProperty("os.name");
	if (osName.startsWith("Linux")) {
	    File libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1");
	    System.setProperty("sun.security.smartcardio.library", libFile.getAbsolutePath());
	// see https://developer.apple.com/library/mac/technotes/tn2002/tn2110.html#FINDINGMAC
	} else if (osName.contains("OS X")) {
	    terminalFactory = TerminalFactory.getInstance(ALGORITHM, null, new SunOSXPCSC());
	    return;
	}
	terminalFactory = TerminalFactory.getInstance(ALGORITHM, null);
    }

    @Override
    public String getType() {
	return terminalFactory.getType();
    }

    @Override
    public CardTerminals terminals() {
	return terminalFactory.terminals();
    }

}
