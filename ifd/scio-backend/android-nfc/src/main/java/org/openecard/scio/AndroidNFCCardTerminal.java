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
public class AndroidNFCCardTerminal extends NFCCardTerminal {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNFCCardTerminal.class);

    @Override
    public void prepareDevices() {
	LOG.info("Preparing devices on android does nothing.");
    }

    void setNFCTag(IsoDep tag, int timeout) throws IOException {

	AndroidNFCCard card = new AndroidNFCCard(tag, timeout, this);
	this.setNFCCard(card);
    }
}
