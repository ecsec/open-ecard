package org.openecard.sc.iface

interface TerminalConnection : AutoCloseable {
	val terminal: Terminal

// 	val capabilities: TerminalCapabilities
	val isCardConnected: Boolean
	val card: Card?

	fun disconnect(disposition: CardDisposition = CardDisposition.LEAVE)

	override fun close() {
		disconnect(CardDisposition.LEAVE)
	}

	fun reconnect(
		protocol: PreferredCardProtocol = PreferredCardProtocol.ANY,
		shareMode: ShareMode = ShareMode.SHARED,
		disposition: CardDisposition = CardDisposition.LEAVE,
	)

	val features: Set<Feature>

	fun beginTransaction()

	fun endTransaction()
}

inline fun <reified FT : Feature> TerminalConnection.feature(): FT? = features.filterIsInstance<FT>().firstOrNull()
