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

import javax.smartcardio.*;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOProtocol;
import org.openecard.common.ifd.scio.SCIOTerminal;
import static org.openecard.scio.PCSCExceptionExtractor.getCode;


/**
 * PC/SC card implementation of the SCIOCard.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public class PCSCCard implements SCIOCard {

    private final PCSCTerminal terminal;
    private final Card card;
    private Boolean isContactless;

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
    public boolean isContactless() {
	if (isContactless == null) {
	    this.isContactless = hasContactlessUid();
	}

	return isContactless;
    }

    private boolean hasContactlessUid() {
	try {
	    CardCommandAPDU getUidCmd = new CardCommandAPDU((byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (short) 0xFF);
	    CommandAPDU convertCommand = new CommandAPDU(getUidCmd.toByteArray());
	    ResponseAPDU response = card.getBasicChannel().transmit(convertCommand);
	    CardResponseAPDU cr = new CardResponseAPDU(response.getBytes());
	    return cr.isNormalProcessed();
	} catch (Exception ex) {
	    // don't care
	    return false;
	}
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
	    throw new SCIOException(String.format(msg, terminal.getName()), getCode(ex), ex);
	}
    }

    @Override
    public void beginExclusive() throws SCIOException {
	try {
	    card.beginExclusive();
	} catch (CardException ex) {
	    String msg = "Failed to get exclusive access to the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, terminal.getName()), getCode(ex), ex);
	}
    }

    @Override
    public void endExclusive() throws SCIOException {
	try {
	    card.endExclusive();
	} catch (CardException ex) {
	    String msg = "Failed to release exclusive access to the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, terminal.getName()), getCode(ex), ex);
	}
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws SCIOException {
	try {
	    return card.transmitControlCommand(controlCode, command);
	} catch (CardException ex) {
	    String msg = "Failed to transmit control command to the terminal '%s'.";
	    throw new SCIOException(String.format(msg, terminal.getName()), getCode(ex), ex);
	}
    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
	try {
	    card.disconnect(reset);
	} catch (CardException ex) {
	    String msg = "Failed to disconnect (reset=%b) the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, reset, terminal.getName()), getCode(ex), ex);
	}
    }

}
