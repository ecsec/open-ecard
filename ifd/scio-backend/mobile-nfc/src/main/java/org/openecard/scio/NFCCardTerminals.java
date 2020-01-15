/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.SCIOTerminals.State;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of smartcardio's CardTerminals interface.
 *
 * @author Dirk Petrautzki
 * @author Daniel Nemmert
 * @author Mike Prechtl
 */
public final class NFCCardTerminals implements SCIOTerminals {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardTerminals.class);

    private final NFCCardTerminal nfcTerminal;

    public NFCCardTerminals(NFCCardTerminal terminal) {
	this.nfcTerminal = terminal;
    }

    @Override
    public boolean prepareDevices() throws SCIOException {
	return this.nfcTerminal.prepareDevices();
    }

    @Override
    public boolean powerDownDevices() {
	return this.nfcTerminal.powerDownDevices();
    }

    @Override
    public List<SCIOTerminal> list(State state) throws SCIOException {
	switch (state) {
	    case ALL:
		return Collections.singletonList(this.nfcTerminal);
	    case CARD_ABSENT:
		if (! nfcTerminal.isCardPresent()) {
		    return Collections.singletonList(this.nfcTerminal);
		}
		break;
	    case CARD_PRESENT:
		if (nfcTerminal.isCardPresent()) {
		    return Collections.singletonList(this.nfcTerminal);
		}
		break;
	}
	return Collections.emptyList();
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
        return list(State.ALL);
    }

    @Override
    public SCIOTerminal getTerminal(@Nonnull String name) throws NoSuchTerminal {
        if (nfcTerminal.getName().equals(name)) {
	    return this.nfcTerminal;
	}
	String errorMsg = String.format("There is no terminal with the name '%s' available.", name);
	throw new NoSuchTerminal(errorMsg);
    }

    @Override
    public TerminalWatcher getWatcher() throws SCIOException {
	return new NFCCardWatcher(this, nfcTerminal);
    }
}
