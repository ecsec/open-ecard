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

import java.util.concurrent.ConcurrentSkipListMap;
import javax.annotation.Nonnull;
import org.openecard.common.ECardConstants;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.util.ValueGenerators;
import org.openecard.ifd.scio.IFDException;


/**
 *
 * @author Tobias Wich
 */
public class ChannelManager {

    private final DeadAndAliveTerminals terminals;
    private final ConcurrentSkipListMap<byte[], HandledChannel> channels;

    public ChannelManager() throws IFDException {
	this.terminals = new DeadAndAliveTerminals();
	this.channels = new ConcurrentSkipListMap<>(new ByteArrayComparator());
    }

    public static byte[] createHandle(int size) {
	return ValueGenerators.generateRandom(size * 2);
    }

    public static byte[] createSlotHandle() {
	return createHandle(ECardConstants.SLOT_HANDLE_DEFAULT_SIZE);
    }

    public static byte[] createCtxHandle() {
	return createHandle(ECardConstants.CONTEXT_HANDLE_DEFAULT_SIZE);
    }

    public SCIOTerminals getTerminals() {
	return terminals;
    }

    /**
     *
     * @param ifdName
     * @return
     * @throws NoSuchTerminal
     * @throws SCIOException
     * @throws IllegalStateException
     * @throws NullPointerException
     * @throws SecurityException
     */
    @Nonnull
    public byte[] openChannel(@Nonnull String ifdName) throws NoSuchTerminal, SCIOException,
	    IllegalStateException {
	SCIOTerminal t = terminals.getTerminal(ifdName);
	SCIOCard card = t.connect(SCIOProtocol.ANY);
	SCIOChannel channel = card.getBasicChannel();
	byte[] slotHandle = createSlotHandle();
	HandledChannel ch = new HandledChannel(slotHandle, channel);
	channels.put(slotHandle, ch);
	return slotHandle.clone();
    }

    @Nonnull
    public HandledChannel getChannel(@Nonnull byte[] slotHandle) throws NoSuchChannel {
	HandledChannel ch = channels.get(slotHandle);
	if (ch == null) {
	    throw new NoSuchChannel("No channel available for the requested slot handle.");
	} else {
	    return ch;
	}
    }

    public void closeChannel(@Nonnull byte[] slotHandle) throws SCIOException {
	HandledChannel ch = channels.remove(slotHandle);
	if (ch != null) {
	    ch.shutdown();
	}
    }

}
