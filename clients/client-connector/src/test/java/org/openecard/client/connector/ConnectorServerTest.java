/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.client.connector;

import java.io.IOException;
import org.apache.http.protocol.BasicHttpProcessor;
import org.openecard.client.connector.handler.ConnectorHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ConnectorServerTest {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectorServerTest.class);

    private ConnectorHandlers handlers = new ConnectorHandlers();
    private BasicHttpProcessor interceptors = new BasicHttpProcessor();

    @Test(enabled = !true)
    public void randomPort() {
	try {
	    ConnectorServer connectorServer = new ConnectorServer(0, handlers, interceptors);
	    _logger.debug("Open random port at {}", connectorServer.getPortNumber());
	} catch (IOException e) {
	    _logger.error("Exception", e);
	}
    }

}
