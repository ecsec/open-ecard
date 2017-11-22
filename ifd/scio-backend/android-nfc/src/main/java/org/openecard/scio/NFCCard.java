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
import java.io.ByteArrayOutputStream;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Dirk Petrautzki
 */
public class NFCCard implements SCIOCard {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCard.class);
    private final NFCCardChannel nfcCardChannel = new NFCCardChannel(this);
    private final NFCCardTerminal nfcCardTerminal;
    private final int timeoutForTransceive;
    protected IsoDep isodep;

    public NFCCard(IsoDep tag, int timeout, NFCCardTerminal terminal) {
	isodep = tag;
	timeoutForTransceive = timeout;
	nfcCardTerminal = terminal;
    }

    public int getTimeoutForTransceive() {
	return timeoutForTransceive;
    }

    @Override
    public void beginExclusive() throws SCIOException {
	LOG.warn("beginExclusive not supported");
    }

    @Override
    public void endExclusive() throws SCIOException {
	LOG.warn("endExclusive not supported");
    }

    @Override
    public void disconnect(boolean arg0) throws SCIOException {
	nfcCardChannel.close();
    }

    @Override
    public SCIOATR getATR() {
	// build ATR according to PCSCv2-3, Sec. 3.1.3.2.3.1
	byte[] histBytes = isodep.getHistoricalBytes();
	if (histBytes == null) {
	    histBytes = isodep.getHiLayerResponse();
	}
	if (histBytes == null) {
	    return new SCIOATR(new byte[0]);
	} else {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    // Initial Header
	    out.write(0x3B);
	    // T0
	    out.write(0x80 | (histBytes.length & 0xF));
	    // TD1
	    out.write(0x80);
	    // TD2
	    out.write(0x01);
	    // ISO14443A: The historical bytes from ATS response.
	    // ISO14443B: 1-4=Application Data from ATQB, 5-7=Protocol Info Byte from ATQB, 8=Higher nibble = MBLI from ATTRIB command Lower nibble (RFU) = 0
	    // TODO: check that the HiLayerResponse matches the requirements for ISO14443B
	    out.write(histBytes, 0, histBytes.length);

	    // TCK: Exclusive-OR of bytes T0 to Tk
	    byte[] preATR = out.toByteArray();
	    byte chkSum = 0;
	    for (int i = 1; i < preATR.length; i++) {
		chkSum ^= preATR[i];
	    }
	    out.write(chkSum);

	    byte[] atr = out.toByteArray();
	    return new SCIOATR(atr);
	}
    }

    @Override
    public SCIOChannel getBasicChannel() {
	return this.nfcCardChannel;
    }

    @Override
    public SCIOProtocol getProtocol() {
	// NFC is contactless
        return SCIOProtocol.TCL;
    }

    @Override
    public SCIOChannel openLogicalChannel() throws SCIOException {
	return nfcCardChannel;
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws SCIOException {
	if (controlCode == (0x42000000 + 3400)) {
	    // GET_FEATURE_REQUEST_CTLCODE
	    return new byte[0];
	} else {
	    String msg = "Control command not supported.";
	    throw new SCIOException(msg, SCIOErrorCode.SCARD_E_INVALID_PARAMETER);
	}
    }

    @Override
    public SCIOTerminal getTerminal() {
        return nfcCardTerminal;
    }

}
