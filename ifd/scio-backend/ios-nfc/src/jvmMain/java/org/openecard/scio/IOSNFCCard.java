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

import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.util.Promise;
import org.robovm.apple.corenfc.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.dispatch.DispatchQueueAttr;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSErrorUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Neil Crossley
 * @author Florian Otto
 */
public final class IOSNFCCard extends AbstractNFCCard {

	private final int EXPECTED_IOS_NFC_TIMEOUT_MILLISECONDS = 20 * 1000;
	private final int EXPECTED_REQUIRED_NFC_SESSION_MILLISECONDS = 5 * 1000;

	private DISPATCH_MODE concurrencyMode = DISPATCH_MODE.CONCURRENT;
	private volatile byte[] histBytes;

	public enum DISPATCH_MODE {
		CONCURRENT,
		MAINQUEUE;
	}

	private static final Logger LOG = LoggerFactory.getLogger(IOSNFCCard.class);

	public final Object tagLock = new Object();
	private volatile NFCSessionContext sessionContext;
	private volatile NSError error;
	private volatile NFCISO7816Tag tag;

	private final IOSConfig cfg;
	private volatile boolean tagWasPresent = false;

	public IOSNFCCard(NFCCardTerminal terminal, IOSConfig cfg) {
		super(terminal);
		this.cfg = cfg;
	}

	private void setTag(NFCISO7816Tag tag, NFCSessionContext givenContext, NSError err, boolean notifyRemoveTag) {
		boolean givenEmptyTag = tag == null;
		if (givenContext == sessionContext || (givenEmptyTag && givenContext == null)) {
			this.tagWasPresent = (this.tag != null && givenEmptyTag) || this.tagWasPresent;
			this.error = err;
			this.tag = tag;
			this.setHistBytes(tag);
			LOG.debug("New tag state: [tagWasPresent={}, error={}, tag={}]", tagWasPresent, this.error, this.tag);
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
		NFCPollingOption pollingOption = NFCPollingOption.ISO14443;
		pollingOption.set(NFCPollingOption.PACE);
		NFCTagReaderSession session = new NFCTagReaderSession(pollingOption, delegate, dspqueue);
		session.setAlertMessage(cfg.getDefaultProvideCardMessage());

		NFCSessionContext resultSessionContext = new NFCSessionContext(delegate, session);
		delegate.currentContext = resultSessionContext;
		return resultSessionContext;
	}

	@Override
	public void setDialogMsg(String msg) {
		NFCSessionContext context = this.sessionContext;
		if (context != null && msg != null) {
			context.session.setAlertMessage(msg);
		}
	}

	public void connect() throws SCIOException {

		connect(0);
	}

	private void connect(int attempts) throws SCIOException {
		if (attempts >= 3) {
			throw new SCIOException(
				String.format("Could not create a new NFC session after %d attempts ", attempts),
				SCIOErrorCode.SCARD_E_NOT_READY);
		}

		NFCSessionContext context = this.initSessionObj();

		synchronized (this.tagLock) {
			this.tagWasPresent = false;
			this.error = null;
			this.sessionContext = context;
			context.session.beginSession();

			Thread timeoutDetection = beginTimeoutDetection(context);

			while (this.tag == null && this.error == null) {
				try {
					this.tagLock.wait();
				} catch (InterruptedException ex) {
					throw new SCIOException("", SCIOErrorCode.SCARD_E_TIMEOUT, ex);
				}
			}
			timeoutDetection.interrupt();

			NSError currentError = this.error;
			if (currentError != null) {
				this.tag = null;
				this.sessionContext = null;
				this.histBytes = null;
				this.error = null;
				final long errorCode = currentError.getCode();

				if (errorCode == NFCReaderError.ReaderSessionInvalidationErrorSystemIsBusy.value()) {
					context.session.invalidateSession();

					connect(attempts + 1);
				} else {
					LOG.debug("Could not create a new NFC session. {}", currentError);
					String message = getErrorMessage(errorCode);
					context.session.invalidateSession(message);

					SCIOErrorCode code = getError(errorCode);

					throw new SCIOException("Could not create a new NFC session.", code);
				}
			}
		}
	}

	private Thread beginTimeoutDetection(NFCSessionContext context) {
		Thread timeoutDetection = new Thread(() -> {
			try {
				Thread.sleep(EXPECTED_IOS_NFC_TIMEOUT_MILLISECONDS - EXPECTED_REQUIRED_NFC_SESSION_MILLISECONDS);

				LOG.debug("Triggering manual NFC timeout.");
				String currentDomain = "IOS NFC Open eCard";
				synchronized (this.tagLock) {
					if (this.sessionContext == context) {
						NSError err = new NSError(
							currentDomain,
							NFCReaderError.ReaderSessionInvalidationErrorSessionTimeout.value(),
							new NSErrorUserInfo());
						this.error = err;
						this.tagLock.notifyAll();
					}
				}
			} catch (InterruptedException ex) {
				LOG.debug("NFC timeout detection was interrupted.");
			}
		});
		timeoutDetection.start();
		return timeoutDetection;
	}

	private String getErrorMessage(final long errorCode) {
		if (errorCode == NFCReaderError.ReaderSessionInvalidationErrorSessionTimeout.value()) {
			return this.cfg.getAquireNFCTagTimeoutErrorMessage();
		} else if (errorCode == NFCReaderError.ReaderTransceiveErrorTagConnectionLost.value()) {
			return this.cfg.getTagLostErrorMessage();
		} else {
			return this.cfg.getDefaultNFCErrorMessage();
		}
	}

	private SCIOErrorCode getError(final long errorCode) {
		if (errorCode == NFCReaderError.ReaderSessionInvalidationErrorSessionTimeout.value()) {
			return SCIOErrorCode.SCARD_E_TIMEOUT;
		} else if (errorCode == NFCReaderError.ReaderSessionInvalidationErrorUserCanceled.value()) {
			return SCIOErrorCode.SCARD_W_CANCELLED_BY_USER;
		} else {
			return SCIOErrorCode.SCARD_E_NOT_READY;
		}
	}

	@Override
	public boolean isTagPresent() {
		return this.tag != null;
	}

	@Override
	public boolean tagWasPresent() {

		return this.tagWasPresent;
	}

	@Override
	public boolean terminateTag() throws SCIOException {
		return innerTerminateMethod();
	}

	private boolean innerTerminateMethod() {
		LOG.debug("Beginning to terminate the NFC tag.");
		synchronized (this.tagLock) {
			final NFCSessionContext currentSession = this.sessionContext;
			if (currentSession != null) {
				LOG.debug("Terminating the NFC tag.");
				if (this.error != null) {
					String message = getErrorMessage(error.getCode());
					currentSession.session.invalidateSession(message);
				} else {
					currentSession.session.setAlertMessage(cfg.getNFCCompletionMessage());
					currentSession.session.invalidateSession();
				}
				this.sessionContext = null;
				setTag(null, null, null, false);
				return true;
			} else {
				LOG.debug("Nothing to terminate.");
				return false;
			}
		}
	}

	private void setHistBytes(NFCISO7816Tag currentTag) {
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
				LOG.debug("Following error occurred while transmitting the APDU: {}", er2);
				synchronized (this.tagLock) {
					this.error = er2;
				}
				p.deliver(null);
				this.getTerminal().removeTag();
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

	//    @org.robovm.rt.bro.annotation.Marshaler(NSArray.AsListMarshaler.class)
//	List<NFCTag> tags
	private class NFCTagReaderSessionDelegateAdapterImpl extends NFCTagReaderSessionDelegateAdapter {

		public NFCSessionContext currentContext = null;
		private final Object notifyErrorLock = new Object();
		private volatile boolean hasNotifiedError = false;

		@Override
		public void didInvalidate(NFCTagReaderSession session, NSError err) {
			LOG.debug(".didInvalidate()");
			notifyError(err);
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
				setTag(null, currentContext, err, true);
				tagLock.notifyAll();
			}
		}

		@Override
		public void didBecomeActive(NFCTagReaderSession session) {
			LOG.debug(".didbecomeActive()");
		}

		@Override
		public void didDetectTags(NFCTagReaderSession session, @org.robovm.rt.bro.annotation.Marshaler(NSArray.AsListMarshaler.class) List<NFCTag> tags) {

			for (NFCTag t : tags) {
				setDialogMsg(cfg.getDefaultCardInsertedMessage());
				session.connectToTag(t, (NSError err) -> {
					if (err != null) {
						notifyError(err);
					} else {
						NFCTag tag = session.getConnectedTag();
						if (tag == null) {
							LOG.warn("NFCTag was null allthough detected. Might occur if a card is delivered to app which is not supported - iOS shows entitlement errors here, regardless of whitelisted AIDs");
							notifyError(null);
							return;
						}
						NFCISO7816Tag nfciso7816Tag = tag.asNFCISO7816Tag();
						synchronized (tagLock) {
							setTag(nfciso7816Tag, currentContext, null, false);
							setDialogMsg(cfg.getDefaultCardConnectedMessage());
							tagLock.notifyAll();
						}
					}
				});
			}
		}
	}
}
