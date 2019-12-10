/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.scio;

import android.nfc.tech.IsoDep;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class AndroidNFCCardTerminal extends NFCCardTerminal<AndroidNFCCard> {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNFCCardTerminal.class);

    @Override
    public void prepareDevices() {
	AndroidNFCCard card = new AndroidNFCCard(this);
	this.setNFCCard(card);
    }

    void setNFCTag(IsoDep tag, int timeout) throws IOException {
	synchronized(this.cardLock) {
	    AndroidNFCCard card = getNFCCard();
	    if (card == null) {
		throw new IOException("The NFC stack was not initialized and cannot prematurely accept the NFC tag.");
	    }
	    card.setTag(tag, timeout);
	}
    }
}
