/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.openecard.common.ECardConstants;
import org.openecard.common.util.ValueGenerators;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.ifd.scio.IFDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCWrapper {

    private static final Logger _logger = LoggerFactory.getLogger(SCWrapper.class);

    private final SCIOTerminals terminals;

    private final ConcurrentSkipListMap<String, SCTerminal> scTerminals;

    public SCWrapper() throws IFDException {
	terminals = new DeadAndAliveTerminals();
	scTerminals = new ConcurrentSkipListMap<>();
    }

    public byte[] createHandle(int size) {
	return ValueGenerators.generateRandom(size * 2);
    }


    public synchronized SCChannel getChannel(byte[] handle) throws IFDException {
	for (SCTerminal t : getTerminals()) {
	    if (t.isConnected()) {
		SCCard c = t.getCard(); // this may produce a valid error
		try {
		    // try to match handle, error is raised and element skipped if not matching
		    SCChannel ch = c.getChannel(handle);
		    return ch;
		} catch (IFDException ex) {
		    // ignore, as this exception belongs to the normal find process
		}
	    }
	}
	IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Slot handle does not exist.");
	_logger.warn(ex.getMessage(), ex);
	throw ex;
    }

    public synchronized SCCard getCard(byte[] handle) throws IFDException {
	for (SCTerminal t : getTerminals()) {
	    if (t.isConnected()) {
		SCCard c = t.getCard();
		try {
		    // try to match handle, error is raised and element skipped if not matching
		    c.getChannel(handle);
		    return c;
		} catch (IFDException ex) {
		    // ignore, as this exception belongs to the normal find process
		}
	    }
	}
	IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Slot handle does not exist.");
	_logger.warn(ex.getMessage(), ex);
	throw ex;
    }

    public SCTerminal getTerminal(byte[] handle) throws IFDException {
	for (SCTerminal t : getTerminals()) {
	    if (t.isConnected()) {
		SCCard c = t.getCard();
		try {
		    // try to match handle, error is raised and element skipped if not matching
		    c.getChannel(handle);
		    return t;
		} catch (IFDException ex) {
		    // ignore, as this exception belongs to the normal find process
		}
	    }
	}
	IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Slot handle does not exist.");
	_logger.warn(ex.getMessage(), ex);
	throw ex;
    }

    public synchronized SCTerminal getTerminal(String ifdName) throws IFDException {
	SCTerminal t = getTerminal(ifdName, false);
	return t;
    }

    public synchronized SCTerminal getTerminal(String ifdName, boolean update) throws IFDException {
	if (update) {
	    updateTerminals();
	}
	SCTerminal t = scTerminals.get(ifdName);
	if (t == null) {
	    String msg = "IFD with name '" + ifdName + "' does not exist.";
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg);
	    _logger.warn(ex.getMessage(), ex);
	    throw ex;
	}
	return t;
    }

    public synchronized List<SCTerminal> getTerminals() {
	try {
	    List<SCTerminal> list = getTerminals(false);
	    return list;
	} catch (IFDException ex) { // there is no exception if no update happens
	    _logger.error(ex.getMessage(), ex);
	    return null;
	}
    }

    public synchronized List<SCTerminal> getTerminals(boolean update) throws IFDException {
	if (update) {
	    updateTerminals();
	}
	ArrayList<SCTerminal> list = new ArrayList<>(scTerminals.values());
	return list;
    }

    public synchronized List<String> getTerminalNames() {
	try {
	    List<String> list = getTerminalNames(false);
	    return list;
	} catch (IFDException ex) { // there is no exception if no update happens
	    _logger.error(ex.getMessage(), ex);
	    return null;
	}
    }

    public synchronized List<String> getTerminalNames(boolean update) throws IFDException {
	if (update) {
	    updateTerminals();
	}
	ArrayList<String> list = new ArrayList<>(scTerminals.keySet());
	return list;
    }

    public synchronized void updateTerminals() {
	ConcurrentSkipListSet<String> deleted = new ConcurrentSkipListSet<>(scTerminals.keySet());

	// get list and check all entries
	List<SCIOTerminal> ts;
	try {
	    ts = terminals.list();
	} catch (SCIOException ex) {
	    ts = new ArrayList<>(0); // empty list because list call can fail with exception on some systems
	}
	for (SCIOTerminal t : ts) {
	    if (scTerminals.containsKey(t.getName())) {
		// remove from deleted list
		deleted.remove(t.getName());
		// update terminal status
		scTerminals.get(t.getName()).updateTerminal();
	    } else {
		// add new terminal to list
		SCTerminal newTerm = new SCTerminal(t, this);
		scTerminals.put(t.getName(), newTerm);
	    }
	}

	// remove deleted terminals from map
	for (String s : deleted) {
	    scTerminals.remove(s);
	}
    }

    public synchronized boolean waitForChange(long timeout) throws IFDException {
	try {
	    return terminals.waitForChange(timeout);
	} catch (SCIOException ex) {
	    throw new IFDException(ex);
	}
    }

    public synchronized boolean waitForChange() throws IFDException {
	return waitForChange(0);
    }

    /**
     * Try to shut down as much as possible. This may break the whole structure, but it is ok for release context.
     */
    public synchronized void releaseAll() {
	updateTerminals();
	for (SCTerminal t : getTerminals()) {
	    try {
		t.disconnect();
	    } catch (SCIOException ignore) {
	    }
	}
    }

}
