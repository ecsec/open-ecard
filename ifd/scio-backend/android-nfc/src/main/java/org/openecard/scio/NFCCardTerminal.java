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

import android.nfc.tech.IsoDep;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of smartcardio's CardTerminal interface.<br>
 * Implemented as singleton because we only have one nfc-interface. Only
 * activitys can react on a new intent, so they must set the tag via setTag()
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
public class NFCCardTerminal implements SCIOTerminal {

    public static final String STD_TERMINAL_NAME = "Integrated NFC";

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardTerminal.class);
    private static final HashMap<String, NFCCardTerminal> TERMINALS = new HashMap<>();
    private NFCCard nfcCard;

    private final String terminalName;
    private final Object cardPresent;
    private final Object cardAbsent;

    private NFCCardTerminal(String name) {
	this.terminalName = name;
	this.cardAbsent = new Object();
	this.cardPresent = new Object();
    }

    /**
     * Returns the NFCCardTerminal-Instance.
     *
     * @param name of the card terminal
     * @return The NFCCardTerminal-Instance
     */
    public static synchronized NFCCardTerminal getInstance(@Nonnull String name) {
	if (! TERMINALS.containsKey(name)) {
	    NFCCardTerminal terminal = new NFCCardTerminal(name);
	    TERMINALS.put(name, terminal);
	}
	return TERMINALS.get(name);
    }

    public static synchronized NFCCardTerminal getInstance() {
	return getInstance(STD_TERMINAL_NAME);
    }

    public static synchronized Map<String, NFCCardTerminal> getTerminals() {
	return Collections.unmodifiableMap(TERMINALS);
    }

    public int getLengthOfLastAPDU() {
	return ((NFCCardChannel) nfcCard.getBasicChannel()).getLengthOfLastAPDU();
    }

    public int getMaxTransceiveLength() {
	return nfcCard.isodep.getMaxTransceiveLength();
    }

    public synchronized void setTag(IsoDep tag, int timeout) throws SCIOException {
	LOG.debug("Set nfc tag on terminal '" + getName() + "'");
	if (tag.isExtendedLengthApduSupported()) {
	    LOG.debug("Max Transceive Length: " + tag.getMaxTransceiveLength() + " Bytes.");
	    LOG.debug("Extended Length APDU is supported.");
	    this.nfcCard = new NFCCard(tag, timeout, this);
	    notifyCardPresent();
	} else {
	    String msg = "Extended Length APDU is not supported.";
	    throw new SCIOException(msg, SCIOErrorCode.SCARD_E_READER_UNSUPPORTED);
	}
    }

    public synchronized void removeTag() {
	try {
	    nfcCard.disconnect(true);
	} catch (SCIOException ex) {
	    LOG.error("Disconnect failed.", ex);
	}
	this.nfcCard = null;
	notifyCardAbsent();
    }

    public void notifyCardPresent() {
	synchronized(cardPresent) {
	    cardPresent.notifyAll();
	}
    }

    public void notifyCardAbsent() {
	synchronized(cardAbsent) {
	    cardAbsent.notifyAll();
	}
    }

    @Override
    public synchronized SCIOCard connect(SCIOProtocol protocol) throws SCIOException, IllegalStateException {
	if (this.nfcCard.isodep == null) {
	    String msg = "No tag present.";
	    LOG.warn(msg);
	    throw new SCIOException(msg, SCIOErrorCode.SCARD_E_NO_SMARTCARD);
	}
	try {
	    if (! nfcCard.isodep.isConnected()) {
		nfcCard.isodep.setTimeout(3000);
		nfcCard.isodep.connect();

		// start thread which is monitoring the availability of the card
		Thread nfcAvailableTask = new Thread(new NFCCardMonitoring((this)));
		nfcAvailableTask.start();
	    }
	} catch (IOException e) {
	    nfcCard = null;
	    String msg = "No connection can be established.";
	    LOG.warn(msg, e);
	    // TODO: check if error code is correct
	    throw new SCIOException(msg, SCIOErrorCode.SCARD_E_NO_SMARTCARD, e);
	}
	return nfcCard;
    }

    @Override
    public String getName() {
	return terminalName;
    }

    public synchronized boolean isCardConnected() {
	return nfcCard != null && nfcCard.isodep != null && nfcCard.isodep.isConnected();
    }

    @Override
    public synchronized boolean isCardPresent() throws SCIOException {
	return nfcCard != null && nfcCard.isodep != null;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws SCIOException {
	boolean absent = ! isCardPresent();
	if (absent) {
	    LOG.debug("Card already absent...");
	    return absent;
	}
	LOG.debug("Waiting for card absent...");
	try {
	    synchronized(cardAbsent) {
		while (isCardPresent()) {
		    cardAbsent.wait(timeout);
		}
	    }
	} catch (InterruptedException ex) {
	    LOG.warn("Waiting for card absent interrupted.");
	}
	return ! isCardPresent();
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws SCIOException {
	boolean present = isCardPresent();
	if (present) {
	    LOG.debug("Card already present...");
	    return present;
	}
	LOG.debug("Waiting for card present...");
	try {
	    synchronized(cardPresent) {
		while (! isCardPresent()) {
		    cardPresent.wait(timeout);
		}
	    }
	} catch (InterruptedException ex) {
	    LOG.warn("Waiting for card present interrupted.");
	}
	return isCardPresent();
    }

}
