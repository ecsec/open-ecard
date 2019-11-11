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
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
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

    private static NFCCardTerminals terminals;
    private static AndroidNFCCardTerminal terminal;

    public AndroidNFCFactory() throws NoSuchTerminal {
	LOG.info("Create new NFCFactory");
	terminal = new AndroidNFCCardTerminal();
	terminals = new NFCCardTerminals(terminal);
    }

    @Override
    public String getType() {
	return ALGORITHM;
    }

    @Override
    public SCIOTerminals terminals() {
	return terminals;
    }

    /**
     * Return the names of the nfc terminals. Should return at least one card terminal (the integrated one).
     *
     * @return list of terminal names.
     */
    public static List<String> getTerminalNames() {
	List<String> terminalNames = new ArrayList<>();
	try {
	    List<SCIOTerminal> nfcTerminals = terminals.list();
	    for (SCIOTerminal nfcTerminal : nfcTerminals) {
		terminalNames.add(nfcTerminal.getName());
	    }
	} catch (SCIOException ex) {
	    LOG.warn(ex.getMessage(), ex);
	}
	return terminalNames;
    }

    public static void setNFCTag(Tag tag) throws IOException {
	AndroidNFCCardTerminal staticInstance = terminal;
	if (staticInstance != null) {
	    final IsoDep isoTag = IsoDep.get(tag);

	    final int timeout = isoTag.getTimeout();
	    staticInstance.setNFCTag(isoTag, timeout);
	}
    }

    /**
     * Set the nfc tag in the nfc card terminal.
     *
     * @param tag
     * @param timeout current timeout for transceive(byte[]) in milliseconds.
     */
    public static void setNFCTag(Tag tag, int timeout) throws IOException {
	AndroidNFCCardTerminal staticInstance = terminal;
	if (staticInstance != null) {
	    IsoDep isoDepTag = IsoDep.get(tag);
	    isoDepTag.setTimeout(timeout);
	    staticInstance.setNFCTag(isoDepTag, timeout);

	}
    }

}
