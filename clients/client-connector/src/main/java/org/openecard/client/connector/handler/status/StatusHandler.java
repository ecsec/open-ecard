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

package org.openecard.client.connector.handler.status;

import org.openecard.client.connector.ConnectorHTTPException;
import org.openecard.client.connector.client.ClientRequest;
import org.openecard.client.connector.client.ClientResponse;
import org.openecard.client.connector.client.ConnectorListeners;
import org.openecard.client.connector.handler.ConnectorClientHandler;
import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.http.HTTPStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a StatusHandler to get information about the functionality of the client.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class StatusHandler extends ConnectorClientHandler {

    private static final Logger _logger = LoggerFactory.getLogger(StatusHandler.class);


    /**
     * Creates a new StatusHandler.
     *
     * @param listeners ConnectorListeners
     */
    public StatusHandler(ConnectorListeners listeners) {
	super("/status", listeners);
    }

    @Override
    public ClientRequest handleRequest(HTTPRequest httpRequest) throws Exception {
	_logger.warn("Implement me!");
	//TODO implement me.
	throw new ConnectorHTTPException(HTTPStatusCode.NOT_FOUND_404, "Implement me!");
    }

    @Override
    public HTTPResponse handleResponse(ClientResponse clientResponse) throws Exception {
	_logger.warn("Implement me!");
	//TODO implement me.
	throw new ConnectorHTTPException(HTTPStatusCode.NOT_FOUND_404, "Implement me!");
    }

}
