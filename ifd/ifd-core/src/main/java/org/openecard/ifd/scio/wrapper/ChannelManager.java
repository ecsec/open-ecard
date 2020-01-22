/****************************************************************************
 * Copyright (C) 2015-2019 ecsec GmbH.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.openecard.common.ECardConstants;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.Pair;
import org.openecard.common.util.ValueGenerators;
import org.openecard.ifd.scio.IFDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 * @author Benedikt Biallowons
 */
public class ChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelManager.class);

    private final TerminalFactory termFact;

    private final HashMap<String, SingleThreadChannel> baseChannels;
    private final TreeMap<byte[], SingleThreadChannel> handledChannels;
    private final HashMap<String, Set<byte[]>> ifdNameToHandles;

    public ChannelManager(TerminalFactory termFact) throws IFDException {
	this.termFact = termFact;
	this.baseChannels = new HashMap<>();
	this.handledChannels = new TreeMap<>(new ByteArrayComparator());
	this.ifdNameToHandles = new HashMap<>();
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
	return termFact.terminals();
    }

    public boolean prepareDevices() throws SCIOException {
	return getTerminals().prepareDevices();
    }

    public boolean powerDownDevices() {
	return getTerminals().powerDownDevices();
    }
    public synchronized SingleThreadChannel openMasterChannel(@Nonnull String ifdName) throws NoSuchTerminal,
	    SCIOException {
	if (baseChannels.containsKey(ifdName)) {
	    LOG.warn("Terminal '" + ifdName + "' is already connected.");
	    return baseChannels.get(ifdName);
	}
	SCIOTerminal t = getTerminals().getTerminal(ifdName);
	SingleThreadChannel ch = new SingleThreadChannel(t);
	baseChannels.put(ifdName, ch);
	ifdNameToHandles.put(ifdName, new TreeSet<>(new ByteArrayComparator()));
	return ch;
    }

    public synchronized Pair<byte[], SingleThreadChannel> openSlaveChannel(@Nonnull String ifdName)
	    throws NoSuchTerminal, SCIOException {
	SingleThreadChannel baseCh = getMasterChannel(ifdName);
	SingleThreadChannel slaveCh = new SingleThreadChannel(baseCh, true);
	byte[] slotHandle = createSlotHandle();
	handledChannels.put(slotHandle, slaveCh);
	ifdNameToHandles.get(ifdName).add(slotHandle);
	return new Pair<>(slotHandle, slaveCh);
    }

    public synchronized SingleThreadChannel getMasterChannel(@Nonnull String ifdName) throws NoSuchTerminal {
	SingleThreadChannel ch = baseChannels.get(ifdName);
	if (ch == null) {
	    throw new NoSuchTerminal("No terminal with name '" + ifdName + "' available.");
	} else {
	    return ch;
	}
    }

    public synchronized SingleThreadChannel getSlaveChannel(@Nonnull byte[] slotHandle) throws NoSuchChannel {
	SingleThreadChannel ch = handledChannels.get(slotHandle);
	if (ch == null) {
	    throw new NoSuchChannel("No channel for slot '" + ByteUtils.toHexString(slotHandle) + "' available.");
	} else {
	    return ch;
	}
    }

    public synchronized void closeMasterChannel(String ifdName) {
	LOG.debug("Closing MasterChannel");
	Set<byte[]> slotHandles = ifdNameToHandles.get(ifdName);
	if (slotHandles != null) {
	    // iterate over copy of the list as the closeSlaveHandle call modifies the original slotHandles list
	    for (byte[] slotHandle : new HashSet<>(slotHandles)) {
		try {
		    closeSlaveChannel(slotHandle);
		} catch (NoSuchChannel | SCIOException ex) {
		    LOG.warn("Failed to close channel for terminal '" + ifdName + "'.", ex);
		}
	    }
	    ifdNameToHandles.remove(ifdName);
	}

	SingleThreadChannel ch = baseChannels.remove(ifdName);
	if (ch == null) {
	    LOG.warn("No master channel for terminal '" + ifdName + "' available.");
	} else {
	    try {
		ch.shutdown();
	    } catch (SCIOException ex) {
		LOG.warn("Failed to shut down master channel for terminal '" + ifdName + "'.");
	    }
	}
    }

    public synchronized void closeSlaveChannel(@Nonnull byte[] slotHandle) throws NoSuchChannel, SCIOException {
	LOG.debug("Closing SlaveChannel");
	SingleThreadChannel ch = handledChannels.remove(slotHandle);
	if (ch == null) {
	    throw new NoSuchChannel("No channel for slot '" + ByteUtils.toHexString(slotHandle) + "' available.");
	} else {
	    String ifdName = ch.getChannel().getCard().getTerminal().getName();
	    ifdNameToHandles.get(ifdName).remove(slotHandle);
	    ch.shutdown();
	}
    }

}
