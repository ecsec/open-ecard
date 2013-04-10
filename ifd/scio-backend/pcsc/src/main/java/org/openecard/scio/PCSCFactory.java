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

import org.openecard.common.util.LinuxLibraryFinder;
import java.io.File;
import java.io.FileNotFoundException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;


/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PCSCFactory implements org.openecard.common.ifd.TerminalFactory {

    /**
     * Default constructor with fixes for the faulty SmartcardIO library.
     */
    public PCSCFactory() throws FileNotFoundException {
	String osName = System.getProperty("os.name");
	if (osName.startsWith("Linux")) {
	    File libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1");
	    System.setProperty("sun.security.smartcardio.library", libFile.getAbsolutePath());
	}
    }

    @Override
    public String getType() {
	return TerminalFactory.getDefault().getType();
    }

    @Override
    public CardTerminals terminals() {
	return TerminalFactory.getDefault().terminals();
    }

}
