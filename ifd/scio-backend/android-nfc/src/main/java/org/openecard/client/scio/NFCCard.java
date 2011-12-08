package org.openecard.client.scio;

import java.io.IOException;

import android.nfc.tech.IsoDep;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

/**
 * NFC implementation of smartcardio's Card interface.
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class NFCCard extends Card{

	protected IsoDep isodep = null;
	private NFCCardChannel nfcCardChannel = new NFCCardChannel(this);
	
	public NFCCard(IsoDep tag){
		this.isodep = tag;
	}
		
	@Override
	public void beginExclusive() throws CardException {
		// TODO 
		
	}

	@Override
	public void disconnect(boolean arg0) throws CardException {
		try {
			this.isodep.close();
		} catch (IOException e) {
			throw new CardException("Disconnect failed", e);
		}
		return;
	}

	@Override
	public void endExclusive() throws CardException {
		// TODO 
		
	}

	@Override
	public ATR getATR() {
		/* for now theres no way to get the ATR in android nfc api */
		
		return new ATR(new byte[0]);
	}

	@Override
	public CardChannel getBasicChannel() {
		return this.nfcCardChannel;
	}

	@Override
	public String getProtocol() {
		/* for now theres no way to get the used protocol in android nfc api */
		return "";
	}

	@Override
	public CardChannel openLogicalChannel() throws CardException {
		return this.nfcCardChannel;
	}

	@Override
	public byte[] transmitControlCommand(int arg0, byte[] arg1) throws CardException {
		return new byte[0];
	}

}
