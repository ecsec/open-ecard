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
    public void prepareDevices() throws SCIOException {
	IOSNFCCard card = new IOSNFCCard(this, config);
	card.connect();
	this.setNFCCard(card);
    }

    public void setConfig(IOSConfig config) {
	this.config = config;
    }

}
