package org.openecard.client.richclient.handler;

import java.io.BufferedReader;
import java.io.StringReader;
import org.openecard.client.richclient.activation.messages.StatusRequest;
import org.openecard.client.richclient.activation.messages.common.ClientRequest;
import org.openecard.client.richclient.activation.messages.common.ClientResponse;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class StatusHandler implements ActivationHandler {

    public StatusHandler() {
    }

    @Override
    public ClientRequest handleRequest(String request) throws Exception {
	BufferedReader reader = new BufferedReader(new StringReader(request));
	String line = reader.readLine();

	if (line.startsWith("GET")) {
	    if (line.contains("eID-Client?Status")) {

		StatusRequest statusRequest = new StatusRequest();

		return statusRequest;
	    }
	}
	return null;
    }

    @Override
    public String handleResponse(ClientResponse response) throws Exception {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
