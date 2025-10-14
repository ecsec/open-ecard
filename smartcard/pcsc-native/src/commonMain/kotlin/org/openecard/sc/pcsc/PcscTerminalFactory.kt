package org.openecard.sc.pcsc

import org.openecard.sc.iface.TerminalFactory

class PcscTerminalFactory private constructor() : TerminalFactory {
	override val name: String
		get() = "PCSC"

	override fun load(): PcscTerminals = PcscTerminals(this)

	companion object {
		val instance: PcscTerminalFactory = PcscTerminalFactory()
	}
}
