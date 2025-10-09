package org.openecard.sc.pcsc

import au.id.micolous.kotlin.pcsc.Context
import au.id.micolous.kotlin.pcsc.PCSCError
import au.id.micolous.kotlin.pcsc.getAllReaderStatus
import au.id.micolous.kotlin.pcsc.getStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals
import org.openecard.sc.iface.UnknownReader

private val log = KotlinLogging.logger { }

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
			log.debug { "calling PCSC [$this] Context.establish()" }
			context = Context.establish()
		}

	override fun releaseContext() =
		mapScioError {
			log.debug { "calling PCSC [$this] Context.release()" }
			contextAsserted.release()
			context = null
		}

	override fun list(): List<PcscTerminal> =
		mapScioError {
			contextAsserted.let { ctx ->
				// update reader status, then return list
				log.debug { "calling PCSC [$this] Context.getAllReaderStatus()" }
				ctx.getAllReaderStatus()
				log.debug { "calling PCSC [$this] Context.listReaders()" }
				ctx.listReaders().map { name ->
					PcscTerminal(name, this, ctx)
				}
			}
		}

	override fun getTerminal(name: String): PcscTerminal? =
		try {
			mapScioError {
				contextAsserted.let { ctx ->
					// update reader status, then return the requested reader
					log.debug { "calling PCSC [$this] Context.getStatus($name)" }
					val state = ctx.getStatus(name)
					PcscTerminal(name, this, ctx)
				}
			}
		} catch (ex: UnknownReader) {
			null
		} catch (ex: ReaderUnavailable) {
			null
		}

	override fun toString(): String = "PcscTerminals(context=$context)"
}
