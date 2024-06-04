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

package org.openecard.addons.cardlink.ws

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.openecard.mobile.activation.Websocket
import org.openecard.mobile.activation.WebsocketListener
import kotlin.time.Duration


private val logger = KotlinLogging.logger {}

/**
 * @author Mike Prechtl
 */
class WebsocketListenerImpl: WebsocketListener {

	private lateinit var messageChannel: Channel<GematikEnvelope>

	private var isOpen : Boolean = false

	override fun onOpen(webSocket: Websocket?) {
		messageChannel = Channel()
		isOpen = true
	}

	override fun onClose(webSocket: Websocket?, statusCode: Int, reason: String?) {
		messageChannel.close()
		isOpen = false
	}

	override fun onError(webSocket: Websocket?, error: String?) {
		// TODO: Implement onError handler
		logger.error { "onError handler not implemented yet" }
	}

	@OptIn(DelicateCoroutinesApi::class)
	override fun onText(webSocket: Websocket?, data: String?) {
		GlobalScope.launch {
			if (data != null) {
				logger.debug { "Received message: $data" }
				val egkEnvelope = cardLinkJsonFormatter.decodeFromString<GematikEnvelope>(data)
				messageChannel.send(egkEnvelope)
			} else {
				logger.debug { "Received empty data in CardLink Websocket." }
			}
		}
	}

	fun isOpen() : Boolean {
		return isOpen
	}

	fun nextMessageBlocking() : GematikEnvelope? {
		return runBlocking {
			nextMessage()
		}
	}

	suspend fun nextMessage(timeout: Duration = Duration.parse("30s")) : GematikEnvelope? {
		try {
			return withTimeoutOrNull(timeout) {
				return@withTimeoutOrNull messageChannel.receive()
			}
		} catch (_: TimeoutCancellationException) {
			logger.debug { "Timeout happened during waiting for CardLink message." }
			return null
		}
	}
}
