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

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import java.io.IOException;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NFC specific implementation of the TerminalFactory
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
public class AndroidNFCFactory implements org.openecard.common.ifd.scio.TerminalFactory {

    private static final String ALGORITHM = "AndroidNFC";

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNFCFactory.class);

    private final NFCCardTerminals terminals;
    private final AndroidNFCCardTerminal terminal;

    public AndroidNFCFactory() {
	LOG.info("Create new NFCFactory");
	this.terminal = new AndroidNFCCardTerminal();
	this.terminals = new NFCCardTerminals(terminal);
    }

    @Override
    public String getType() {
	return ALGORITHM;
    }

    @Override
    public SCIOTerminals terminals() {
	return terminals;
    }

    public void setNFCTag(Tag tag) throws IOException {
	final IsoDep isoTag = IsoDep.get(tag);

	final int timeout = isoTag.getTimeout();
	terminal.setNFCTag(isoTag, timeout);
    }

    /**
     * Set the nfc tag in the nfc card terminal.
     *
     * @param tag
     * @param timeout current timeout for transceive(byte[]) in milliseconds.
     */
    public void setNFCTag(Tag tag, int timeout) throws IOException {
	IsoDep isoDepTag = IsoDep.get(tag);
	isoDepTag.setTimeout(timeout);
	terminal.setNFCTag(isoDepTag, timeout);
    }

}
