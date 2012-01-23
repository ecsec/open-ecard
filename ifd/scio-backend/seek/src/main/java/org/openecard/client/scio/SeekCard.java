/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.scio;

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
 * 
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
