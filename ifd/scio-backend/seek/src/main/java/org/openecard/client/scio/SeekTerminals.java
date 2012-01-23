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

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;

/**
 * Seek implementation of smartcardio's CardTerminals interface.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class SeekTerminals extends CardTerminals implements SEService.CallBack {

    private SEService seService;
    private static SeekTerminals instance;

    public static SeekTerminals getInstance() {
	return instance;
    }

    public SeekTerminals(Context c) throws CardException {
	try {
	    seService = new SEService(c, this);
	    instance = this;
	} catch (SecurityException e) {
	    throw new CardException("Binding not allowed, uses-permission org.simalliance.openmobileapi.SMARTCARD?");
	} catch (Exception e) {
	    throw new CardException(e);
	}
    }

    @Override
    public List<CardTerminal> list(State arg0) throws CardException {
	while (!seService.isConnected()) {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	List<CardTerminal> list = new ArrayList<CardTerminal>();
	for (Reader r : seService.getReaders()) {
	    list.add(new SeekTerminal(r));
	}
	return list;
    }

    @Override
    public boolean waitForChange(long arg0) throws CardException {
	// TODO
	return false;
    }

    public void shutdown() {
	if (seService != null && seService.isConnected()) {
	    seService.shutdown();
	}
    }

    @Override
    public void serviceConnected(SEService service) {
	// do nothing
    }

}
