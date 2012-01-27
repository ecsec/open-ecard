/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ifd.scio.wrapper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ifd.TerminalFactory;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.ifd.scio.IFDException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SCWrapper {

    private static final Logger _logger = LogManager.getLogger(SCWrapper.class.getName());

    private final CardTerminals terminals;
    private final SecureRandom secureRandom;

    private final ConcurrentSkipListMap<String,SCTerminal> scTerminals;

    public SCWrapper() throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "SCWrapper()");
	} // </editor-fold>
	TerminalFactory f = IFDTerminalFactory.getInstance();
	terminals = f.terminals();
	secureRandom =  new SecureRandom();
	scTerminals = new ConcurrentSkipListMap<String, SCTerminal>();
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "SCWrapper()");
	} // </editor-fold>
    }

    public byte[] createHandle(int size) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "createHandle(int size)", size);
	} // </editor-fold>
	byte[] handle = new byte[size];
	secureRandom.nextBytes(handle);
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "createHandle(int size)", handle);
	} // </editor-fold>
	return handle;
    }


    public synchronized SCChannel getChannel(byte[] handle) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getChannel(byte[] handle)", handle);
	} // </editor-fold>
	for (SCTerminal t : getTerminals()) {
	    if (t.isConnected()) {
		SCCard c = t.getCard(); // this may produce a valid error
		try {
		    SCChannel ch = c.getChannel(handle);
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "getChannel(byte[] handle)", ch);
		    } // </editor-fold>
		    return ch;
		} catch (IFDException ex) {
		    // ignore, as this exception belongs to the normal find process
		}
	    }
	}
	IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Slot handle does not exist.");
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.WARNING)) {
	    _logger.logp(Level.WARNING, this.getClass().getName(), "getChannel(byte[] handle)", ex.getMessage(), ex);
	} // </editor-fold>
	throw ex;
    }

    public synchronized SCCard getCard(byte[] handle) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getCard(byte[] handle)", handle);
	} // </editor-fold>
	for (SCTerminal t : getTerminals()) {
	    if (t.isConnected()) {
		SCCard c = t.getCard();
		try {
		    SCChannel ch = c.getChannel(handle);
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "getCard(byte[] handle)", c);
		    } // </editor-fold>
		    return c;
		} catch (IFDException ex) {
		    // ignore, as this exception belongs to the normal find process
		}
	    }
	}
	IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Slot handle does not exist.");
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.WARNING)) {
	    _logger.logp(Level.WARNING, this.getClass().getName(), "getCard(byte[] handle)", ex.getMessage(), ex);
	} // </editor-fold>
	throw ex;
    }

    public SCTerminal getTerminal(byte[] handle) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getTerminal(byte[] handle)", handle);
	} // </editor-fold>
	for (SCTerminal t : getTerminals()) {
	    if (t.isConnected()) {
		SCCard c = t.getCard();
		try {
		    SCChannel ch = c.getChannel(handle);
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "getTerminal(byte[] handle)", t);
		    } // </editor-fold>
		    return t;
		} catch (IFDException ex) {
		    // ignore, as this exception belongs to the normal find process
		}
	    }
	}
	IFDException ex = new IFDException(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Slot handle does not exist.");
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.WARNING)) {
	    _logger.logp(Level.WARNING, this.getClass().getName(), "getTerminal(byte[] handle)", ex.getMessage(), ex);
	} // </editor-fold>
	throw ex;
    }

    public synchronized SCTerminal getTerminal(String ifdName) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getTerminal(String ifdName)", ifdName);
	} // </editor-fold>
	SCTerminal t = getTerminal(ifdName, false);
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "getTerminal(String ifdName)", t);
	} // </editor-fold>
	return t;
    }

    public synchronized SCTerminal getTerminal(String ifdName, boolean update) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "SCTerminal getTerminal(String ifdName, boolean update)", new Object[]{ifdName, update});
	} // </editor-fold>
	if (update) {
	    updateTerminals();
	}
	SCTerminal t = scTerminals.get(ifdName);
	if (t == null) {
	    IFDException ex = new IFDException(ECardConstants.Minor.IFD.UNKNOWN_IFD, "IFD with name '" + ifdName + "' does not exist.");
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "SCTerminal getTerminal(String ifdName, boolean update)", ex.getMessage(), ex);
	    } // </editor-fold>
	    throw ex;
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "SCTerminal getTerminal(String ifdName, boolean update)", t);
	} // </editor-fold>
	return t;
    }

    public synchronized List<SCTerminal> getTerminals() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getTerminals()");
	} // </editor-fold>
	try {
	    List<SCTerminal> list = getTerminals(false);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "getTerminals()", list);
	    } // </editor-fold>
	    return list;
	} catch (IFDException ex) { // there is no exception if no update happens
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "getTerminals()", null);
	    } // </editor-fold>
	    return null;
	}
    }

    public synchronized List<SCTerminal> getTerminals(boolean update) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getTerminals(boolean update)", update);
	} // </editor-fold>
	if (update) {
	    updateTerminals();
	}
	ArrayList<SCTerminal> list = new ArrayList<SCTerminal>(scTerminals.values());
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "getTerminals(boolean update)", list);
	} // </editor-fold>
	return list;
    }

    public synchronized List<String> getTerminalNames() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getTerminalNames()");
	} // </editor-fold>
	try {
	    List<String> list = getTerminalNames(false);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "getTerminalNames()", list);
	    } // </editor-fold>
	    return list;
	} catch (IFDException ex) { // there is no exception if no update happens
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "getTerminalNames()", null);
	    } // </editor-fold>
	    return null;
	}
    }

    public synchronized List<String> getTerminalNames(boolean update) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getTerminalNames(boolean update)", update);
	} // </editor-fold>
	if (update) {
	    updateTerminals();
	}
	ArrayList<String> list = new ArrayList<String>(scTerminals.keySet());
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "getTerminalNames(boolean update)", list);
	} // </editor-fold>
	return list;
    }

    public synchronized void updateTerminals() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "updateTerminals()");
	} // </editor-fold>
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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "releaseAll()");
	} // </editor-fold>
	updateTerminals();
	for (SCTerminal t : getTerminals()) {
	    try {
		t.disconnect();
	    } catch (CardException ex) {
	    }
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "releaseAll()");
	} // </editor-fold>
    }

}
