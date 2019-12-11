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
import org.robovm.apple.corenfc.NFCReaderError;
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
    private byte[] histBytes;

    public enum DISPATCH_MODE {
	CONCURRENT,
	MAINQUEUE;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IOSNFCCard.class);

    public final Object tagLock = new Object();
    private NFCSessionContext sessionContext;
    private NSError error;
    private volatile NFCISO7816Tag tag;

    private final IOSConfig cfg;

    public IOSNFCCard(NFCCardTerminal terminal, IOSConfig cfg) throws IOException {
	super(terminal);
	this.cfg = cfg;
    }

    private void setTag(NFCISO7816Tag tag, NFCSessionContext givenContext) {
	if (givenContext == sessionContext || (tag == null && givenContext == null)) {
	    this.tag = tag;
	    this.setHistBytes();
	}
    }

    private NFCSessionContext initSessionObj() throws SCIOException {
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
	NFCTagReaderSessionDelegateAdapterImpl delegate = new NFCTagReaderSessionDelegateAdapterImpl();
	NFCTagReaderSession session = new NFCTagReaderSession(NFCPollingOption.ISO14443, delegate, dspqueue);
	session.setAlertMessage(cfg.getDefaultProviderCardMSG());

	NFCSessionContext resultSessionContext = new NFCSessionContext(delegate, session);
	delegate.currentContext = resultSessionContext;
	return resultSessionContext;
    }

    @Override
    public void setDialogMsg(String msg) {
	NFCSessionContext context = this.sessionContext;
	if (context != null) {
	    context.session.setAlertMessage(msg);
	}
    }

    public void connect() throws SCIOException {

	connect(0);
    }

    private void connect(int attempts) throws SCIOException {
	if (attempts >= 3) {
	    throw new IllegalStateException(String.format("Could not create a new NFC session after %d attempts ", attempts));
	}

	NFCSessionContext context = this.initSessionObj();

	synchronized (this.tagLock) {
	    this.error = null;
	    this.sessionContext = context;
	    context.session.beginSession();
	    while (this.tag == null && this.error == null) {
		try {
		    this.tagLock.wait();
		} catch (InterruptedException ex) {
		    throw new SCIOException("", SCIOErrorCode.SCARD_E_TIMEOUT, ex);
		}
	    }
	    if (this.error != null) {
		if (this.error.getCode() != NFCReaderError.ReaderSessionInvalidationErrorSystemIsBusy.value()) {
		    LOG.error("Could not create a new NFC session. {}", this.error);
		    context.session.invalidateSession();
		    this.error = null;
		    this.sessionContext = null;
		    this.terminateTag();
		    throw new IllegalStateException("Could not create a new NFC session.");
		} else {
		    connect(attempts + 1);
		}

	    }
	}
    }

    @Override
    public boolean isTagPresent() {
	LOG.debug("isTag present was called");
	return this.tag != null;
    }

    @Override
    public void terminateTag() throws SCIOException {
	synchronized (this.tagLock) {
	    final NFCSessionContext currentSession = this.sessionContext;
	    if (currentSession != null) {
		currentSession.session.invalidateSession();
		this.sessionContext = null;
		setHistBytes();
	    }
	}
    }

    private void setHistBytes() {
	final NFCISO7816Tag currentTag = this.tag;
	final NSData hist = currentTag != null ? currentTag.getHistoricalBytes() : null;
	if (hist != null) {
	    this.histBytes = hist.getBytes();
	} else {
	    this.histBytes = null;
	}
    }

    @Override
    public SCIOATR getATR() {
	final byte[] currentHistBytes;
	synchronized (this.tagLock) {
	    currentHistBytes = this.histBytes;
	}
	// build ATR according to PCSCv2-3, Sec. 3.1.3.2.3.1
	if (currentHistBytes == null) {
	    LOG.debug("hist bytes are null");
	    return new SCIOATR(new byte[0]);
	} else {
	    LOG.debug("hist bytes will be processed ");
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    // Initial Header
	    out.write(0x3B);
	    // T0
	    out.write(0x80 | (currentHistBytes.length & 0xF));
	    // TD1
	    out.write(0x80);
	    // TD2
	    out.write(0x01);
	    // ISO14443A: The historical bytes from ATS response.
	    // ISO14443B: 1-4=Application Data from ATQB, 5-7=Protocol Info Byte from ATQB, 8=Higher nibble = MBLI from ATTRIB command Lower nibble (RFU) = 0
	    // TODO: check that the HiLayerResponse matches the requirements for ISO14443B
	    out.write(currentHistBytes, 0, currentHistBytes.length);

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
	final NFCISO7816Tag currentTag = tag;

	if (currentTag == null) {
	    throw new IllegalStateException("Cannot transceive because the tag is null.");
	}
	NFCISO7816APDU isoapdu = new NFCISO7816APDU(new NSData(apdu));
	Promise<byte[]> p = new Promise<>();
	currentTag.sendCommandAPDU(isoapdu, (NSData resp, Byte sw1, Byte sw2, NSError er2) -> {
	    if (er2 != null) {
		LOG.error("Following error occurred while transmitting the APDU: {}", er2);
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
		throw new IllegalStateException();
	    }
	    return response;
	} catch (InterruptedException ex) {
	    throw new IOException(ex);
	}
    }

    public void setConcurrencyMode(DISPATCH_MODE mode) {
	this.concurrencyMode = mode;
    }

    private class NFCTagReaderSessionDelegateAdapterImpl extends NFCTagReaderSessionDelegateAdapter {

	public NFCSessionContext currentContext = null;
	private final Object notifyErrorLock = new Object();
	private volatile boolean hasNotifiedError = false;

	@Override
	public void didInvalidate(NFCTagReaderSession session, NSError err) {
	    LOG.debug(".didInvalidate()");
	    notifyError(err);
	    return;
	}

	private void notifyError(NSError err) {

	    synchronized (notifyErrorLock) {
		if (hasNotifiedError) {
		    return;
		} else {
		    hasNotifiedError = true;
		}
	    }

	    synchronized (tagLock) {
		error = err;
		setTag(null, currentContext);
		tagLock.notifyAll();
	    }
	}

	@Override
	public void tagReaderSessionDidBecomeActive(NFCTagReaderSession session) {
	    LOG.debug(".didbecomeActive()");
	    return;
	}

	@Override
	public void didDetectTags(NFCTagReaderSession session, NSArray<?> tags) {
	    for (NSObject t : tags) {
		session.connectToTag(t, (NSError err) -> {

		    if (err != null) {
			notifyError(err);
		    } else {

			NFCISO7816Tag tag = session.getConnectedTag().asNFCISO7816Tag();
			synchronized (tagLock) {
			    setTag(tag, currentContext);
			    setDialogMsg(cfg.getDefaultCardRecognizedMSG());
			    tagLock.notifyAll();
			}
		    }
		});
	    }
	}
    }

}
