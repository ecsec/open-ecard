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

import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of smartcardio's CardTerminal interface.
 * Implemented as singleton because we only have one nfc-interface. Only
 * activitys can react on a new intent, so they must set the tag via setTag()
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
public abstract class NFCCardTerminal implements SCIOTerminal {

    public static final String STD_TERMINAL_NAME = "Integrated NFC";

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardTerminal.class);

    private AbstractNFCCard nfcCard;

    private final String terminalName;
    private final Object cardPresent;
    private final Object cardAbsent;

    public NFCCardTerminal() {
	this.terminalName = STD_TERMINAL_NAME;
	this.cardAbsent = new Object();
	this.cardPresent = new Object();
    }

    @Override
    public String getName() {
	return terminalName;
    }

    public abstract void prepareDevices();

    public void powerDownDevices() {
	this.removeTag();
    }

    public void setDialogMsg(String dialogMsg) {
	AbstractNFCCard currentCard = this.nfcCard;
	if (currentCard != null) {
	    currentCard.setDialogMsg(dialogMsg);
	}
    }

    @Override
    public synchronized boolean isCardPresent() {
	return nfcCard != null;
    }

    public synchronized void setNFCCard(AbstractNFCCard card) {
	this.nfcCard = card;
	if (card == null) {
	    notifyCardAbsent();
	} else {
	    notifyCardPresent();
	}
    }

    public synchronized void removeTag() {
	final AbstractNFCCard currentCard = nfcCard;
	if (currentCard != null) {
	    nfcCard = null;
	    removeTag(currentCard);
	}
    }

    private void removeTag(final AbstractNFCCard currentCard) {
	if (currentCard != null) { // maybe nfc tag is already removed
	    LOG.info("Removing NFC Tag and terminating card connection.");
	    try {
		currentCard.terminateTag();
	    } catch (SCIOException ex) {
		LOG.error("Disconnect failed.", ex);
	    }
	    notifyCardAbsent();
	} else {
	    LOG.warn("Double invocation of removeTag function.");
	}
    }

    private void notifyCardPresent() {
	synchronized (cardPresent) {
	    cardPresent.notifyAll();
	}
    }

    private void notifyCardAbsent() {
	synchronized (cardAbsent) {
	    cardAbsent.notifyAll();
	}
    }

    @Override
    public synchronized SCIOCard connect(SCIOProtocol protocol) throws SCIOException, IllegalStateException {
	final AbstractNFCCard currentCard = nfcCard;
	if (currentCard == null) {
	    String msg = "No tag present.";
	    LOG.warn(msg);
	    throw new SCIOException(msg, SCIOErrorCode.SCARD_E_NO_SMARTCARD);
	}
	return currentCard;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws SCIOException {
	long startTime = System.nanoTime() / 1000_000;
	boolean absent = ! isCardPresent();
	if (absent) {
	    LOG.debug("Card already absent...");
	    return absent;
	}
	LOG.debug("Waiting for card absent...");
	try {
	    synchronized (cardAbsent) {
		while (isCardPresent()) {
		    // wait only if timeout is not finished
		    long curTime = System.nanoTime() / 1000_000;
		    long waitTime = timeout - (curTime - startTime);
		    if (waitTime < 0) {
			break;
		    }
		    cardAbsent.wait(timeout);
		}
	    }
	} catch (InterruptedException ex) {
	    LOG.warn("Waiting for card absent interrupted.");
	}
	final boolean nowAbsent = ! isCardPresent();
	LOG.debug("Notifying card is absent: {}", nowAbsent);
	return nowAbsent;
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws SCIOException {
	long startTime = System.nanoTime() / 1000_000;
	boolean present = isCardPresent();
	if (present) {
	    LOG.debug("Card already present...");
	    return present;
	}
	LOG.debug("Waiting for card present...");
	try {
	    synchronized(cardPresent) {
		while (! isCardPresent()) {
		    // wait only if timeout is not finished
		    long curTime = System.nanoTime() / 1000_000;
		    long waitTime = timeout - (curTime - startTime);
		    if (waitTime < 0) {
			break;
		    }
		    cardPresent.wait(timeout);
		}
	    }
	} catch (InterruptedException ex) {
	    LOG.warn("Waiting for card present interrupted.");
	}
	final boolean nowPresent = isCardPresent();
	LOG.debug("Notifying card is present: {}", nowPresent);
	return nowPresent;
    }

}
