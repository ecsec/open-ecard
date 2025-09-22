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
import org.openecard.addons.tr03124.BindingException
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.i18n.I18N
import org.openecard.richclient.tr03124.EacProcessState

/**
 * Action waiting for the EAC process to finish.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
private val logger = KotlinLogging.logger { }

class ProcessingStepAction(
	step: Step,
	val state: EacProcessState,
) : StepAction(step) {
	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		return runBlocking {
			try {
				val paceResult =
					state.paceResponse ?: run {
						val errorStep =
							ErrorStep(
								I18N.strings.tr03112_int_error.localized(),
								I18N.strings.tr03112_error_internal.localized(),
							)
						return@runBlocking StepActionResult(StepActionResultStatus.REPEAT, errorStep)
					}

				val serverStep = state.uiStep.processAuthentication(paceResult)
				state.bindingResponse = serverStep.processEidServerLogic()

				StepActionResult(StepActionResultStatus.NEXT)
			} catch (ex: BindingException) {
				state.bindingResponse = ex.toResponse()

				StepActionResult(
					StepActionResultStatus.REPEAT,
					ErrorStep(
						I18N.strings.tr03112_err_header.localized(),
						I18N.strings.tr03112_authentication_failed.localized(),
					),
				)
			}
		}

// 		val pAuthDone = ctx.getPromise(EACProtocol.AUTHENTICATION_DONE)
// 		try {
// 			pAuthDone.deref(120, TimeUnit.SECONDS)
// 			return StepActionResult(StepActionResultStatus.NEXT)
// 		} catch (ex: InterruptedException) {
// 			logger.error(ex) { "ProcessingStepAction interrupted by the user or an other thread." }
// 			ctx.put(
// 				EACProtocol.PACE_EXCEPTION,
// 				createException(
// 					makeResultError(
// 						ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
// 						"User canceled the EAC dialog.",
// 					),
// 				),
// 			)
// 			return StepActionResult(StepActionResultStatus.CANCEL)
// 		} catch (ex: TimeoutException) {
// 			logger.info(ex) { "Timeout while waiting for the authentication to finish." }
// 			ctx.put(
// 				EACProtocol.PACE_EXCEPTION,
// 				createException(
// 					makeResultError(
// 						ECardConstants.Minor.Disp.TIMEOUT,
// 						"Timeout during EAC process.",
// 					),
// 				),
// 			)
// 			return StepActionResult(StepActionResultStatus.CANCEL)
// 		}
	}
}
