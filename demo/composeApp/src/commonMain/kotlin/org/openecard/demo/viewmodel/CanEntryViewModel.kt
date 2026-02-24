package org.openecard.demo.viewmodel

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.demo.PinOperationResult
import org.openecard.demo.PinStatus
import org.openecard.demo.data.SalStackFactory
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sc.iface.TerminalFactory

private val logger = KotlinLogging.logger { }

class CanEntryViewModel(
	private val terminalFactory: TerminalFactory?,
) : PinMgmtViewModel() {
	private val _canPinUiState = MutableStateFlow(CanPinUiState())
	val canPinUiState = _canPinUiState.asStateFlow()

	var paceCan: PaceDid? = null

	fun onCanChanged(value: String) {
		_canPinUiState.update {
			it.copy(can = value, isSubmitEnabled = value.isNotBlank() && it.pin.isNotBlank())
		}
	}

	fun onPinChanged(value: String) {
		_canPinUiState.update {
			it.copy(pin = value, isSubmitEnabled = it.can.isNotBlank() && value.isNotBlank())
		}
	}

	fun validateCanPin(): String? {
		val state = _canPinUiState.value

		if (state.can.length != 6) {
			return "CAN must be 6 digits long."
		}
		if (state.pin.length !in 5..6) {
			return "PIN must be 5 to 6 digits long."
		}
		return null
	}

	suspend fun recoverWithCan(
		nfcDetected: () -> Unit,
		can: String,
		pin: String,
	): PinOperationResult =
		try {
			if (pinOps == null) {
				pinOps = terminalFactory?.let { SalStackFactory.createPinSession(it) }
			}

			val ops = pinOps
			if (ops != null) {
				ops.connectCard(this, nfcDetected)

				when (val status = ops.getPinStatus(this.pacePin)) {
					PinStatus.Suspended -> {
						if (!ops.enterCan(this, can)) {
							PinOperationResult(PinStatus.WrongCAN)
						} else if (ops.enterPinForCan(this, pin)) {
							PinOperationResult(PinStatus.OK)
						} else {
							PinOperationResult(status)
						}
					}

					else -> {
						PinOperationResult(status)
					}
				}
			} else {
				logger.error { "Could not create session" }
				PinOperationResult(null, "Could not create session")
			}
		} catch (e: Exception) {
			logger.error(e) { "PIN operation failed" }
			PinOperationResult(
				status = null,
				errorMessage = "PIN operation failed: ${e.message}",
			)
		} finally {
			pinOps?.shutdownStack()
			pinOps = null
		}

	fun setDefaults(
		can: String,
		pin: String,
	) {
		_canPinUiState.value =
			CanPinUiState(
				can = can,
				pin = pin,
				isSubmitEnabled = can.isNotBlank() && pin.isNotBlank(),
			)
	}

	fun clear() {
		_canPinUiState.value = CanPinUiState()
	}

	override fun setConnectionForOperation(mf: SmartcardApplication) {
		this.paceCan =
			mf.dids.filterIsInstance<PaceDid>().find {
				it.name == NpaDefinitions.Apps.Mf.Dids.paceCan
			} ?: throw IllegalStateException("PACE CAN DID not found")
	}
}

data class CanPinUiState(
	val can: String = "",
	val pin: String = "",
	val isSubmitEnabled: Boolean = false,
)
