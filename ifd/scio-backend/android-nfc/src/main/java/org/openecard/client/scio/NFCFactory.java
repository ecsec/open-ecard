package org.openecard.client.scio;

import javax.smartcardio.CardTerminals;
import org.openecard.client.common.ifd.TerminalFactory;


/**
 * NFC specific implementation of the TerminalFactory
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class NFCFactory implements TerminalFactory {

    @Override
    public String getType() {
	return "NFC";
    }

    @Override
    public CardTerminals terminals() {
	return new NFCCardTerminals();
    }

}
