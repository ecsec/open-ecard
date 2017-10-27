/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC specific implementation of the TerminalFactory
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
public class NFCFactory implements org.openecard.common.ifd.scio.TerminalFactory {

    private static final String ALGORITHM = "AndroidNFC";

    private static final Logger LOG = LoggerFactory.getLogger(NFCFactory.class);

    private static Context context;
    private static NfcAdapter adapter;
    private static NFCCardTerminals terminals;

    public NFCFactory() throws NoSuchTerminal {
	LOG.info("Create new NFCFactory");
	if (adapter == null || terminals == null) {
	    adapter = getNFCAdapter();
	    terminals = new NFCCardTerminals(adapter);
	    if(adapter == null) {
		String msg = "NFC not available";
		LOG.error(msg);
		throw new NoSuchTerminal(msg);
	    }
	}
    }

    @Override
    public String getType() {
	return ALGORITHM;
    }

    @Override
    public SCIOTerminals terminals() {
	return terminals;
    }

    public static void setContext(Context c){
	context = c;
    }

    /**
     * Return the names of the nfc terminals. Should return at least one card terminal (the integrated one).
     *
     * @return list of terminal names.
     */
    public static List<String> getTerminalNames() {
	List<String> terminalNames = new ArrayList<>();
	try {
	    List<SCIOTerminal> nfcTerminals = terminals.list();
	    for (SCIOTerminal nfcTerminal : nfcTerminals) {
		terminalNames.add(nfcTerminal.getName());
	    }
	} catch (SCIOException ex) {
	    LOG.warn(ex.getMessage(), ex);
	}
	return terminalNames;
    }

    public static void setNFCTag(Tag tag) {
	setNFCTag(tag, IsoDep.get(tag).getTimeout());
    }

    /**
     * Set the nfc tag in the nfc card terminal.
     *
     * @param tag
     * @param timeout current timeout for transceive(byte[]) in milliseconds.
     */
    public static void setNFCTag(Tag tag, int timeout) {
	IsoDep isoDepTag = IsoDep.get(tag);
	isoDepTag.setTimeout(timeout);
	try {
	    // standard nfc terminal
	    NFCCardTerminal.getInstance().setTag(isoDepTag, timeout);
	} catch (SCIOException ex) {
	    LOG.warn(ex.getMessage(), ex);
	}
    }

    /**
     * Signals if a nfc tag is removed.
     */
    public static void removeNFCTag() {
	NFCCardTerminal.getInstance().removeTag();
    }

    /**
     * Proof if NFC is available on the corresponding device.
     *
     * @return true if nfc is available, otherwise false
     */
    public static boolean isNFCAvailable() {
	return NFCFactory.getNFCAdapter() != null;
    }

    /**
     * Proof if NFC is enabled on the corresponding device. If this method return {@code false} nfc should be activated
     * in the device settings.
     *
     * @return true if nfc is enabled, otherwise false
     */
    public static boolean isNFCEnabled() {
	return NFCFactory.getNFCAdapter() != null ? NFCFactory.getNFCAdapter().isEnabled() : false;
    }

    /**
     * Return the adapter for NFC.
     *
     * @return nfc adapter.
     */
    public static NfcAdapter getNFCAdapter() {
	if (adapter == null) {
	    LOG.info("Try to create new NFCAdapter...");
	    NfcManager nfcManager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
	    adapter = nfcManager.getDefaultAdapter();
	}
	return adapter;
    }

}
