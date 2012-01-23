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

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

/**
 * NFC implementation of smartcardio's CardTerminals interface.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class NFCCardTerminals extends CardTerminals {

    @Override
    public List<CardTerminal> list(State arg0) throws CardException {
	List<CardTerminal> list = new ArrayList<CardTerminal>();
	list.add(NFCCardTerminal.getInstance());
	return list;
    }

    @Override
    public boolean waitForChange(long arg0) throws CardException {
	// TODO Auto-generated method stub
	return false;
    }

}
