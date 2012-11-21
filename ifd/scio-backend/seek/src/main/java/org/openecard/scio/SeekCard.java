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
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import org.simalliance.openmobileapi.Session;


/**
 * Seek implementation of smartcardio's Card interface.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SeekCard extends Card {

    private static byte[] aid = null;
    private Session session;

    /*
     * SELECT-command is not allowed in seek, so we must set the aid beforhand
     * and use it in getbasicchannel and openlogicalchannel. Cant pass it there
     * because the smartcardios interface doesnt declare parameters for that
     * functions.
     */
    public static void setAID(byte[] b) {
	aid = b;
    }

    public SeekCard(Session s) {
	this.session = s;
    }

    @Override
    public void beginExclusive() throws CardException {
	// TODO
    }

    @Override
    public void disconnect(boolean arg0) throws CardException {
	this.session.close();
    }

    @Override
    public void endExclusive() throws CardException {
	// TODO
    }

    @Override
    public ATR getATR() {
	return new ATR(this.session.getATR());
    }

    @Override
    public CardChannel getBasicChannel() {
	try {
	    return new SeekChannel(this.session.openBasicChannel(aid));
	} catch (IOException e) {
	    return null;
	}
    }

    @Override
    public String getProtocol() {
	/* for now theres no way to get the used protocol in seek */
	return "";
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
	try {
	    return new SeekChannel(this.session.openLogicalChannel(aid));
	} catch (IOException e) {
	    throw new CardException(e);
	}
    }

    @Override
    public byte[] transmitControlCommand(int arg0, byte[] arg1) throws CardException {
	return new byte[0];
    }

}
