package org.openecard.client.scio;

import java.security.NoSuchAlgorithmException;

import javax.smartcardio.CardTerminals;



/**
 * NFC specific implementation of the TerminalFactory
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class NFCFactory implements org.openecard.client.common.ifd.TerminalFactory {
	
	 public static org.openecard.client.common.ifd.TerminalFactory getInstance() throws NoSuchAlgorithmException {
			return new NFCFactory();
		    }
	 

	    private NFCFactory() {

	    }
	 
	 
	public String getType() {
		return "NFC";
	}
	
	public CardTerminals terminals() {
		return new NFCCardTerminals();
	}
	
}

