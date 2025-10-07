package org.openecard.sc.pcsc

import au.id.micolous.kotlin.pcsc.Context
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals

class PcscTerminals(
	override val factory: TerminalFactory,
) : Terminals {
	private var context: Context? = null

	private val contextAsserted: Context
		get() {
			return context ?: throw InvalidHandle("No PCSC context is established")
		}

	override val isEstablished: Boolean
		get() = context != null
	override val supportsControlCommand: Boolean = true

	override fun establishContext() =
		mapScioError {
			context = Context.establish()
		}

	override fun releaseContext() =
		mapScioError {
			contextAsserted.release()
			context = null
		}

	override fun list(): List<PcscTerminal> =
		mapScioError {
			contextAsserted.let { ctx ->
				ctx.listReaders().map { name ->
					PcscTerminal(name, this, ctx)
				}
			}
		}

	override fun getTerminal(name: String): PcscTerminal? =
		mapScioError {
			contextAsserted.let { ctx ->
				ctx
					.listReaders()
					.find { it == name }
					?.let {
						PcscTerminal(name, this, ctx)
					}
			}
		}
}
