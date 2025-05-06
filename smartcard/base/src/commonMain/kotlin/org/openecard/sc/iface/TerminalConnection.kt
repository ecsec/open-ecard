package org.openecard.sc.iface

interface TerminalConnection : AutoCloseable {
	val terminal: Terminal
	val capabilities: TerminalCapabilities
	val isConnected: Boolean
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

	fun controlCommand(
		code: Int,
		command: ByteArray,
	): ByteArray

	fun beginTransaction()

	fun endTransaction()
}
