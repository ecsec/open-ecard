package org.openecard.addons.cardlink.ws

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.mobile.activation.Websocket
import org.openecard.mobile.activation.WebsocketListener

private val logger = KotlinLogging.logger { }

class WsPair(
	val socket: Websocket,
	val listener: WebsocketListenerImpl,
	private val successor: WebsocketListener,
) : AutoCloseable {
	companion object {
		fun withNewListener(
			ws: Websocket,
			successor: WebsocketListener,
		): WsPair {
			val listener = WebsocketListenerImpl()
			ws.setListener(listener)
			return WsPair(ws, listener, successor)
		}
	}

	private fun switchToSuccessorListener() {
		logger.info { "Replacing websocket listener with provided successor." }
		socket.setListener(successor)
	}

	override fun close() {
		switchToSuccessorListener()
	}
}
