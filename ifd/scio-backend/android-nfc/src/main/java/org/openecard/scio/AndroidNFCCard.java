/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.scio;

import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Dirk Petrautzki
 */
public final class AndroidNFCCard extends AbstractNFCCard {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNFCCard.class);

    private final Object connectLock = new Object();

    private volatile int transceiveTimeout;
    private volatile byte[] histBytes;
    private volatile IsoDep isodep;
    private volatile NFCCardMonitoring cardMonitor;
    private volatile boolean tagPending;

    private volatile Thread monitor;

    public AndroidNFCCard(NFCCardTerminal terminal) {
	super(terminal);

	tagPending = true;
    }

    public void setTag(IsoDep tag, int timeout) throws IOException {
	LOG.debug("Assigning tag {} with timeout {}", tag, timeout);
	synchronized(connectLock) {
	    isodep = tag;
	    transceiveTimeout = timeout;

	    byte[] histBytesTmp = isodep.getHistoricalBytes();
	    if (histBytesTmp == null) {
		histBytesTmp = isodep.getHiLayerResponse();
	    }
	    this.histBytes = histBytesTmp;

	    connectTag();

	    tagPending = false;
	    connectLock.notifyAll();

	    startCardMonitor();
	}
    }

    private void startCardMonitor() {
	NFCCardMonitoring createdMonitor = new NFCCardMonitoring(nfcCardTerminal, this);
	Thread executionThread = new Thread(createdMonitor);
	executionThread.start();
	this.monitor = executionThread;
	this.cardMonitor = createdMonitor;
    }


    private void connectTag() throws IOException {
	isodep.connect();
	isodep.setTimeout(getTransceiveTimeout());
    }

    private int getTransceiveTimeout() {
	return transceiveTimeout;
    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
	if (reset) {
	    synchronized(connectLock) {
		boolean wasConnected = this.isTagPresent();
		innerTerminateTag();

		if (wasConnected) {
		    try {
			connectTag();

			startCardMonitor();
		    } catch (IOException ex) {
			LOG.error("Failed to connect NFC tag.", ex);
			throw new SCIOException("Failed to reset channel.", SCIOErrorCode.SCARD_E_UNEXPECTED, ex);
		    }
		}
	    }
	}
    }

    @Override
    public boolean isTagPresent() {
        try{
            final boolean isTagPresent = !tagPending && isodep != null && isodep.isConnected();
            return isTagPresent;
        } catch (SecurityException e){
            //this seems to be thrown on some specific implementations allthough not specified
            return false;
        }
    }

    @Override
    public boolean tagWasPresent() {
	final boolean tagWasPresent = !tagPending && (isodep == null || !isodep.isConnected());
	return tagWasPresent;
    }

    @Override
    public boolean terminateTag() throws SCIOException {
	synchronized(connectLock) {
	    boolean changed = this.innerTerminateTag();
	    this.isodep = null;
	    this.histBytes = null;
	    this.tagPending = false;
	    connectLock.notifyAll();
	    return changed;
	}
    }

    public boolean innerTerminateTag() throws SCIOException {
	try {
	    return this.terminateTag(this.monitor, this.cardMonitor);
	} catch (IOException ex) {
	    LOG.error("Failed to close NFC tag.");
	    throw new SCIOException("Failed to close NFC channel.", SCIOErrorCode.SCARD_E_UNEXPECTED, ex);
	} finally {
	    this.monitor = null;
	    this.cardMonitor = null;
	}
    }

    private boolean terminateTag(Thread monitor, NFCCardMonitoring cardMonitor) throws IOException {
	synchronized(connectLock) {
	    if (cardMonitor != null) {
		LOG.debug("Killing the monitor");
		cardMonitor.notifyStopMonitoring();
	    }

	    if (this.isodep != null) {
		LOG.debug("Closing the tag");
		boolean wasClosed = this.isodep.isConnected();
		this.isodep.close();
		return wasClosed;
	    } else {
		return false;
	    }
	}
    }

    @Override
    public SCIOATR getATR() {
	byte[] currentHistBytes;
	synchronized (connectLock) {
	    boolean interrupted = false;
	    while (tagPending && !interrupted) {
		try {
		    connectLock.wait();
		} catch (InterruptedException ex) {
		    interrupted = true;
		}
	    }
	    if (interrupted) {
		currentHistBytes = null;
	    } else {
		currentHistBytes = this.histBytes;
	    }
	}

	// build ATR according to PCSCv2-3, Sec. 3.1.3.2.3.1
	if (currentHistBytes == null) {
	    return new SCIOATR(new byte[0]);
	} else {
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
	this.cardMonitor.notifyStartTranceiving();
	try {
	    IsoDep currentTag;
	    synchronized (connectLock) {
		while (tagPending) {
		    try {
			connectLock.wait();
		    } catch (InterruptedException ex) {
			throw new IOException(ex);
		    }
		}
		currentTag = isodep;
	    }
	    if (currentTag == null) {
		throw new IllegalStateException("Transmit of apdu command failed, because the tag is not present.");
	    }
	    try {
		return currentTag.transceive(apdu);
	    } catch (TagLostException ex) {
		LOG.debug("NFC Tag is not present.", ex);
		this.nfcCardTerminal.removeTag();
		throw new IllegalStateException("Transmit of apdu command failed, because the tag was lost.");
	    }
	}
	finally {
	    this.cardMonitor.notifyStopTranceiving();
	}
    }

}
