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
import java.nio.ByteBuffer;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.util.Promise;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.dispatch.DispatchQueueAttr;
import org.robovm.apple.ext.corenfc.NFCISO7816APDU;
import org.robovm.apple.ext.corenfc.NFCISO7816Tag;
import org.robovm.apple.ext.corenfc.NFCPollingOption;
import org.robovm.apple.ext.corenfc.NFCTagReaderSession;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Neil Crossley
 * @author Florian Otto
 */
public final class IOSNFCCard extends AbstractNFCCard {

    private DISPATCH_MODE concurrencyMode = DISPATCH_MODE.CONCURRENT;
    private String dialogMsg = "Please provide card.";
    private boolean tagPresent;

    public enum DISPATCH_MODE {
	CONCURRENT,
	MAINQUEUE;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IOSNFCCard.class);

    private NFCTagReaderSession nfcSession;
    private NFCISO7816Tag tag;

    public IOSNFCCard(NFCCardTerminal terminal) throws IOException {
	super(terminal);

    }

    public void connect() throws SCIOException {
	switch (this.concurrencyMode) {
	    case CONCURRENT:
		this.nfcSession = new NFCTagReaderSession(NFCPollingOption._4443, new IOSNFCDelegate(this),
			DispatchQueue.create("nfcqueue", DispatchQueueAttr.Concurrent()));
		break;
	    case MAINQUEUE:
		this.nfcSession = new NFCTagReaderSession(NFCPollingOption._4443, new IOSNFCDelegate(this),
			DispatchQueue.getMainQueue());
		break;
	    default:
		throw new SCIOException("Bad configuration", SCIOErrorCode.SCARD_W_EOF);

	}
	this.nfcSession.setAlertMessage(this.getDialogMsg());
	this.nfcSession.beginSession();

    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
	this.nfcSession.invalidateSession();
	this.tagPresent = false;
    }

    @Override
    public boolean isCardPresent() {
	return this.tagPresent;
    }

    @Override
    public void terminate(boolean killNfcConnection) throws SCIOException {
	this.disconnect(false);
    }

    @Override
    public SCIOATR getATR() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public byte[] transceive(byte[] apdu) throws IOException {
	NFCISO7816APDU isoapdu = new NFCISO7816APDU(new NSData(apdu));
	Promise<byte[]> p = new Promise<>();
	tag.sendCommandAPDU$completionHandler$(isoapdu, (NSData resp, Byte sw1, Byte sw2, NSError er2) -> {
	    ByteBuffer bb = ByteBuffer.allocate((int) resp.getLength() + 2);
	    bb.put(resp.getBytes(), 0, (int) resp.getLength());
	    bb.put(sw1);
	    bb.put(sw2);
	    p.deliver(bb.array());
	});

	try {
	    return p.deref();
	} catch (InterruptedException ex) {
	    throw new IOException(ex);
	}
    }

    public void setConcurrencyMode(DISPATCH_MODE mode) {
	this.concurrencyMode = mode;
    }

    public DISPATCH_MODE getConcurrencyMode() {
	return concurrencyMode;
    }

    public String getDialogMsg() {
	return dialogMsg;
    }

    public void setDialogMsg(String dialogMsg) {
	this.dialogMsg = dialogMsg;
    }

    public void setTag(NFCISO7816Tag tag) {
	this.tag = tag;
	this.tagPresent = true;
    }

}
