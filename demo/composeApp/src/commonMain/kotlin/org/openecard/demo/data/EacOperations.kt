package org.openecard.demo.data

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.EidActivation
import org.openecard.addons.tr03124.UserAgent
import org.openecard.demo.model.NpaEacModel
import org.openecard.sc.iface.feature.PaceError

class EacOperations(
	private val model: NpaEacModel,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	suspend fun doEac(
		tokenUrl: String,
		pin: String
	): String? {
		return try {
			val clientInfo = ClientInformation(
				UserAgent("Open-eCard Test", UserAgent.Version(1, 0, 0))
			)

			val uiStep = EidActivation.startEacProcess(
				clientInfo,
				tokenUrl,
				model.session,
				model.terminalName
			)

			val paceResp =
				try {
					uiStep.getPaceDid().establishChannel(
						pin,
						uiStep.guiData.requiredChat.asBytes,
						uiStep.guiData.certificateDescription.asBytes
					)
				} catch (p: PaceError) {
					return "Wrong PIN"
				} catch (e: Exception) {
					return e.message
				}

			val serverStep = uiStep.processAuthentication(paceResp)

			when (val result = serverStep.processEidServerLogic()) {
				is BindingResponse.RedirectResponse -> result.redirectUrl
				else -> "failed result ${result.status}"
			}
		} catch (e: Exception) {
			e.message
		}
	}
}
