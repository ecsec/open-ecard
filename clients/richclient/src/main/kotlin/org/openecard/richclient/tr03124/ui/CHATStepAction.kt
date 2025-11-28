/*
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
 */

package org.openecard.richclient.tr03124.ui

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.richclient.processui.StepResult
import org.openecard.richclient.processui.definition.Checkbox
import org.openecard.richclient.processui.definition.Step
import org.openecard.richclient.processui.executor.ExecutionResults
import org.openecard.richclient.processui.executor.StepAction
import org.openecard.richclient.processui.executor.StepActionResult
import org.openecard.richclient.processui.executor.StepActionResultStatus
import org.openecard.richclient.tr03124.EacProcessState
import org.openecard.sc.pace.cvc.ReadAccess
import org.openecard.sc.pace.cvc.SpecialFunction
import org.openecard.sc.pace.cvc.WriteAccess
import org.openecard.utils.common.cast

private val logger = KotlinLogging.logger { }

/**
 * StepAction for evaluation of CHAT value items on the EAC GUI.
 *
 * @author Tobias Wich
 */
class CHATStepAction(
	step: Step,
	private val state: EacProcessState,
) : StepAction(step) {
	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		if (result.isOK()) {
			processResult(oldResults)

			val nextStep = PINStep.buildPinStep(state)
			return StepActionResult(StepActionResultStatus.NEXT, nextStep)
		} else {
			// cancel can not happen, so only back is left to be handled
			return StepActionResult(StepActionResultStatus.BACK)
		}
	}

	private fun processResult(results: Map<String, ExecutionResults>) {
		val executionResults: ExecutionResults = results[stepID]!!
		val selectedCHAT = state.selectedChat

		// process read access and special functions
		executionResults.getResult(CHATStep.READ_CHAT_BOXES)?.cast<Checkbox>()?.let { cbRead ->
			for (item in cbRead.boxItems) {
				val itemName = item.name
				if (specialFunctionNames.contains(itemName)) {
					val itemType = SpecialFunction.valueOf(item.name)
					selectedCHAT.specialFunctions[itemType] = item.isChecked
				} else {
					val itemType = ReadAccess.valueOf(item.name)
					selectedCHAT.readAccess[itemType] = item.isChecked
				}
			}
		}

		// process write access
		executionResults.getResult(CHATStep.WRITE_CHAT_BOXES)?.cast<Checkbox>()?.let { cbWrite ->
			for (item in cbWrite.boxItems) {
				val itemType = WriteAccess.valueOf(item.name)
				selectedCHAT.writeAccess[itemType] = item.isChecked
			}
		}
	}

	companion object {
		/**
		 * Returns a list containing the names of all special functions.
		 * @return list containing the names of all special functions.
		 */
		private val specialFunctionNames: Set<String> by lazy {
			SpecialFunction.entries
				.map { it.name }
				.toSet()
		}
	}
}
