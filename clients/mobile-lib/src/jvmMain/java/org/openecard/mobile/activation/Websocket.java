/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.mobile.activation;

import org.openecard.robovm.annotations.FrameworkInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for a Websocket connection.
 */
@FrameworkInterface
public interface Websocket {

	/**
	 * Set the listener for the websocket events.
	 * This method replaces an existing listener, if one is already set.
	 * @param listener the listener to set.
	 */
	void setListener(WebsocketListener listener);
	/**
	 * Remove the listener for the websocket events, if one is set.
	 */
	void removeListener();

	@Nonnull
	String getUrl();

	/**
	 * Gets the selected subprotocol once the connection is established.
	 * @return the selected subprotocol or null if no subprotocol was selected.
	 */
	@Nullable
	String getSubProtocol();

	/**
	 * Connect to the server.
	 * This method can also be used to reestablish a lost connection.
	 * @throws WebsocketException if the connection could not be established.
	 */
	void connect() throws WebsocketException;

	/**
	 * Get open state of the connection.
	 * @return true if the connection is open, false otherwise.
	 */
	boolean isOpen();

	/**
	 * Get closed state of the connection.
	 * @return true if the connection is closed, false otherwise.
	 */
	default boolean isClosed() {
		return !isOpen();
	}

	/**
	 * Get failed state of the connection.
	 * @return true if the connection is failed, false otherwise.
	 */
	boolean isFailed();

	/**
	 * Initiate the closing handshake.
	 * @param statusCode the status code to send.
	 * @param reason the reason for closing the connection, or null if none should be given.
	 * @throws WebsocketException if the connection could not be closed.
	 */
	void close(int statusCode, @Nullable String reason) throws WebsocketException;

	/**
	 * Send a text frame.
	 * @param data the data to send.
	 * @throws WebsocketException if the data could not be sent.
	 */
	void send(String data) throws WebsocketException;

	/**
	 * Send a binary frame.
	 * @param data the data to send.
	 * @throws WebsocketException if the data could not be sent.
	 */
	//void send(byte[] data) throws WebsocketException;

}
