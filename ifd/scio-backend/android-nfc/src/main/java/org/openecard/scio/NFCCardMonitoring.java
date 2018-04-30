/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Task which checks if the received nfc tag is still available. If the nfc tag is no longer available, then the nfc
 * tag is removed and the card object is removed from the NFCCardTerminal, see
 * {@link org.openecard.scio.NFCCardTerminal#removeTag()}.
 *
 * @author Mike Prechtl
 */
public class NFCCardMonitoring implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardMonitoring.class);

    private final NFCCardTerminal terminal;

    public NFCCardMonitoring(NFCCardTerminal terminal) {
	this.terminal = terminal;
    }

    @Override
    public void run() {
	while (true) {
	    try {
		Thread.sleep(250);
		if (! terminal.isCardConnected()) {
		    // remove tag if card is no longer available/connected to terminal
		    terminal.removeTag();
		    break;
		}
	    } catch (InterruptedException ex) {
		LOG.warn("Task which checks the availability of the nfc card is interrupted.", ex);
	    }
	}
    }

}
