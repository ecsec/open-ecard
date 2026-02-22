package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.EidActivation
import org.openecard.addons.tr03124.UserAgent
import org.openecard.addons.tr03124.eac.UiStep
import org.openecard.demo.data.SalStackFactory
import org.openecard.demo.domain.EacOperations
import org.openecard.demo.util.ChatAttributeUi
import org.openecard.demo.util.buildChatFromSelection
import org.openecard.demo.util.chatToUi
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.pace.cvc.AuthenticationTerminalChat

private val logger = KotlinLogging.logger { }

class EacViewModel(
	private val terminalFactory: TerminalFactory?,
) : ViewModel() {
	private val _eacUiState = MutableStateFlow(EacUiState())
	val eacUiState = _eacUiState.asStateFlow()

	fun onPinChanged(value: String) {
		_eacUiState.update {
			it.copy(
				pin = value,
				isSubmitEnabled = value.isNotBlank(),
			)
		}
	}

	fun validatePin(): String? {
		val s = eacUiState.value

		if (s.pin.length !in 5..6) {
			return "PIN must be 5 to 6 digits long."
		}
		return null
	}

	private val _chatItems = MutableStateFlow<List<ChatAttributeUi>>(emptyList())
	val chatItems = _chatItems.asStateFlow()
	private var serverRequiredChat: AuthenticationTerminalChat? = null
	private var serverRequestedChat: AuthenticationTerminalChat? = null
	var userSelectedChat: AuthenticationTerminalChat? = null
	var eacOps: EacOperations? = null
	var uiStep: UiStep? = null

	private val _uiMode = MutableStateFlow(Config())
	val uiMode = _uiMode.asStateFlow()

	fun updateConfig(mode: Config) {
		_uiMode.value = mode

		if (!mode.requiredChatEnabled) {
			_chatItems.update { items ->
				items.map { item ->
					if (item.required) {
						item.copy(selected = true)
					} else {
						item
					}
				}
			}
		}
	}

	fun updateChatSelection(newList: List<ChatAttributeUi>) {
		_chatItems.value = newList
	}

	fun confirmChatSelection() {
		val base = serverRequestedChat ?: return

		val selectedIds =
			_chatItems.value
				.filter { it.selected }
				.map { it.id }
				.toList()

		userSelectedChat = buildChatFromSelection(base, selectedIds)
	}

	suspend fun setChatItems(tokenUrl: String): Boolean {
		if (eacOps == null) {
			eacOps = terminalFactory?.let { SalStackFactory.createEacSession(it) } ?: return false
		}

		val ops = eacOps

		val clientInfo =
			ClientInformation(
				UserAgent("Open-eCard Test", UserAgent.Version(1, 0, 0)),
			)

		if (ops != null) {
			uiStep =
				EidActivation.startEacProcess(
					clientInfo,
					tokenUrl,
					ops.session,
					null,
				)
		}

		when (val step = uiStep) {
			null -> {
				return false
			}

			else -> {
				val requiredChat = step.guiData.requiredChat
				val optionalChat = step.guiData.optionalChat

				serverRequiredChat = requiredChat
				serverRequestedChat = optionalChat

				_chatItems.value =
					chatToUi(
						requiredChat,
						optionalChat,
					)

				return true
			}
		}
	}

	suspend fun startEacProcess(
		nfcDetected: () -> Unit,
		pin: String,
	): String? {
		val ops = eacOps ?: return null

		return try {
			ops.doEac(
				this,
				pin,
				nfcDetected,
			)
		} catch (p: PaceError) {
			logger.error(p) { "PACE error occurred." }
			return "Wrong PIN or invalid card state."
		} catch (e: Exception) {
			logger.error(e) { "EAC process failed." }
			e.message
		} finally {
			ops.session.shutdownStack()
		}
	}

	fun setDefaults(pin: String) {
		_eacUiState.value =
			EacUiState(
				pin = pin,
				isSubmitEnabled = pin.isNotBlank(),
			)
	}

	fun clear() {
		_eacUiState.value = EacUiState()
	}
}

data class EacUiState(
	val pin: String = "",
	val isSubmitEnabled: Boolean = false,
)

data class Config(
	val requiredChatEnabled: Boolean = false,
)
