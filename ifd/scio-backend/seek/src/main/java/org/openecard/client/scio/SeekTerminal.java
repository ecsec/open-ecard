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

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.simalliance.openmobileapi.Reader;

/**
 * Seek implementation of smartcardio's CardTerminal interface.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SeekTerminal extends CardTerminal {

    private Reader reader;

    public SeekTerminal(Reader r) {
	this.reader = r;
    }

    @Override
    public Card connect(String arg0) throws CardException {
	try {
	    return new SeekCard(reader.openSession());
	} catch (IOException e) {
	    throw new CardException(e);
	}
    }

    @Override
    public String getName() {
	return reader.getName();
    }

    @Override
    public boolean isCardPresent() throws CardException {
	return reader.isSecureElementPresent();
    }

    @Override
    public boolean waitForCardAbsent(long arg0) throws CardException {
	// TODO
	return false;
    }

    @Override
    public boolean waitForCardPresent(long arg0) throws CardException {
	// TODO
	return false;
    }

}
