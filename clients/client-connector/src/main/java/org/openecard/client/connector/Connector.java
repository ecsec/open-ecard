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
import org.openecard.client.connector.handler.StatusHandler;
import org.openecard.client.connector.handler.TCTokenHandler;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class Connector {

    private final ConnectorServer connectorServer;
    private final ConnectorHandlers handlers = new ConnectorHandlers();
    private final ConnectorListeners listeners = new ConnectorListeners();


    /**
     * Create a new Activation.
     *
     * @param port Port the server should listen on.
     * @throws IOException
     */
    protected Connector(int port) throws Exception {
	// Add handlers
	handlers.addConnectorHandler(new TCTokenHandler());
	handlers.addConnectorHandler(new StatusHandler());

	connectorServer = new ConnectorServer(port, handlers, listeners);
	connectorServer.start();
    }

    /**
     * Create a new Activation.
     * The port is set to any available number.
     *
     * @throws Exception if an I/O error occurs when opening the socket.
     */
    public Connector() throws Exception {
	this(0);
    }

    public ConnectorHandlers getHandlers() {
	return handlers;
    }

    public ConnectorListeners getListeners() {
	return listeners;
    }

    /**
     * @see ConnectorServer#getPortNumber() 
     * @return Port number where the connector is reachable.
     */
    public int getPortNumber() {
	return connectorServer.getPortNumber();
    }

}
