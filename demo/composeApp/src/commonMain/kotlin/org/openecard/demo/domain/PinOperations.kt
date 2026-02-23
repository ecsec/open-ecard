package org.openecard.demo.domain

import org.openecard.demo.PinStatus
import org.openecard.demo.data.SalStackFactory
import org.openecard.demo.data.SalStackFactory.Companion.initializeNfcStack
import org.openecard.demo.viewmodel.CanEntryViewModel
import org.openecard.demo.viewmodel.PinChangeViewModel
import org.openecard.demo.viewmodel.PinMgmtViewModel
import org.openecard.demo.viewmodel.PukEntryViewModel
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.iface.feature.PaceError

class PinOperations(
	val session: SmartcardSalSession,
) {
	suspend fun connectCard(
		pinMgmtViewModel: PinMgmtViewModel,
		nfcDetected: () -> Unit,
	) {
		val terminal = session.initializeNfcStack { nfcDetected() }
		val connection = session.connect(terminal.name)
		pinMgmtViewModel.setConnection(connection)
	}

	fun getPinStatus(pacePin: PaceDid?): PinStatus =
		when (val status = pacePin?.passwordStatus()) {
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

			else -> {
				PinStatus.Unknown
			}
		}

	fun enterPinForCan(
		canEntryViewModel: CanEntryViewModel,
		pinValue: String,
	): Boolean = canEntryViewModel.pacePin?.enterPassword(pinValue) == null

	fun enterCan(
		canEntryViewModel: CanEntryViewModel,
		canValue: String,
	): Boolean = canEntryViewModel.paceCan?.enterPassword(canValue) == null

	fun enterPuk(
		pukEntryViewModel: PukEntryViewModel,
		pukValue: String,
	): Boolean = pukEntryViewModel.pacePuk?.enterPassword(pukValue) == null

	fun changePin(
		pinChangeViewModel: PinChangeViewModel,
		oldPin: String,
		newPin: String,
	): Boolean {
		val result = pinChangeViewModel.pacePin?.enterPassword(oldPin)
		return if (result == null) {
			pinChangeViewModel.pin?.resetPassword(null, newPin)
			pinChangeViewModel.pacePin?.closeChannel()
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
		session.shutdownStack()
	}
}
