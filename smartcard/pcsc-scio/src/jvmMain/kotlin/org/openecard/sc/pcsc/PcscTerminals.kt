package org.openecard.sc.pcsc

import jnasmartcardio.Smartcardio
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.Terminals
import javax.smartcardio.CardTerminal

class PcscTerminals internal constructor(
	override val factory: PcscTerminalFactory,
	private val scio: javax.smartcardio.TerminalFactory,
) : Terminals {
	// using the implementation allows us to call close
	private var scioTerminals: Smartcardio.JnaCardTerminals? = null

	override val isEstablished: Boolean
		get() = scioTerminals != null
	override val supportsControlCommand: Boolean = true

	override fun establishContext() =
		mapScioError {
			scioTerminals = scio.terminals() as Smartcardio.JnaCardTerminals
		}

	override fun releaseContext() =
		mapScioError {
			scioTerminals?.close()
			scioTerminals = null
		}

	/**
	 * This implementation does not throw [org.openecard.sc.iface.NoReadersAvailable], but returns an empty list instead.
	 */
	override fun list(): List<Terminal> =
		mapScioError {
			val scioTerminals = assertInitialized()
			scioTerminals.list().map { PcscTerminal(this, it.name) }
		}

	/**
	 * This implementation does not throw [org.openecard.sc.iface.NoReadersAvailable], but returns an empty list instead.
	 */
	override fun getTerminal(name: String): Terminal? =
		mapScioError {
			val scioTerminals = assertInitialized()
			scioTerminals.list().find { it.name == name }?.let {
				PcscTerminal(this, it.name)
			}
		}

	@Throws(InvalidHandle::class)
	internal fun getScioTerminal(name: String): CardTerminal? =
		mapScioError {
			val scioTerminals = assertInitialized()
			scioTerminals.getTerminal(name)
		}

	internal fun assertInitialized(): Smartcardio.JnaCardTerminals =
		scioTerminals ?: throw InvalidHandle("PCSC context is not initialized")
}
