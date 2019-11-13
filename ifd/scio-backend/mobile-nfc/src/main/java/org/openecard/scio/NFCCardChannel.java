/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of smartcardio's cardChannel interface.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class NFCCardChannel implements SCIOChannel {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardChannel.class);

    private final AbstractNFCCard card;

    public NFCCardChannel(AbstractNFCCard card) {
	this.card = card;
    }

    @Override
    public void close() throws SCIOException {
	// we only have one channel and this will be open as long as we are connected to the tag
	LOG.debug("SCIOChannel: closing");
    }

    @Override
    public SCIOCard getCard() {
	return card;
    }

    @Override
    public int getChannelNumber() {
	return 0;
    }

    @Override
    public CardResponseAPDU transmit(CardCommandAPDU apdu) throws SCIOException, IllegalStateException {
	return transmit(apdu.toByteArray());
    }

    @Override
    public CardResponseAPDU transmit(byte[] apdu) throws SCIOException, IllegalStateException {
	synchronized (card) {
	    try {
		return new CardResponseAPDU(card.transceive(apdu));
	    } catch (IOException ex) {
		if (!card.isTagPresent()) {
		    throw new IllegalStateException("Transmit of apdu command failed, because the card has been removed.");
		} else {
		    // TODO: check if the error code can be chosen more specifically
		    throw new SCIOException("Transmit failed.", SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, ex);
		}
	    }
	}
    }


    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws SCIOException, IllegalStateException {
	CardResponseAPDU cra = transmit(command.array());
	byte[] data = cra.toByteArray();
	response.put(data);

	return data.length;
    }

    @Override
    public boolean isBasicChannel() {
	return true;
    }

    @Override
    public boolean isLogicalChannel() {
	return false;
    }

}
