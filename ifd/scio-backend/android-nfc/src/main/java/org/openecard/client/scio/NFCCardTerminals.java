package org.openecard.client.scio;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

/**
 * NFC implementation of smartcardio's CardTerminals interface.
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class NFCCardTerminals extends CardTerminals{

	
	
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
