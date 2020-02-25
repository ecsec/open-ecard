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

import java.io.IOException;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Dirk Petrautzki
 */
public abstract class AbstractNFCCard implements SCIOCard {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNFCCard.class);

    protected final NFCCardChannel nfcCardChannel;

    protected final NFCCardTerminal nfcCardTerminal;

    public AbstractNFCCard(NFCCardTerminal terminal) {
	nfcCardTerminal = terminal;

	this.nfcCardChannel = new NFCCardChannel(this);
    }

    public abstract boolean isTagPresent();

    public abstract boolean tagWasPresent();

    public abstract boolean terminateTag() throws SCIOException;

    @Override
    public void beginExclusive() throws SCIOException {
	LOG.warn("beginExclusive not supported");
    }

    @Override
    public void endExclusive() throws SCIOException {
	LOG.warn("endExclusive not supported");
    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
    }

    @Override
    public abstract SCIOATR getATR();

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
	throw new SCIOException("Logical channels are not supported.", SCIOErrorCode.SCARD_E_UNSUPPORTED_FEATURE);
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
    public NFCCardTerminal getTerminal() {
	return nfcCardTerminal;
    }

    public abstract byte[] transceive(byte[] apdu) throws IOException;

    void setDialogMsg(String msg) {
	throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }



}
