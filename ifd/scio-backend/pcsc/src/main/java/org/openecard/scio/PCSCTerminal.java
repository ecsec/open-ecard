/****************************************************************************
 * Copyright (C) 2014-2015 TU Darmstadt.
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

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;
import static org.openecard.scio.PCSCExceptionExtractor.getCode;


/**
 * PC/SC terminal implementation of the SCIOTerminal.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public class PCSCTerminal implements SCIOTerminal {

    private final CardTerminal terminal;

    PCSCTerminal(CardTerminal terminal) {
	this.terminal = terminal;
    }

    @Override
    public String getName() {
	return terminal.getName();
    }


    @Override
    public SCIOCard connect(SCIOProtocol protocol) throws SCIOException, IllegalStateException {
	try {
	    Card c = terminal.connect(protocol.identifier);
	    return new PCSCCard(this, c);
	} catch (CardNotPresentException ex) {
	    String msg = "Card has been removed before connect could be finished for terminal '%s'.";
	    throw new IllegalArgumentException(String.format(msg, getName()));
	} catch (CardException ex) {
	    String msg = "Failed to connect the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, getName()), getCode(ex), ex);
	}
    }

    @Override
    public boolean isCardPresent() throws SCIOException {
	try {
	    return terminal.isCardPresent();
	} catch (CardException ex) {
	    throw new SCIOException("Failed to determine whether card is present or not.", getCode(ex), ex);
	}
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws SCIOException {
	try {
	    return terminal.waitForCardPresent(timeout);
	} catch (CardException ex) {
	    String msg = "Failed to wait for card present event in terminal '%s'.";
	    throw new SCIOException(String.format(msg, getName()), getCode(ex), ex);
	}
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws SCIOException {
	try {
	    return terminal.waitForCardAbsent(timeout);
	} catch (CardException ex) {
	    String msg = "Failed to wait for card absent event in terminal '%s'.";
	    throw new SCIOException(String.format(msg, getName()), getCode(ex), ex);
	}
    }

}
