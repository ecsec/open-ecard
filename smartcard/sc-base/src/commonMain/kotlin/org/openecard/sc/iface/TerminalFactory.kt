package org.openecard.sc.iface

interface TerminalFactory {
	val name: String

	@Throws(SmartCardStackMissing::class)
	fun load(): Terminals
}
