package org.openecard.richclient.pinmanagement.npa

import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory

class NpaPacePinModel(
	private val application: SmartcardApplication,
) {
	val pacePin: PaceDid =
		application.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_PIN" }
			?: throw IllegalStateException("PACE PIN not found")

	val paceCan: PaceDid? = application.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_CAN" }
	val pacePuk: PaceDid? = application.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_PUK" }
	val pin: PinDid? = application.dids.filterIsInstance<PinDid>().find { it.name == "PIN" }

	fun getPinStatus(): PinStatus =
		when (val status = pacePin.passwordStatus()) {
			is SecurityCommandSuccess -> PinStatus.OK
			is SecurityCommandFailure ->
				when (status.retries) {
					0 -> PinStatus.Blocked
					else -> PinStatus.Suspended
				}
		}

	fun enterPin(pinValue: String): Boolean = pacePin.enterPassword(pinValue) == null

	fun enterCan(canValue: String): Boolean = paceCan?.enterPassword(canValue) == null

	fun enterPuk(pukValue: String): Boolean = pacePuk?.enterPassword(pukValue) == null

	fun changePin(
		oldPin: String,
		newPin: String,
	): Boolean {
		val result = pacePin.enterPassword(oldPin)
		return if (result == null) {
			pin?.resetPassword(null, newPin)
			pacePin.closeChannel()
			true
		} else {
			false
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun PaceDid.enterPassword(pin: String): SecurityCommandFailure? {
		try {
			establishChannel(pin, null, null)
			return null
		} catch (ex: PaceError) {
			val secErr = ex.securityError
			if (secErr != null) {
				return secErr
			} else {
				throw ex
			}
		}
	}

	fun shutdownStack() {
		val terminals = application.device.channel.card.terminalConnection.terminal.terminals
		terminals.releaseContext()
	}

	companion object {
		fun createConnectedModel(terminal: TerminalInfo): NpaPacePinModel {
			val app = connectCard(terminal)
			return NpaPacePinModel(app)
		}

		private fun connectCard(terminal: TerminalInfo): SmartcardApplication {
			val ctx = PcscTerminalFactory.instance.load()
			ctx.establishContext()
			val sal =
				SmartcardSal(
					ctx,
					setOf(NpaCif),
					object : CardRecognition {
						override fun recognizeCard(channel: CardChannel) = NpaDefinitions.cardType
					},
					PaceFeatureSoftwareFactory(),
				)
			val session = sal.startSession()
			val connection = session.connect(terminal.terminalName)

			if (connection.deviceType != NpaCif.metadata.id) {
				throw IllegalStateException("Card is not an nPA")
			}

			return connection.applications.find { it.name == NpaDefinitions.Apps.Mf.name }
				?: throw IllegalStateException("MF application not found")
		}
	}
}
