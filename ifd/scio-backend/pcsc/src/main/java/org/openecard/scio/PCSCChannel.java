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

import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PC/SC channel implementation of the SCIOChannel.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public class PCSCChannel implements SCIOChannel {

    private static final Logger logger = LoggerFactory.getLogger(PCSCChannel.class);

    private final PCSCCard card;
    private final CardChannel channel;
    private final int channelNum;

    PCSCChannel(@Nonnull PCSCCard card, @Nonnull CardChannel channel) {
	this.card = card;
        this.channel = channel;
	// pretend channel num = 0 in case there is something really fucked up during init of the card
	int num = 0;
	try {
	    num = channel.getChannelNumber();
	} catch (IllegalStateException ex) {
	    // very unlikely event, that the card is removed during the connect phase
	    String msg = "Card disconnected during connect phase, pretending to be channel 0 regardless of what it is.";
	    logger.error(msg);
	}
	this.channelNum = num;
    }

    @Override
    public PCSCCard getCard() {
	return card;
    }

    @Override
    public int getChannelNumber() {
        return channelNum;
    }

    @Override
    public boolean isBasicChannel() {
	return channel.getChannelNumber() == 0;
    }

    @Override
    public boolean isLogicalChannel() {
	return ! isBasicChannel();
    }

    @Override
    public CardResponseAPDU transmit(byte[] command) throws SCIOException {
	return transmit(new CardCommandAPDU(command));
    }

    @Override
    public CardResponseAPDU transmit(CardCommandAPDU apdu) throws SCIOException {
        try {
            CommandAPDU ConvertCommand = new CommandAPDU(apdu.toByteArray());
            ResponseAPDU Response = channel.transmit(ConvertCommand);
            return new CardResponseAPDU(Response.getBytes());
	} catch (CardException ex) {
	    String msg = "Failed to transmit APDU to the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, card.getTerminal().getName()), ex);
	}
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws SCIOException {
        try {
            return channel.transmit(command, response);
        } catch (CardException ex) {
	    String msg = "Failed to transmit APDU to the card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, card.getTerminal().getName()), ex);
        }
    }

    @Override
    public void close() throws SCIOException {
        try {
            channel.close();
        } catch (CardException ex) {
	    String msg = "Failed to close channel to card in terminal '%s'.";
	    throw new SCIOException(String.format(msg, card.getTerminal().getName()), ex);
        }
    }

}
