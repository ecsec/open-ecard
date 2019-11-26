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

    private IOSConfig config;


    public IOSNFCFactory(IOSConfig config) {
	this.terminal = new IOSNFCCardTerminal();
	this.terminals = new NFCCardTerminals(terminal);
	this.terminal.setConfig(config);
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

    public void setStaticConfig(IOSConfig config) {
	this.config = config;
	this.terminal.setConfig(config);
    }

    public void setDialogMsg(String dialogMsg) {
	terminal.setDialogMsg(dialogMsg);
    }

}
