package org.openecard.demo.domain

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.demo.data.SalStackFactory
import org.openecard.demo.viewmodel.EacViewModel
import org.openecard.sal.sc.SmartcardSalSession

class EacOperations(
	val session: SmartcardSalSession,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	suspend fun doEac(
		eacViewModel: EacViewModel,
		pin: String,
		nfcDetected: () -> Unit,
	): String? {
		val chat = eacViewModel.userSelectedChat ?: return null
		val step = eacViewModel.uiStep ?: return null

		SalStackFactory.initializeNfcStack(session, nfcDetected)

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
}
