package org.openecard.sc.pcsc

import au.id.micolous.kotlin.pcsc.Context
import au.id.micolous.kotlin.pcsc.connect
import au.id.micolous.kotlin.pcsc.getStatus
import au.id.micolous.kotlin.pcsc.getStatusChangeSuspend
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalStateType

private val log = KotlinLogging.logger { }

class PcscTerminal(
	override val name: String,
	override val terminals: PcscTerminals,
	private val context: Context,
) : Terminal {
	override fun isCardPresent(): Boolean = getState() == TerminalStateType.PRESENT

	override fun getState(): TerminalStateType =
		mapScioError {
			log.trace { "calling PCSC [$this] Context.getStatus($name)" }
			val status = context.getStatus(name)
			return status.eventState.toSc()
		}

	override fun connectTerminalOnly(): TerminalConnection =
		mapScioError {
			val shareMode = au.id.micolous.kotlin.pcsc.ShareMode.Direct
			val protocol = setOf(au.id.micolous.kotlin.pcsc.Protocol.Undefined)
			log.trace { "calling PCSC [$this] Context.connect($name, $shareMode, $protocol)" }
			val card = context.connect(name, shareMode, protocol)
			return PcscTerminalConnection(card, this)
		}

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	): TerminalConnection =
		mapScioError {
			log.trace { "calling PCSC [$this] Context.connect($name, $shareMode, $protocol)" }
			val card = context.connect(name, shareMode.toPcsc(), protocol.toPcsc())
			return PcscTerminalConnection(card, this)
		}

	override suspend fun waitForCardPresent() =
		mapScioError {
			var statusChanged = false
			while (!statusChanged) {
				log.trace { "calling PCSC [$this] Context.getStatus($name)" }
				val oldState = context.getStatus(listOf(name)).first()
				if (oldState.eventState.empty) {
					log.debug { "Wait for card present in terminal $name" }
					val reqState = oldState.copy(currentState = oldState.eventState)
					val state = context.getStatusChangeSuspend(Int.MAX_VALUE, listOf(reqState)).first()
					if (state.eventState.empty) {
						log.debug { "Card is not present after status call: ${state.eventState}" }
					} else {
						statusChanged = true
					}
				} else {
					// no card present in initial assumption
					statusChanged = true
				}
			}
		}

	override suspend fun waitForCardAbsent() =
		mapScioError {
			var statusChanged = false
			while (!statusChanged) {
				log.trace { "calling PCSC [$this] Context.getStatus($name)" }
				val oldState = context.getStatus(listOf(name)).first()
				if (oldState.eventState.present) {
					log.debug { "Wait for card absent in terminal $name" }
					val reqState = oldState.copy(currentState = oldState.eventState)
					val state = context.getStatusChangeSuspend(Int.MAX_VALUE, listOf(reqState)).first()
					if (state.eventState.present) {
						log.debug { "Card is not absent after status call: ${state.eventState}" }
					} else {
						statusChanged = true
					}
				} else {
					// no card present in initial assumption
					statusChanged = true
				}
			}
		}

	override fun toString(): String = "PcscTerminal(context=$context, name=$name)"
}
