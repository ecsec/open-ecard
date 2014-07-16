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
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class NFCCard implements SCIOCard {

    private static final Logger logger = LoggerFactory.getLogger(NFCCard.class);
    private final NFCCardChannel nfcCardChannel = new NFCCardChannel(this);
    protected IsoDep isodep;

    public NFCCard(IsoDep tag) {
	isodep = tag;
    }

    @Override
    public void beginExclusive() throws SCIOException {
	logger.warn("beginExclusive not supported");
    }

    @Override
    public void endExclusive() throws SCIOException {
	logger.warn("endExclusive not supported");
    }

    @Override
    public void disconnect(boolean arg0) throws SCIOException {
	try {
	    isodep.close();
	} catch (IOException e) {
	    throw new SCIOException("Disconnect failed", e);
	}
    }

    @Override
    public SCIOATR getATR() {
	logger.warn("getATR not supported");
	// for now there is no way to get the ATR in android nfc api
	return new SCIOATR(new byte[0]);
    }

    @Override
    public SCIOChannel getBasicChannel() {
	return this.nfcCardChannel;
    }

    @Override
    public String getProtocol() {
	logger.warn("getProtocol not supported");
	// for now theres no way to get the used protocol in android nfc api
	return "";
    }

    @Override
    public SCIOChannel openLogicalChannel() throws SCIOException {
	return nfcCardChannel;
    }

    @Override
    public byte[] transmitControlCommand(int arg0, byte[] arg1) throws SCIOException {
	logger.warn("transmitControlCommand not supported");
	return new byte[0];
    }

}
