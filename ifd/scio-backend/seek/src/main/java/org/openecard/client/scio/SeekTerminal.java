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

import java.io.IOException;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import org.simalliance.openmobileapi.Reader;


/**
 * Seek implementation of smartcardio's CardTerminal interface.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SeekTerminal extends CardTerminal {

    private Reader reader;

    public SeekTerminal(Reader r) {
	this.reader = r;
    }

    @Override
    public Card connect(String arg0) throws CardException {
	try {
	    return new SeekCard(reader.openSession());
	} catch (IOException e) {
	    throw new CardException(e);
	}
    }

    @Override
    public String getName() {
	return reader.getName();
    }

    @Override
    public boolean isCardPresent() throws CardException {
	return reader.isSecureElementPresent();
    }

    @Override
    public boolean waitForCardAbsent(long arg0) throws CardException {
	// TODO
	return false;
    }

    @Override
    public boolean waitForCardPresent(long arg0) throws CardException {
	// TODO
	return false;
    }

}
