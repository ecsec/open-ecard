/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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
 * Task which checks if the received nfc tag is still available. If the nfc tag is no longer available, then the nfc tag
 * is removed and the card object is removed from the NFCCardTerminal, see
 * {@link org.openecard.scio.NFCCardTerminal#removeTag()}.
 *
 * @author Mike Prechtl
 */
public class NFCCardMonitoring implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardMonitoring.class);

    private final NFCCardTerminal terminal;
    private final AbstractNFCCard card;
    private final Object lock = new Object();
    private volatile boolean wasSignalled = false;

    public NFCCardMonitoring(NFCCardTerminal terminal, AbstractNFCCard card) {
	this.terminal = terminal;
	this.card = card;
    }

    @Override
    public void run() {
	LOG.debug("Starting monitor thread.");

	synchronized (lock) {
	    while (!wasSignalled) {
		try {
		    if (!card.isTagPresent()) {
			// remove tag if card is no longer available/connected to terminal
			terminal.removeTag();
			LOG.debug("Stopping monitor thread.");
			return;
		    }
		    lock.wait(250);
		} catch (InterruptedException ex) {
		    LOG.warn("Task which checks the availability of the nfc card is interrupted.", ex);
		    LOG.debug("Stopping monitor thread.");
		    return;
		}
	    }
	}
    }

    public void notifyStopMonitoring() {
	synchronized (lock) {
	    wasSignalled = true;
	    lock.notifyAll();
	}
    }
}
