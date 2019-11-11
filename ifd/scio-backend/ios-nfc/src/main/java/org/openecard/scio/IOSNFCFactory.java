/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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

import java.io.IOException;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC specific implementation of the TerminalFactory
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
public class IOSNFCFactory implements org.openecard.common.ifd.scio.TerminalFactory {

    private static final String ALGORITHM = "IOSNFC";

    private static final Logger LOG = LoggerFactory.getLogger(IOSNFCFactory.class);
    private final IOSNFCCardTerminal terminal;
    private final NFCCardTerminals terminals;

    private static IOSNFCFactory staticInstance;
    private static IOSConfig staticConfig;


    public IOSNFCFactory() throws NoSuchTerminal, IOException {
	this.terminal = new IOSNFCCardTerminal();
	this.terminals = new NFCCardTerminals(terminal);
	staticInstance = this;
	IOSConfig givenConfig = staticConfig;
	if (givenConfig != null) {
	    this.terminal.setConfig(givenConfig);
	}
    }

    @Override
    public String getType() {
	return ALGORITHM;
    }

    @Override
    public SCIOTerminals terminals() {
	return terminals;
    }

    public void setConfig(IOSConfig config) {
	this.terminal.setConfig(config);
    }

    public static void setStaticConfig(IOSConfig config) {
	staticConfig = config;
	IOSNFCFactory givenInstance = staticInstance;
	if (givenInstance != null) {
	    givenInstance.setConfig(config);
	}
    }

    public static void setDialogMsg(String dialogMsg) {
	IOSNFCFactory givenInstance = staticInstance;
	if (givenInstance != null) {
	    givenInstance.terminal.setDialogMsg(dialogMsg);
	}
    }

}
