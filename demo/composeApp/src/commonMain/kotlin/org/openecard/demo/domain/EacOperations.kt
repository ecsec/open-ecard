package org.openecard.demo.domain

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.eac.UiStep
import org.openecard.demo.data.SalStackFactory.Companion.initializeNfcStack
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sc.pace.cvc.AuthenticationTerminalChat

class EacOperations(
	val session: SmartcardSalSession,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	suspend fun doEac(
		userSelectedChat: AuthenticationTerminalChat,
		uiStep: UiStep,
		pin: String,
		nfcDetected: () -> Unit,
	): String? {
		val terminal = session.initializeNfcStack { nfcDetected() }

		val paceResp =
			uiStep.getPaceDid(terminal.name).establishChannel(
				pin,
				userSelectedChat.asBytes,
				uiStep.guiData.certificateDescription.asBytes,
			)

		val serverStep = uiStep.processAuthentication(paceResp)

		return when (val result = serverStep.processEidServerLogic()) {
			is BindingResponse.RedirectResponse -> result.redirectUrl
			else -> "failed result ${result.status}"
		}
	}
}
