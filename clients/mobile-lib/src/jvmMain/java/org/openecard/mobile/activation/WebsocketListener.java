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

import javax.annotation.Nullable;
import org.openecard.robovm.annotations.FrameworkInterface;

/**
 * Interface for listening to websocket events.
 */
@FrameworkInterface
public interface WebsocketListener {
	/**
	 * Called when the websocket connection is established.
	 * @param webSocket the websocket that was opened.
	 */
	void onOpen(Websocket webSocket);

	/**
	 * Called when the websocket connection is closed.
	 * @param webSocket the websocket that was closed.
	 * @param statusCode the status code of the close.
	 * @param reason the reason for the close, if any.
	 */
	void onClose(Websocket webSocket, int statusCode, @Nullable  String reason);

	/**
	 * Called when an error occurs on the websocket connection.
	 * @param webSocket the websocket that had the error.
	 * @param error the error that occurred.
	 */
	void onError(Websocket webSocket, Throwable error);

	/**
	 * Called when a binary message is received.
	 * @param webSocket the websocket that received the message.
	 * @param data the binary data that was received.
	 */
	void onBinary(Websocket webSocket, byte[] data);

	/**
	 * Called when a text message is received.
	 * @param webSocket the websocket that received the message.
	 * @param data the text data that was received.
	 */
	void onText(Websocket webSocket, String data);
}
