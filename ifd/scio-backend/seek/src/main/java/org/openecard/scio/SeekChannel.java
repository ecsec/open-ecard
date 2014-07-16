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
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import org.simalliance.openmobileapi.Channel;


/**
 * Seek implementation of smartcardio's cardChannel interface.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SeekChannel implements SCIOChannel {

    private Channel channel;

    public SeekChannel(Channel c) {
	this.channel = c;
    }

    @Override
    public void close() throws SCIOException {
	channel.close();

    }

    @Override
    public SCIOCard getCard() {
	return new SeekCard(channel.getSession());
    }

    @Override
    public int getChannelNumber() {
	return 0;
    }

    @Override
    public CardResponseAPDU transmit(CardCommandAPDU apdu) throws SCIOException {
	try {
	    return new CardResponseAPDU(channel.transmit(apdu.toByteArray()));
	} catch (IOException e) {
	    throw new SCIOException("Transmit failed", e);
	}
    }
    
    @Override
    public CardResponseAPDU transmit(byte[] apdu) throws SCIOException {
        try {
	    return new CardResponseAPDU(channel.transmit(apdu));
	} catch (Exception e) {
	    throw new SCIOException("Transmit failed", e);
	}
    }

    @Override
    public int transmit(ByteBuffer arg0, ByteBuffer arg1) throws SCIOException {
	throw new SCIOException("not yet implemented");
    }

}
