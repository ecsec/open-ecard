package org.openecard.client.connector;

import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public interface ConnectorListener {

    public ClientResponse request(ClientRequest request);
}
