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

import java.io.IOException;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class IOSNFCCardTerminal extends NFCCardTerminal<IOSNFCCard> {

    private static final Logger LOG = LoggerFactory.getLogger(IOSNFCCardTerminal.class);

    private IOSConfig config;

    @Override
    public void prepareDevices() {
	try {
	    IOSNFCCard card = new IOSNFCCard(this, config);
	    card.connect();
	    this.setNFCCard(card);
	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	} catch (SCIOException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void setConfig(IOSConfig config) {
	this.config = config;
    }

}
