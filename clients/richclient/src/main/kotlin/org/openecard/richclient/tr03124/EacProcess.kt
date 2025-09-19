/****************************************************************************
 * Copyright (C) 2025 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.richclient.tr03124

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.EidActivation
import org.openecard.addons.tr03124.eac.UiStep
import org.openecard.cif.bundled.NpaCif
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.i18n.I18N
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.tr03124.TerminalSelection.trySelectPinPadTerminal
import org.openecard.richclient.tr03124.ui.CHATStep
import org.openecard.richclient.tr03124.ui.CHATStepAction
import org.openecard.richclient.tr03124.ui.CVCStep
import org.openecard.richclient.tr03124.ui.CVCStepAction
import org.openecard.richclient.tr03124.ui.PINStep
import org.openecard.richclient.tr03124.ui.ProcessingStep
import org.openecard.richclient.tr03124.ui.ProcessingStepAction
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.Terminals
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class EacProcess(
	val terminals: Terminals,
	val cardRecognition: CardRecognition,
	val paceFactory: PaceFeatureSoftwareFactory,
	val clientInfo: ClientInformation,
	val cardWatcher: CardWatcher,
	val gui: UserConsent,
	val scope: CoroutineScope,
) {
	@Throws(BindingException::class)
	suspend fun start(tcTokenUrl: String): BindingResponse {
		val sal = SmartcardSal(terminals, setOf(NpaCif), cardRecognition, paceFactory)
		val session = sal.startSession()

		val uiStep =
			EidActivation.startEacProcess(
				clientInfo = clientInfo,
				tokenUrl = tcTokenUrl,
				session = session,
				terminalName = null,
			)

		return processUi(uiStep)
	}

	@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
	@Throws(BindingException::class)
	private suspend fun processUi(uiStep: UiStep): BindingResponse {
		// state variables
		val state = EacProcessState(terminals, cardWatcher, uiStep)

		val result =
			CoroutineScope(newSingleThreadContext("EAC-GUI")).launch {
				state.trySelectPinPadTerminal()

				val uc = UserConsentDescription(I18N.strings.eac_user_consent_title.localized())
				uc.dialogType = "EAC"

				// create GUI and init executor
				val cvcStep = CVCStep(uiStep.guiData)
				val cvcStepAction = CVCStepAction(cvcStep)
				cvcStep.action = cvcStepAction
				uc.steps.add(cvcStep)

				val selectedChat = state.selectedChat
				val chatStep = CHATStep(state)
				val chatAction = CHATStepAction(chatStep, state)
				chatStep.action = chatAction
				uc.steps.add(chatStep)

				uc.steps.add(PINStep.createDummy(uiStep.guiData.pinType))

				val procStep = ProcessingStep()
				val procStepAction = ProcessingStepAction(procStep, state)
				procStep.action = procStepAction
				uc.steps.add(procStep)

				val nav = gui.obtainNavigator(uc)
				val exec = ExecutionEngine(nav)
				val uiResult = exec.process()

				// TODO: provide a better bindingResponse based on exceptions in the process

				if (state.bindingResponse == null) {
					state.bindingResponse = state.uiStep.cancel()
				}
			}

		result.join()
		return state.bindingResponse
			?: BindingResponse.ReferencedContentResponse(
				HttpStatusCode.InternalServerError.value,
				BindingResponse.ContentCode.INTERNAL_ERROR,
			)
	}
}
