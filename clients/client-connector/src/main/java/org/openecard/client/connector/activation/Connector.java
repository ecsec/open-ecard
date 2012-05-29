/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.connector.activation;

import java.io.IOException;
import java.util.List;
import org.openecard.client.connector.handler.ConnectorHandler;
import org.openecard.client.connector.handler.StatusHandler;
import org.openecard.client.connector.handler.TCTokenHandler;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class Connector {

    private static Connector connector;
    private List<ConnectorHandler> connectorHandlers;
    private List<ConnectorListener> connectorListeners;

    /**
     * Returns a new instance of the Connector.
     *
     * @return Activation
     * @throws IOException
     */
    public static Connector getInstance() throws IOException {
	if (connector == null) {
	    connector = new Connector();
	}
	return connector;
    }

    /**
     * Creates a new Activation.
     *
     * @throws IOException
     */
    protected Connector() throws IOException {
	ConnectorServer connectorServer = ConnectorServer.getInstance();

	// Add handlers
	connector.addConnectorHandler(new TCTokenHandler());
	connector.addConnectorHandler(new StatusHandler());

	connectorServer.start();
    }

    protected List<ConnectorListener> getConnectorListeners() {
	return connectorListeners;
    }

    public void addConnectorListener(ConnectorListener listener) {
	connectorListeners.add(listener);
    }

    public void removeConnectorListener(ConnectorListener listener) {
	connectorListeners.remove(listener);
    }

    protected List<ConnectorHandler> getConnectorHandlers() {
	return connectorHandlers;
    }

    public void addConnectorHandler(ConnectorHandler handler) {
	connectorHandlers.add(handler);
    }

    public void removeConnectorHandler(ConnectorHandler handler) {
	connectorHandlers.remove(handler);
    }
}
