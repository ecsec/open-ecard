/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.ifd.scio.wrapper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ifd.TerminalFactory;
import org.openecard.client.ifd.scio.IFDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCWrapper {

    private static final Logger _logger = LoggerFactory.getLogger(SCWrapper.class);

    private final CardTerminals terminals;
    private final SecureRandom secureRandom;

    private final ConcurrentSkipListMap<String,SCTerminal> scTerminals;

    public SCWrapper() throws IFDException {
	TerminalFactory f = IFDTerminalFactory.getInstance();
	terminals = f.terminals();
	secureRandom =  new SecureRandom();
	scTerminals = new ConcurrentSkipListMap<String, SCTerminal>();
    }

    public byte[] createHandle(int size) {
	byte[] handle = new byte[size];
	secureRandom.nextBytes(handle);
	return handle;
    }


    public synchronized SCChannel getChannel(byte[] handle) throws IFDException {
	for (SCTerminal t : getTerminals()) {
	    if (t.isConnected()) {
		SCCard c = t.getCard(); // this may produce a valid error
		try {
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
		    SCChannel ch = c.getChannel(handle);
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
		    SCChannel ch = c.getChannel(handle);
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
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.UNKNOWN_IFD, "IFD with name '" + ifdName + "' does not exist.");
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
	ArrayList<SCTerminal> list = new ArrayList<SCTerminal>(scTerminals.values());
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
	ArrayList<String> list = new ArrayList<String>(scTerminals.keySet());
	return list;
    }

    public synchronized void updateTerminals() {
	ConcurrentSkipListSet<String> deleted = new ConcurrentSkipListSet<String>(scTerminals.keySet());

	// get list and check all entries
	List<CardTerminal> ts;
	try {
	    ts = terminals.list();
	} catch (CardException ex) {
	    ts = new ArrayList<CardTerminal>(0); // empty list because list call can fail with exception on some systems
	}
	for (CardTerminal t : ts) {
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

    /**
     * Try to shut down as much as possible. This may break the whole structure, but it is ok for release context.
     */
    public synchronized void releaseAll() {
	updateTerminals();
	for (SCTerminal t : getTerminals()) {
	    try {
		t.disconnect();
	    } catch (CardException ex) {
	    }
	}
    }

}
