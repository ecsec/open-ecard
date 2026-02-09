package org.openecard.demo.domain

import org.openecard.demo.PinStatus
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.iface.feature.PaceError

class PinOperations(
	application: SmartcardApplication,
) {
	val pacePin: PaceDid =
		application.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_PIN" }
			?: throw IllegalStateException("PACE PIN not found")

	val paceCan: PaceDid? = application.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_CAN" }
	val pacePuk: PaceDid? = application.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_PUK" }
	val pin: PinDid? = application.dids.filterIsInstance<PinDid>().find { it.name == "PIN" }

	fun getPinStatus(): PinStatus =
		when (val status = pacePin.passwordStatus()) {
			is SecurityCommandSuccess -> {
				PinStatus.OK
			}

			is SecurityCommandFailure -> {
				when (status.retries) {
					3 -> PinStatus.OK
					2 -> PinStatus.Retry
					1 -> PinStatus.Suspended
					0 -> PinStatus.Blocked
					else -> PinStatus.Unknown
				}
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
}
