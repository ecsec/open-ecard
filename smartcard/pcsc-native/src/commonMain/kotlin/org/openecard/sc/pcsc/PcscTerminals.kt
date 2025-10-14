package org.openecard.sc.pcsc

import au.id.micolous.kotlin.pcsc.Context
import au.id.micolous.kotlin.pcsc.ReaderState
import au.id.micolous.kotlin.pcsc.getAllReaderStatus
import au.id.micolous.kotlin.pcsc.getStatus
import au.id.micolous.kotlin.pcsc.getStatusChangeSuspend
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.ReaderUnsupported
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
			log.trace { "calling PCSC [$this] Context.establish()" }
			context = Context.establish()
		}

	override fun releaseContext() =
		mapScioError {
			log.trace { "calling PCSC [$this] Context.release()" }
			contextAsserted.release()
			context = null
		}

	override fun list(): List<PcscTerminal> =
		mapScioError {
			contextAsserted.let { ctx ->
				// update reader status, then return list
				log.trace { "calling PCSC [$this] Context.getAllReaderStatus()" }
				ctx.getAllReaderStatus()
				log.trace { "calling PCSC [$this] Context.listReaders()" }
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
					log.trace { "calling PCSC [$this] Context.getStatus($name)" }
					val state = ctx.getStatus(name)
					PcscTerminal(name, this, ctx)
				}
			}
		} catch (ex: UnknownReader) {
			null
		} catch (ex: ReaderUnavailable) {
			null
		}

	override suspend fun waitForTerminalChange(currentState: List<String>) {
		val eventReaderName = "\\\\?PnP?\\Notification"
		mapScioError {
			contextAsserted.let { ctx ->
				var curState =
					if (currentState.isEmpty()) {
						listOf(ReaderState(reader = eventReaderName))
					} else {
						val curStateReq =
							currentState.map { name ->
								ReaderState(reader = name)
							} + ReaderState(reader = eventReaderName)
						log.trace { "calling PCSC [$this] Context.getStatusChange(...)" }
						ctx
							.getStatusChange(0, curStateReq)
							.map { state -> state.copy(currentState = state.eventState) }
					}

				// on osx, the PnP reader does not exist and yields unknown for that status
				if (curState.last().eventState.unknown) {
					throw ReaderUnsupported("Special event reader ($eventReaderName) is not supported")
				}

				// check if we already have a change to the expected state
				if (curState.any { it.eventState.unknown }) {
					log.debug { "Initial terminal status is not correct. Returning to notify change." }
					return
				}
				// wait for a change to the determined state
				while (true) {
					log.debug { "Wait for terminal change" }
					val newState = ctx.getStatusChangeSuspend(Int.MAX_VALUE, curState)
					// check if we already have a change to the new state
					if (newState.last().eventState.changed) {
						log.debug { "Terminal list has changed" }
						return
					} else {
						curState = newState.map { state -> state.copy(currentState = state.eventState) }
					}
				}
			}
		}
	}

	override fun toString(): String = "PcscTerminals(context=$context)"
}
