package org.openecard.demo.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.EidActivation
import org.openecard.addons.tr03124.UserAgent
import org.openecard.demo.data.NpaEac
import org.openecard.demo.util.toUiItem
import org.openecard.demo.viewmodel.EacViewModel
import org.openecard.sal.iface.SalSession
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sc.iface.feature.PaceError

private val logger = KotlinLogging.logger { }

class EacOperations(
	val session: SmartcardSalSession,
) {
// 	@OptIn(ExperimentalUnsignedTypes::class)
// 	suspend fun doEac(
// 		eacViewModel: EacViewModel,
// 		tokenUrl: String,
// 		pin: String,
// 		nfcDetected: () -> Unit,
// 	): String? {
// 		return try {
// 			val clientInfo =
// 				ClientInformation(
// 					UserAgent("Open-eCard Test", UserAgent.Version(1, 0, 0)),
// 				)
//
// 			val uiStep =
// 				EidActivation.startEacProcess(
// 					clientInfo,
// 					tokenUrl,
// 					session,
// 					null,
// 				)
//
// 			session.initializeStack()
// 			session.sal.terminals
// 				.getTerminal("")
// 				?.waitForCardPresent()
// 			nfcDetected()
//
// 			val chat = uiStep.guiData.requiredChat.asBytes
//
// 			val paceResp =
// 				try {
// 					uiStep.getPaceDid("").establishChannel(
// 						pin,
// 						uiStep.guiData.requiredChat.asBytes,
// // 						chat.asBytes,
// 						uiStep.guiData.certificateDescription.asBytes,
// 					)
// 				} catch (p: PaceError) {
// 					logger.error(p) { "PACE error occurred - could not establish channel." }
// 					return "Wrong PIN or invalid card state."
// 				} catch (e: Exception) {
// 					logger.error(e) { "Could not establish channel." }
// 					return e.message
// 				}
//
// 			val serverStep = uiStep.processAuthentication(paceResp)
//
// 			when (val result = serverStep.processEidServerLogic()) {
// 				is BindingResponse.RedirectResponse -> result.redirectUrl
// 				else -> "failed result ${result.status}"
// 			}
// 		} catch (e: Exception) {
// 			logger.error(e) { "Some error occurred." }
// 			e.message
// 		}
// 	}

	@OptIn(ExperimentalUnsignedTypes::class)
	suspend fun doEac(
		eacViewModel: EacViewModel,
		pin: String,
		nfcDetected: () -> Unit,
	): String? {
		val ops = eacViewModel.eacOps ?: return null
		val chat = eacViewModel.userSelectedChat ?: return null
		val step = eacViewModel.uiStep ?: return null

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

	fun shutdownStack() {
		session.shutdownStack()
	}
}
