/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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
import org.openecard.common.util.ByteUtils;
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
    private final NFCCard card;
    private int lengthOfLastAPDU;

    public NFCCardChannel(NFCCard card) {
	this.card = card;
    }

    @Override
    public void close() throws SCIOException {
	// we only have one channel and this will be open as long as we are connected to the tag
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
    public CardResponseAPDU transmit(CardCommandAPDU apdu) throws SCIOException {
	return transmit(apdu.toByteArray());
    }

    @Override
    public CardResponseAPDU transmit(byte[] apdu) throws SCIOException {
	if (card != null && card.isodep != null && card.isodep.isConnected()) {
	    try {
		lengthOfLastAPDU = apdu.length;
		LOG.info("Send: {}", ByteUtils.toHexString(apdu, true));
		card.isodep.setTimeout(card.getTimeoutForTransceive());
		return new CardResponseAPDU(card.isodep.transceive(apdu));
	    } catch (IOException e) {
		// TODO: check if the error code can be chosen more specifically
		throw new SCIOException("Transmit failed", SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, e);
	    }
	} else {
	    throw new SCIOException("Transmit failed, cause card removed.", SCIOErrorCode.SCARD_W_REMOVED_CARD);
	}
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws SCIOException {
	CardResponseAPDU cra = transmit(command.array());
	byte[] data = cra.toByteArray();
	response.put(data);

	return data.length;
    }

    public int getLengthOfLastAPDU() {
	return lengthOfLastAPDU;
    }

    @Override
    public boolean isBasicChannel() {
	return true;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLogicalChannel() {
	return true;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}
