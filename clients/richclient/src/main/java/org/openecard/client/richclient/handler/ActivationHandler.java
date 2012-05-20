package org.openecard.client.richclient.handler;

import org.openecard.client.richclient.activation.messages.common.ClientRequest;
import org.openecard.client.richclient.activation.messages.common.ClientResponse;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public interface ActivationHandler {

    public ClientRequest handleRequest(String clientRequest) throws Exception;

    public String handleResponse(ClientResponse clientResponse) throws Exception;
}
