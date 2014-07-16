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


/**
 * NFC implementation of smartcardio's Card interface.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class NFCCard implements SCIOCard {

    protected IsoDep isodep = null;
    private NFCCardChannel nfcCardChannel = new NFCCardChannel(this);

    public NFCCard(IsoDep tag) {
	this.isodep = tag;
    }

    @Override
    public void beginExclusive() throws SCIOException {
	// TODO
    }

    @Override
    public void disconnect(boolean arg0) throws SCIOException {
	try {
	    this.isodep.close();
	} catch (IOException e) {
	    throw new SCIOException("Disconnect failed", e);
	}
    }

    @Override
    public void endExclusive() throws SCIOException {
	// TODO
    }

    @Override
    public SCIOATR getATR() {
	// for now there is no way to get the ATR in android nfc api
	return new SCIOATR(new byte[0]);
    }

    @Override
    public SCIOChannel getBasicChannel() {
	return this.nfcCardChannel;
    }

    @Override
    public String getProtocol() {
	// for now theres no way to get the used protocol in android nfc api
	return "";
    }

    @Override
    public SCIOChannel openLogicalChannel() throws SCIOException {
	return this.nfcCardChannel;
    }

    @Override
    public byte[] transmitControlCommand(int arg0, byte[] arg1) throws SCIOException {
	return new byte[0];
    }

}
