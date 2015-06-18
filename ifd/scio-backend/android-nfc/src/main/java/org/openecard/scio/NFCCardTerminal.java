/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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
 */
public class NFCCardTerminal implements SCIOTerminal {

    private static final Logger logger = LoggerFactory.getLogger(NFCCardTerminal.class);
    private static NFCCardTerminal instance;
    private NFCCard nfcCard;

    /**
     * Returns the NFCCardTerminal-Instance.
     *
     * @return The NFCCardTerminal-Instance
     */
    public static synchronized NFCCardTerminal getInstance() {
	if (instance == null) {
	    instance = new NFCCardTerminal();
	}
	return instance;
    }

    public int getLengthOfLastAPDU() {
	return ((NFCCardChannel) nfcCard.getBasicChannel()).getLengthOfLastAPDU();
    }

    public int getMaxTransceiveLength() {
	return nfcCard.isodep.getMaxTransceiveLength();
    }

    public synchronized void setTag(IsoDep tag) {
	nfcCard = new NFCCard(tag);
    }

    @Override
    public synchronized SCIOCard connect(SCIOProtocol protocol) throws SCIOException, IllegalStateException {
	if (nfcCard == null || this.nfcCard.isodep == null) {
	    String msg = "No tag present.";
	    logger.warn(msg);
	    throw new SCIOException(msg, SCIOErrorCode.SCARD_E_NO_SMARTCARD);
	}
	try {
	    if (! nfcCard.isodep.isConnected()) {
		nfcCard.isodep.setTimeout(3000);
		nfcCard.isodep.connect();
	    }
	} catch (IOException e) {
	    nfcCard = null;
	    // TODO: check if error code is correct
	    throw new SCIOException("No connection could be established", SCIOErrorCode.SCARD_E_NO_SMARTCARD, e);
	}
	return nfcCard;
    }

    @Override
    public String getName() {
	return "Integrated NFC";
    }

    @Override
    public synchronized boolean isCardPresent() throws SCIOException {
	return nfcCard != null && nfcCard.isodep != null && nfcCard.isodep.isConnected();
    }

    @Override
    public boolean waitForCardAbsent(long arg0) throws SCIOException {
	logger.warn("waitForCardAbsent not supported");
	return false;
    }

    @Override
    public boolean waitForCardPresent(long arg0) throws SCIOException {
	logger.warn("waitForCardPresent not supported");
	return false;
    }

}
