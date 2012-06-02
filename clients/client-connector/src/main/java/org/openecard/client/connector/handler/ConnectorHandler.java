package org.openecard.client.connector.handler;

import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;


/**
 * Implements a common connector handler.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public interface ConnectorHandler {

    /**
     * Handles a HTTP request and creates a client request.
     * If the request can be handled by the handler it should return a client request.
     * Otherwise the handler should return null.
     *
     * @param httpRequest HTTP request
     * @return A client request or null
     * @throws Exception If the request should be handled by the handler but is malformed
     */
    public ClientRequest handleRequest(HTTPRequest httpRequest) throws Exception;

    /**
     * Handles a client response and creates a HTTP response.
     *
     * @param clientResponse Client response
     * @return A HTTP response
     * @throws Exception
     */
    public HTTPResponse handleResponse(ClientResponse clientResponse) throws Exception;
}
