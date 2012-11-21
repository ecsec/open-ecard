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
import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;


/**
 * Seek implementation of smartcardio's CardTerminals interface.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SeekTerminals extends CardTerminals implements SEService.CallBack {

    private SEService seService;
    private static SeekTerminals instance;

    public static SeekTerminals getInstance() {
	return instance;
    }

    public SeekTerminals(Context c) throws CardException {
	try {
	    seService = new SEService(c, this);
	    instance = this;
	} catch (SecurityException e) {
	    throw new CardException("Binding not allowed, uses-permission org.simalliance.openmobileapi.SMARTCARD?");
	} catch (Exception e) {
	    throw new CardException(e);
	}
    }

    @Override
    public List<CardTerminal> list(State arg0) throws CardException {
	while (!seService.isConnected()) {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	List<CardTerminal> list = new ArrayList<CardTerminal>();
	for (Reader r : seService.getReaders()) {
	    list.add(new SeekTerminal(r));
	}
	return list;
    }

    @Override
    public boolean waitForChange(long arg0) throws CardException {
	// TODO
	return false;
    }

    public void shutdown() {
	if (seService != null && seService.isConnected()) {
	    seService.shutdown();
	}
    }

    @Override
    public void serviceConnected(SEService service) {
	// do nothing
    }

}
