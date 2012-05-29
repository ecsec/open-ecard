package org.openecard.client.connector.handler;

import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public interface ConnectorHandler {

    public ClientRequest handleRequest(String clientRequest) throws Exception;

    public String handleResponse(ClientResponse clientResponse) throws Exception;
}
