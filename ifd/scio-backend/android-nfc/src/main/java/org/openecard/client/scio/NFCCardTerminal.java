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

package org.openecard.client.scio;

import android.nfc.tech.IsoDep;
import java.io.IOException;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;


/**
 * NFC implementation of smartcardio's CardTerminal interface.<br/>
 * Implemented as singleton because we only have one nfc-interface. Only
 * activitys can react on a new intent, so they must set the tag via setTag()
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class NFCCardTerminal extends CardTerminal {

    private NFCCard nfcCard = null;
    private static NFCCardTerminal instance = null;

    private NFCCardTerminal() {
    }

    public void setTag(IsoDep tag) {
	this.nfcCard = new NFCCard(tag);
    }

    public static synchronized NFCCardTerminal getInstance() {
	if (instance == null) {
	    instance = new NFCCardTerminal();
	}
	return instance;
    }

    @Override
    public Card connect(String arg0) throws CardException {
	if (this.nfcCard == null || this.nfcCard.isodep == null) {
	    System.out.println("no tag present");
	    throw new CardNotPresentException("No tag present");
	}
	try {
	    if (!this.nfcCard.isodep.isConnected()) {
		this.nfcCard.isodep.setTimeout(3000);
		this.nfcCard.isodep.connect();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    this.nfcCard = null;
	    throw new CardException("No connection could be established", e);
	}
	return this.nfcCard;
    }

    @Override
    public String getName() {
	return "Integrated NFC";
    }

    @Override
    public boolean isCardPresent() throws CardException {
	// TODO delete the following sleep
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	boolean ret = (this.nfcCard != null && this.nfcCard.isodep != null && this.nfcCard.isodep.isConnected());
	if (!ret && this.nfcCard != null) {
	    this.nfcCard.isodep = null;
	}
	return ret;
    }

    @Override
    public boolean waitForCardAbsent(long arg0) throws CardException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean waitForCardPresent(long arg0) throws CardException {
	// TODO Auto-generated method stub
	return false;
    }

}
