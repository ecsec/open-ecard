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

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;


/**
 * NFC implementation of smartcardio's cardChannel interface.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class NFCCardChannel extends CardChannel {

    private NFCCard card;

    public NFCCardChannel(NFCCard card) {
	this.card = card;
    }

    @Override
    public void close() throws CardException {
	 // we only have one channel and this will be open as long as we are connected to the tag
    }

    @Override
    public Card getCard() {
	return this.card;
    }

    @Override
    public int getChannelNumber() {
	return 0;
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU arg0) throws CardException {
	try {
	    return new ResponseAPDU(this.card.isodep.transceive(arg0.getBytes()));
	} catch (IOException e) {
	    throw new CardException("Transmit failed", e);
	}
    }

    @Override
    public int transmit(ByteBuffer arg0, ByteBuffer arg1) throws CardException {
	throw new CardException("not yet  implemented");
    }

}
