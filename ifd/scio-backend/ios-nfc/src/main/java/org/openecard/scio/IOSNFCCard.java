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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.util.Promise;
import org.robovm.apple.corenfc.NFCISO7816APDU;
import org.robovm.apple.corenfc.NFCISO7816Tag;
import org.robovm.apple.corenfc.NFCPollingOption;
import org.robovm.apple.corenfc.NFCTagReaderSession;
import org.robovm.apple.corenfc.NFCTagReaderSessionDelegateAdapter;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.dispatch.DispatchQueueAttr;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSObject;
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
    private byte[] histBytes;
    private NFCTagReaderSessionDelegateAdapter del;

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

    private void setTag(NFCISO7816Tag tag) {
	this.tag = tag;
	this.setHistBytes();
    }

    private void initSessionObj() throws SCIOException {
	DispatchQueue dspqueue;
	switch (this.concurrencyMode) {
	    case CONCURRENT:
		dspqueue = DispatchQueue.create("nfcqueue", DispatchQueueAttr.Concurrent());
		break;
	    case MAINQUEUE:
		dspqueue = DispatchQueue.getMainQueue();
		break;
	    default:
		throw new SCIOException("Bad configuration", SCIOErrorCode.SCARD_W_EOF);
	}

	LOG.debug("Initializing new NFCTagReaderSession");
	this.del = new NFCTagReaderSessionDelegateAdapter() {
	    @Override
	    public void didInvalidate(NFCTagReaderSession session, NSError err) {
		LOG.debug(".didInvalidate()");
		return;
	    }

	    @Override
	    public void tagReaderSessionDidBecomeActive(NFCTagReaderSession session) {
		LOG.debug(".didbecomeActive()");
		return;
	    }

	    @Override
	    public void didDetectTags(NFCTagReaderSession session, NSArray<?> tags) {
		for (NSObject t : tags) {
		    session.connectToTag(t, (NSError er) -> {

			NFCISO7816Tag tag = session.getConnectedTag().asNFCISO7816Tag();
			setTag(tag);
		    });
		}
	    }
	};
	this.nfcSession = new NFCTagReaderSession(NFCPollingOption.ISO14443, del, dspqueue);

    }

    public void connect() throws SCIOException {
	this.initSessionObj();
	this.nfcSession.setAlertMessage(this.dialogMsg);
	this.nfcSession.beginSession();
	while (!isCardPresent()) {
	    try {
		Thread.sleep(100, 0);
	    } catch (InterruptedException ex) {
		throw new SCIOException("Error during session initialization", SCIOErrorCode.SCARD_F_INTERNAL_ERROR, ex);
	    }
	}
    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
	this.nfcSession.invalidateSession();
	this.nfcSession = null;
	this.setTag(null);
    }

    @Override
    public boolean isCardPresent() {
	return this.tag != null;
    }

    @Override
    public void terminate(boolean killNfcConnection) throws SCIOException {
	this.disconnect(false);
    }

    private void setHistBytes() {
	NSData hist = this.tag != null ? this.tag.getHistoricalBytes() : null;
	if (hist != null) {
	    this.histBytes = hist.getBytes();
	} else {
	    this.histBytes = null;
	}
    }

    @Override
    public SCIOATR getATR() {
	// build ATR according to PCSCv2-3, Sec. 3.1.3.2.3.1
	if (this.histBytes == null) {
	    LOG.debug("hist bytes are null");
	    return new SCIOATR(new byte[0]);
	} else {
	    LOG.debug("hist bytes will be processed ");
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    // Initial Header
	    out.write(0x3B);
	    // T0
	    out.write(0x80 | (this.histBytes.length & 0xF));
	    // TD1
	    out.write(0x80);
	    // TD2
	    out.write(0x01);
	    // ISO14443A: The historical bytes from ATS response.
	    // ISO14443B: 1-4=Application Data from ATQB, 5-7=Protocol Info Byte from ATQB, 8=Higher nibble = MBLI from ATTRIB command Lower nibble (RFU) = 0
	    // TODO: check that the HiLayerResponse matches the requirements for ISO14443B
	    out.write(this.histBytes, 0, this.histBytes.length);

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
    public byte[] transceive(byte[] apdu) throws IOException {
	NFCISO7816APDU isoapdu = new NFCISO7816APDU(new NSData(apdu));
	Promise<byte[]> p = new Promise<>();

	tag.sendCommandAPDU(isoapdu, (NSData resp, Byte sw1, Byte sw2, NSError er2) -> {
	    if (er2 != null) {
		p.deliver(null);
	    } else {
		ByteBuffer bb = ByteBuffer.allocate((int) resp.getLength() + 2);
		bb.put(resp.getBytes(), 0, (int) resp.getLength());
		bb.put(sw1);
		bb.put(sw2);
		p.deliver(bb.array());
	    }
	});

	try {
	    byte[] response = p.deref();
	    if (response == null) {
		throw new IOException();
	    }
	    return response;
	} catch (InterruptedException ex) {
	    throw new IOException(ex);
	}
    }

    public void setConcurrencyMode(DISPATCH_MODE mode) {
	this.concurrencyMode = mode;
    }

    public void setDialogMsg(String dialogMsg) {
	this.dialogMsg = dialogMsg;
    }

}
