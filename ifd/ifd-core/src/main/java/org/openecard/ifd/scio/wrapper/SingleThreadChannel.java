/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.ifd.scio.wrapper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardCommandStatus;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.Protocol;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.util.ByteUtils;
import org.openecard.ifd.scio.TransmitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of a channel executing all commands in the same thread.
 * Executing commands in the same thread has the effect, that transactions are not broken when the IFD is called from
 * different threads which is the case almost every time.
 *
 * @author Tobias Wich
 */
public class SingleThreadChannel implements IfdChannel {

    private static final Logger LOG = LoggerFactory.getLogger(SingleThreadChannel.class);

    private static final AtomicInteger THREAD_NUM = new AtomicInteger(1);

    private final ExecutorService exec;
    private SCIOChannel channel;
    /**
     * Currently active secure messaging protocol.
     */
    private Protocol smProtocol = null;

    /**
     * Creates an instance and launches a command submission thread.
     *
     * @param term Terminal whose channel is to be bound to the thread
     * @param isBasic {@code true} if a basic channel shall be opened, {@code false} if a logical channel shall be opened.
     * @throws SCIOException Thrown in case the channel could not be established.
     */
    public SingleThreadChannel(SCIOTerminal term, boolean isBasic) throws SCIOException {
	this.exec = Executors.newSingleThreadExecutor(new ThreadFactory() {
	    @Override
	    public Thread newThread(Runnable r) {
		int num = SingleThreadChannel.this.channel.getChannelNumber();
		String termName = SingleThreadChannel.this.channel.getCard().getTerminal().getName();
		String name = String.format("Channel-%d %d '%s'", THREAD_NUM.getAndIncrement(), num, termName);
		Thread t = new Thread(r, name);
		t.setDaemon(true);
		return t;
	    }
	});

	SCIOCard card = connectCard(term);
	if (isBasic) {
	    this.channel = card.getBasicChannel();
	} else {
	    this.channel = card.openLogicalChannel();
	}
    }

    @Override
    public void shutdown() throws SCIOException {
	exec.shutdown();
	channel.close();
    }

    private static SCIOCard connectCard(SCIOTerminal term) throws SCIOException {
	SCIOCard card;
	try {
	    card = term.connect(SCIOProtocol.T1);
	} catch (SCIOException e1) {
	    try {
		card = term.connect(SCIOProtocol.TCL);
	    } catch (SCIOException e2) {
		try {
		    card = term.connect(SCIOProtocol.T0);
		} catch (SCIOException e3) {
		    try {
			card = term.connect(SCIOProtocol.ANY);
		    } catch (SCIOException ex) {
			throw new SCIOException("Reader refused to connect card with any protocol.", ex.getCode());
		    }
		}
	    }
	}
	return card;
    }

    @Override
    public void reconnect() throws SCIOException {
	if (channel.isBasicChannel()) {
	    SCIOCard card = channel.getCard();
	    SCIOTerminal term = card.getTerminal();
	    channel.close();
	    card.disconnect(true);
	    card = connectCard(term);

	    channel = card.getBasicChannel();
	    removeSecureMessaging();
	} else {
	    throw new RuntimeException("Reconnect called on logical channel.");
	}
    }

    @Nonnull
    @Override
    public SCIOChannel getChannel() {
	return channel;
    }

    /**
     * Transmits the given command APDU to the card.
     * <p>The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.</p>
     * <p>Note that this method cannot be used to transmit {@code MANAGE CHANNEL} APDUs. Logical channels should be
     * managed using the {@link SCIOCard#openLogicalChannel()} and {@link #close()} methods.</p>
     * <p>Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
     * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:</p>
     * <ul>
     * <li>if the response APDU has an SW1 of 61, the implementation should issue a {@code GET RESPONSE} command using
     * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
     * exchanges is concatenated to form the final response body.</li>
     * <li>if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.</li>
     * </ul>
     *
     * @param command Command APDU, which should be sent to the card.
     * @return The response APDU after the given command APDU is processed.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
     * @throws IllegalArgumentException Thrown if the APDU encodes a {@code MANAGE CHANNEL} command.
     * @throws NullPointerException Thrown in case the argument is {@code null}.
     */
    @Nonnull
    private CardResponseAPDU transmit(final @Nonnull byte[] command) throws SCIOException, IllegalStateException {
	// send command
	Future<CardResponseAPDU> result = exec.submit(new Callable<CardResponseAPDU>() {
	    @Override
	    public CardResponseAPDU call() throws Exception {
		return channel.transmit(command);
	    }
	});
	// return result or evaluate errors
	try {
	    return result.get();
	} catch (ExecutionException ex) {
	    // check out the real cause of the error
	    Throwable cause = ex.getCause();
	    if (cause instanceof SCIOException) {
		throw (SCIOException) cause;
	    } else if (cause instanceof IllegalStateException) {
		throw (IllegalStateException) cause;
	    } else if (cause instanceof IllegalArgumentException) {
		throw (IllegalArgumentException) cause;
	    } else if (cause instanceof NullPointerException) {
		throw (NullPointerException) cause;
	    } else {
		String msg = "Unknown error during APDU submission.";
		throw new SCIOException(msg, SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, cause);
	    }
	} catch (InterruptedException ex) {
	    result.cancel(true);
	    throw new IllegalStateException("Running command cancelled during execution.");
	}
    }

    /**
     * Transmits the given command APDU to the card.
     * <p>The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.</p>
     * <p>Note that this method cannot be used to transmit {@code MANAGE CHANNEL} APDUs. Logical channels should be
     * managed using the {@link SCIOCard#openLogicalChannel()} and {@link #close()} methods.</p>
     * <p>Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
     * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:</p>
     * <ul>
     * <li>if the response APDU has an SW1 of 61, the implementation should issue a {@code GET RESPONSE} command using
     * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
     * exchanges is concatenated to form the final response body.</li>
     * <li>if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.</li>
     * </ul>
     *
     * @param command Command APDU, which should be sent to the card.
     * @return The response APDU after the given command APDU is processed.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
     * @throws IllegalArgumentException Thrown if the APDU encodes a {@code MANAGE CHANNEL}..
     * @throws NullPointerException Thrown in case the argument is {@code null}.
     */
    @Nonnull
    private CardResponseAPDU transmit(final @Nonnull CardCommandAPDU command) throws SCIOException,
	    IllegalStateException {
	return transmit(command.toByteArray());
    }

    @Nonnull
    @Override
    public byte[] transmit(@Nonnull byte[] input, @Nonnull List<byte[]> responses) throws TransmitException,
	    SCIOException, IllegalStateException {
	byte[] inputAPDU = input;
	if (isSM()) {
	    LOG.debug("Apply secure messaging to APDU: {}", ByteUtils.toHexString(inputAPDU, true));
	    inputAPDU = smProtocol.applySM(inputAPDU);
	}
	LOG.debug("Send APDU: {}", ByteUtils.toHexString(inputAPDU, true));
	CardResponseAPDU rapdu = transmit(inputAPDU);
	byte[] result = rapdu.toByteArray();
	LOG.debug("Receive APDU: {}", ByteUtils.toHexString(result, true));
	if (isSM()) {
	    result = smProtocol.removeSM(result);
	    LOG.debug("Remove secure messaging from APDU: {}", ByteUtils.toHexString(result, true));
	}
	// get status word
	byte[] sw = new byte[2];
	sw[0] = result[result.length - 2];
	sw[1] = result[result.length - 1];

	// return without validation when no expected results given
	if (responses.isEmpty()) {
	    return result;
	}
	// verify result
	for (byte[] expected : responses) {
	    // one byte codes are used like mask values
	    // AcceptableStatusCode-elements containing only one byte match all status codes starting with this byte
	    if (ByteUtils.isPrefix(expected, sw)) {
		return result;
	    }
	}

	// not an expected result
	String msg = "The returned status code is not in the list of expected status codes. The returned code is:\n";
	TransmitException tex = new TransmitException(result, msg + CardCommandStatus.getMessage(sw));
	throw tex;
    }

    @Nonnull
    @Override
    public byte[] transmitControlCommand(final int controlCode, final @Nonnull byte[] command) throws SCIOException,
	    IllegalStateException, NullPointerException {
	// send command
	Future<byte[]> result = exec.submit(new Callable<byte[]>() {
	    @Override
	    public byte[] call() throws Exception {
		return channel.getCard().transmitControlCommand(controlCode, command);
	    }
	});
	// return result or evaluate errors
	try {
	    return result.get();
	} catch (ExecutionException ex) {
	    // check out the real cause of the error
	    Throwable cause = ex.getCause();
	    if (cause instanceof SCIOException) {
		throw (SCIOException) cause;
	    } else if (cause instanceof IllegalStateException) {
		throw (IllegalStateException) cause;
	    } else if (cause instanceof NullPointerException) {
		throw (NullPointerException) cause;
	    } else {
		String msg = "Unknown error during control command submission.";
		throw new SCIOException(msg, SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, cause);
	    }
	} catch (InterruptedException ex) {
	    result.cancel(true);
	    throw new IllegalStateException("Running command cancelled during execution.");
	}
    }

    @Override
    public void beginExclusive() throws SCIOException, IllegalStateException {
	submitTransaction(true);
    }

    @Override
    public void endExclusive() throws SCIOException, IllegalStateException {
	submitTransaction(false);
    }

    private void submitTransaction(final boolean start) throws SCIOException, IllegalStateException {
	// send command
	Future<Void> result = exec.submit(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		SCIOCard card = channel.getCard();
		if (start) {
		    card.beginExclusive();
		} else {
		    card.endExclusive();
		}
		return null;
	    }
	});
	// return result or evaluate errors
	try {
	    result.get();
	} catch (ExecutionException ex) {
	    // check out the real cause of the error
	    Throwable cause = ex.getCause();
	    if (cause instanceof SCIOException) {
		throw (SCIOException) cause;
	    } else if (cause instanceof IllegalStateException) {
		throw (IllegalStateException) cause;
	    } else {
		String msg = String.format("Unknown error during transaction submission (start=%b).", start);
		throw new SCIOException(msg, SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, cause);
	    }
	} catch (InterruptedException ex) {
	    result.cancel(true);
	    throw new IllegalStateException("Running command cancelled during execution.");
	}
    }

    @Override
    public boolean isSM() {
	boolean result = this.smProtocol != null;
	return result;
    }

    @Override
    public void addSecureMessaging(Protocol protocol) {
	this.smProtocol = protocol;
    }

    @Override
    public void removeSecureMessaging() {
	this.smProtocol = null;
    }

}
