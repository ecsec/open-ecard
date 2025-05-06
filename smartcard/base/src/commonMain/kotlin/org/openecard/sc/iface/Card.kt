package org.openecard.sc.iface

interface Card {
	val terminalConnection: TerminalConnection
	val atr: Atr
	val protocol: CardProtocol
	val isContactless: Boolean
	val basicChannel: CardChannel

	fun openLogicalChannel(): CardChannel
}
