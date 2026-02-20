package org.openecard.demo.viewmodel

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.demo.PinOperationResult
import org.openecard.demo.PinStatus
import org.openecard.demo.data.Session
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sc.iface.TerminalFactory

private val logger = KotlinLogging.logger { }

class PukEntryViewModel(
	private val terminalFactory: TerminalFactory?,
) : PinMgmtViewModel() {
	private val _pukUiState = MutableStateFlow(PukUiState())
	val pukUiState = _pukUiState.asStateFlow()

	var pacePuk: PaceDid? = null

	fun onPukChanged(value: String) {
		_pukUiState.update {
			it.copy(puk = value, isSubmitEnabled = value.isNotBlank())
		}
	}

	fun validatePuk(): String? {
		val state = _pukUiState.value

		if (state.puk.length != 10) {
			return "PUK must be 10 digits long."
		}
		return null
	}

	suspend fun unblockPin(
		nfcDetected: () -> Unit,
		puk: String,
	): PinOperationResult =
		try {
			if (pinOps == null) {
				pinOps = terminalFactory?.let { Session.createPinSession(it) }
			}

			val ops = pinOps
			if (ops != null) {
				ops.connectCard(this, nfcDetected)

				when (val status = ops.getPinStatus(this.pacePin)) {
					PinStatus.Blocked -> {
						if (ops.enterPuk(this, puk)) {
							PinOperationResult(PinStatus.OK)
						} else {
							PinOperationResult(PinStatus.WrongPUK)
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

	fun setDefaults(puk: String) {
		_pukUiState.value =
			PukUiState(
				puk = puk,
				isSubmitEnabled = puk.isNotBlank(),
			)
	}

	fun clear() {
		_pukUiState.value = PukUiState()
	}

	override fun setConnectionForOperation(mf: SmartcardApplication) {
		this.pacePuk =
			mf.dids.filterIsInstance<PaceDid>().find {
				it.name == NpaDefinitions.Apps.Mf.Dids.pacePuk
			} ?: throw IllegalStateException("PACE PUK DID not found")
	}
}

data class PukUiState(
	val puk: String = "",
	val isSubmitEnabled: Boolean = false,
	val errorMessage: String? = null,
)
