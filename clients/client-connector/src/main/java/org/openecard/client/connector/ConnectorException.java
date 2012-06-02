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
package org.openecard.client.connector;


/**
 * Implements an exception for connector errors.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ConnectorException extends RuntimeException {

    /**
     * Create a new ConnectorException.
     */
    public ConnectorException() {
	super();
    }

    /**
     * Create a new ConnectorException.
     *
     * @param message Message
     */
    public ConnectorException(String message) {
	super(message);
    }

    /**
     * Create a new ConnectorException.
     *
     * @param message Message
     * @param throwable Throwable
     */
    public ConnectorException(String message, Throwable throwable) {
	super(message, throwable);
    }

}
