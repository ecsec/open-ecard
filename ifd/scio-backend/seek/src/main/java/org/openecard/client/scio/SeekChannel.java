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

import java.nio.ByteBuffer;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.simalliance.openmobileapi.Channel;


/**
 * Seek implementation of smartcardio's cardChannel interface.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SeekChannel extends CardChannel {

    private Channel channel;

    public SeekChannel(Channel c) {
	this.channel = c;
    }

    @Override
    public void close() throws CardException {
	channel.close();

    }

    @Override
    public Card getCard() {
	return new SeekCard(channel.getSession());
    }

    @Override
    public int getChannelNumber() {
	return 0;
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU arg0) throws CardException {
	try {
	    return new ResponseAPDU(channel.transmit(arg0.getBytes()));
	} catch (Exception e) {
	    throw new CardException("Transmit failed", e);
	}
    }

    @Override
    public int transmit(ByteBuffer arg0, ByteBuffer arg1) throws CardException {
	throw new CardException("not yet implemented");
    }

}
