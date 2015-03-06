/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.IsoDep;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.SCIOTerminals.State;
import org.openecard.common.ifd.scio.TerminalState;
import org.openecard.common.ifd.scio.TerminalWatcher;

/**
 * NFC implementation of smartcardio's CardTerminals interface.
 *
 * @author Dirk Petrautzki 
 * @author Daniel Nemmert
 */
public class NFCCardTerminals implements SCIOTerminals {          
    
    @Override
    public List<SCIOTerminal> list(State arg0) throws SCIOException {
	List<SCIOTerminal> list = new ArrayList<SCIOTerminal>() {};
	list.add(NFCCardTerminal.getInstance());
	return list;
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
        return list(State.ALL);
    }

    @Override
    public SCIOTerminal getTerminal(@Nonnull String name) throws NoSuchTerminal {
        return new NFCCardTerminal();
    }

    @Override
    public TerminalWatcher getWatcher() throws SCIOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static class NFCCardWatcher implements TerminalWatcher {
        
        private final NFCCardTerminals terminal;
        private final NFCCardTerminal androidT;
        protected IsoDep isodep;
        private NFCCard nfcCard; 
        NfcManager manager;
        NfcAdapter adapter;
        Context context;
        
        public NFCCardWatcher(NFCCardTerminals terminal, NFCCardTerminal androidT) {
            this.terminal = terminal;
            this.androidT = androidT;
        }
        
        private Queue<StateChangeEvent> pendingEvents;
        private Collection<String> terminals;
        private Collection<String> cardPresent;
        
        private boolean isEnabled;           
        
        @Override
        public SCIOTerminals getTerminals() {
            return terminal;
        }
        
        @Override
        public List<TerminalState> start() throws SCIOException {
            ArrayList<TerminalState> result = new ArrayList<>();
            if (pendingEvents != null) {
                throw new IllegalStateException("Trying to initialize already initialized watcher instance");
            }
            pendingEvents = new LinkedList<>();
            terminals = new HashSet<>();
            cardPresent = new HashSet<>();
            
            // Check if NFC Adapter is present and enabled
            manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
            adapter = manager.getDefaultAdapter();
            isEnabled = adapter.isEnabled();

            String name = androidT.getName();
            terminals.add(name);
            
            if (adapter == null) {
		String msg = "No NFC Adapter on this Android Device";
                throw new SCIOException(msg, SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE);
            } else if (! isEnabled) {
                throw new SCIOException("NFC Adapter not enabled", SCIOErrorCode.SCARD_E_NO_SERVICE);
            } else if (adapter != null && adapter.isEnabled()) {
                    if (nfcCard.isodep.isConnected()) {
                    //List<SCIOTerminal> androidTerm = terminal.list(State.ALL);
                        cardPresent.add(name);
                        result.add(new TerminalState(name, true));
                    } else {
                        result.add(new TerminalState(name, false));
                    }
            }            
            return result;   
        }

        @Override
        public StateChangeEvent waitForChange() throws SCIOException {
            return waitForChange(0);
        }
        
        @Override
        public StateChangeEvent waitForChange(long timeout) throws SCIOException {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            if (pendingEvents == null) {
                throw new IllegalStateException("Calling wait on uninitialized watcher instance.");
            }
            
            StateChangeEvent nextEvent = pendingEvents.poll();
            
            return null;
        }   
    }
}
