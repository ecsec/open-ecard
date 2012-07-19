package org.openecard.client.scio;

import javax.smartcardio.*;
import org.openecard.client.common.ifd.TerminalFactory;

import sun.security.smartcardio.PCSC;
import sun.security.smartcardio.PCSCException;
import sun.security.smartcardio.PCSCTerminals;

/**
 * TerminalFactory for PC/SC on Android.
 *
 * @author Dirk Petrautzki
 */
public final class AndroidPCSCFactory implements TerminalFactory {

    public String getType() {
	return "Android PC/SC factory";
    }

    public CardTerminals terminals() {
	PCSC.checkAvailable();
	try {
	    PCSCTerminals.initContext();
	} catch (PCSCException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return new PCSCTerminals();
    }

}
