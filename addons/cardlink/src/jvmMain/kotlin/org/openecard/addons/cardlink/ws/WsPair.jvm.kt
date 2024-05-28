package org.openecard.addons.cardlink.ws

import org.openecard.mobile.activation.Websocket

class WsPair (
	val socket: Websocket,
	val listener: WebsocketListenerImpl,
) {
	companion object {
		fun addListener(ws: Websocket): WsPair {
			val listener = WebsocketListenerImpl()
			ws.setListener(listener)
			return WsPair(ws, listener)
		}
	}
}
