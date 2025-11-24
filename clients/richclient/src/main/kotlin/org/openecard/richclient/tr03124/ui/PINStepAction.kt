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

package org.openecard.richclient.tr03124.ui

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.openecard.i18n.I18N
import org.openecard.richclient.processui.StepResult
import org.openecard.richclient.processui.executor.ExecutionResults
import org.openecard.richclient.processui.executor.StepActionResult
import org.openecard.richclient.processui.executor.StepActionResultStatus
import org.openecard.richclient.tr03124.EacProcessState
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.utils.common.cast

private val logger = KotlinLogging.logger { }

private val pin: String = I18N.strings.pace_pin.localized()
private val puk: String = I18N.strings.pace_puk.localized()

/**
 * StepAction for capturing the user PIN on the EAC GUI.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class PINStepAction(
	step: PINStep,
	state: EacProcessState,
) : AbstractPasswordStepAction(step, state) {
	private val retryCounter = 0

	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		return runBlocking {
			try {
				if (state.status?.cast<SecurityCommandFailure>()?.retries == 1) {
					// we need to enter the can first
					performPACEWithCAN(oldResults, state)?.let {
						// if this yields a result, handle it in the gui executor
						return@runBlocking it
					}
				}
				// now enter the PIN
				val resp = performPACEWithPIN(oldResults, state)
				resp
			} catch (ex: PinOrCanEmptyException) {
				// no pin was captured, but one was needed. correct UI and try again
				val newPinStep = PINStep(state)
				newPinStep.action = CANStepAction(newPinStep, state)
				StepActionResult(StepActionResultStatus.REPEAT, newPinStep)
			}
		}
	}
}
