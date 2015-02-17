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

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;


/**
 * PC/SC card implementation of the SCIOCard.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public class PCSCCard implements SCIOCard {

    private final PCSCTerminal terminal;
    private final Card card;

    PCSCCard(PCSCTerminal terminal, Card card) {
	this.terminal = terminal;
	this.card = card;
    }

    @Override
    public SCIOTerminal getTerminal() {
	return terminal;
    }

    @Override
    public SCIOATR getATR() {
	ATR atr = card.getATR();
	return new SCIOATR(atr.getBytes());
    }

    @Override
    public SCIOProtocol getProtocol() {
	String proto = card.getProtocol();
	return SCIOProtocol.getType(proto);
    }

    @Override
    public PCSCChannel getBasicChannel() {
	return new PCSCChannel(this, card.getBasicChannel());
    }

    @Override
    public PCSCChannel openLogicalChannel() throws SCIOException {
	try {
	    return new PCSCChannel(this, card.openLogicalChannel());
	} catch (CardException ex) {
	    String msg = "Failed to open logical channel to card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, terminal.getName()), ex);
	}
    }

    @Override
    public void beginExclusive() throws SCIOException {
	try {
	    card.beginExclusive();
	} catch (CardException ex) {
	    String msg = "Failed to get exclusive access to the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, terminal.getName()), ex);
	}
    }

    @Override
    public void endExclusive() throws SCIOException {
	try {
	    card.endExclusive();
	} catch (CardException ex) {
	    String msg = "Failed to release exclusive access to the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, terminal.getName()), ex);
	}
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws SCIOException {
	try {
	    return card.transmitControlCommand(controlCode, command);
	} catch (CardException ex) {
	    String msg = "Failed to transmit control command to the terminal '%s'.";
	    throw new SCIOException(String.format(msg, terminal.getName()), ex);
	}
    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
	try {
	    card.disconnect(reset);
	} catch (CardException ex) {
	    String msg = "Failed to disconnect (reset=%b) the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, reset, terminal.getName()), ex);
	}
    }

}
