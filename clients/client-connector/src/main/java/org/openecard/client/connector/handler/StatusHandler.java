package org.openecard.client.connector.handler;

import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;


/**
 * Implements a status handler to get information about the functionality of the client.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class StatusHandler implements ConnectorHandler {

    /**
     * Creates a new status handler.
     */
    public StatusHandler() {
    }

    @Override
    public ClientRequest handleRequest(HTTPRequest httpRequest) throws Exception {
	// TODO implement me.
	return null;
    }

    @Override
    public HTTPResponse handleResponse(ClientResponse clientResponse) throws Exception {
	// TODO implement me.
	return null;
    }
}
