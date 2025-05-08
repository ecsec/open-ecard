package org.openecard.sc.pcsc

import jnasmartcardio.Smartcardio
import org.openecard.sc.iface.SmartCardStackMissing
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals
import java.security.Security

class PcscTerminalFactory private constructor(
	private val scio: javax.smartcardio.TerminalFactory,
) : TerminalFactory {
	override val name: String
		get() = "PCSC"

	override fun load(): Terminals = PcscTerminals(this, scio)

	companion object {
		private var isInit = false

		@get:Throws(SmartCardStackMissing::class)
		val instance: PcscTerminalFactory
			get() {
				try {
					synchronized(PcscTerminalFactory) {
						if (!isInit) {
							Security.addProvider(Smartcardio())
							isInit = true
						}
					}
					return PcscTerminalFactory(
						javax.smartcardio.TerminalFactory.getInstance(
							"PC/SC",
							null,
							Smartcardio.PROVIDER_NAME,
						),
					)
				} catch (ex: UnsatisfiedLinkError) {
					throw SmartCardStackMissing("Failed to open pcsc library", ex)
				}
			}
	}
}
