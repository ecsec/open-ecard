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
 * 
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
