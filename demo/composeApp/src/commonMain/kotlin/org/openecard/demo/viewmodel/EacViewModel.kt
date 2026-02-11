package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.EidActivation
import org.openecard.addons.tr03124.UserAgent
import org.openecard.addons.tr03124.eac.UiStep
import org.openecard.demo.data.NpaEac
import org.openecard.demo.domain.EacOperations
import org.openecard.demo.util.ChatAttributeUi
import org.openecard.demo.util.buildChatFromSelection
import org.openecard.demo.util.toUiItem
import org.openecard.sc.iface.TerminalFactory
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

	private var serverRequestedChat: AuthenticationTerminalChat? = null
	var userSelectedChat: AuthenticationTerminalChat? = null

	// 	 state objects to avoid initializing twice
	var eacOps: EacOperations? = null
	var uiStep: UiStep? = null

	// called when user toggles checkboxes
	fun updateChatSelection(newList: List<ChatAttributeUi>) {
		_chatItems.value = newList
	}

	// convert
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
			eacOps = terminalFactory?.let { NpaEac.createEacSession(it) } ?: return false
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

		if (uiStep != null) {
			val chat = uiStep!!.guiData.requiredChat
			serverRequestedChat = chat

			// convert
			_chatItems.value = chat.toUiItem()

			return true
		} else {
			return false
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	suspend fun doEac(
		pin: String,
		nfcDetected: () -> Unit,
	): String? {
		val ops = eacOps ?: return null
		val chat = userSelectedChat ?: return null
		val step = uiStep ?: return null

		ops.session.initializeStack()
		ops.session.sal.terminals
			.getTerminal("")
			?.waitForCardPresent()

		nfcDetected()

		val paceResp =
			step.getPaceDid("").establishChannel(
				pin,
				chat.asBytes,
				step.guiData.certificateDescription.asBytes,
			)

		val serverStep = step.processAuthentication(paceResp)

		return when (val result = serverStep.processEidServerLogic()) {
			is BindingResponse.RedirectResponse -> result.redirectUrl
			else -> "failed result ${result.status}"
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
		} catch (e: Exception) {
			logger.error(e) { "EAC operation failed." }
			e.message
		} finally {
			ops.shutdownStack()
		}
	}

	fun setDefaults() {
		_eacUiState.value =
			EacUiState(
				pin = "123123",
				isSubmitEnabled = true,
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
