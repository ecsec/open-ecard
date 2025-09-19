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
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.richclient.tr03124.EacProcessState
import org.openecard.richclient.tr03124.TerminalSelection.waitForNpa
import org.openecard.sal.iface.dids.PaceDid

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
abstract class AbstractPasswordStepAction(
	protected val step: PINStep,
	val state: EacProcessState,
) : StepAction(
		step,
	) {
	@OptIn(ExperimentalUnsignedTypes::class)
	protected suspend fun performPACEWithPIN(
		oldResults: Map<String, ExecutionResults>,
		state: EacProcessState,
	): StepActionResult {
		try {
			val pace = state.waitForNpa()

			val result =
				if (state.nativePace) {
					pace.establishChannel(
						state.selectedChat.asBytes,
						state.uiStep.guiData.certificateDescription.asBytes,
					)
				} else {
					val pinValue: CharArray =
						oldResults[stepID]
							?.results
							?.filterIsInstance<PasswordField>()
							?.firstOrNull {
								it.id ==
									PINStep.PIN_FIELD
							}?.value
							?: throw PinOrCanEmptyException("No PIN value specified")

					pace.establishChannel(
						pinValue.concatToString(),
						state.selectedChat.asBytes,
						state.uiStep.guiData.certificateDescription.asBytes,
					)
				}

			state.paceResponse = result

			return StepActionResult(StepActionResultStatus.NEXT)
		} catch (ex: Exception) {
			TODO("handle exceptions and failures")
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	protected suspend fun performPACEWithCAN(
		oldResults: Map<String, ExecutionResults>,
		state: EacProcessState,
	): StepActionResult? {
		try {
			val pacePin = state.waitForNpa()
			val paceCan =
				pacePin.application.dids
					.filterIsInstance<PaceDid>()
					.find { it.name == NpaDefinitions.Apps.Mf.Dids.paceCan }
					?: throw IllegalStateException("nPA CIF contains no CAN DID")
			
			if (state.nativePace) {
				paceCan.establishChannel(
					state.selectedChat.asBytes,
					state.uiStep.guiData.certificateDescription.asBytes,
				)
			} else {
				val canValue: CharArray =
					oldResults[stepID]
						?.results
						?.filterIsInstance<PasswordField>()
						?.firstOrNull {
							it.id ==
								PINStep.CAN_FIELD
						}?.value
						?: throw PinOrCanEmptyException("No CAN value specified")

				paceCan.establishChannel(
					canValue.contentToString(),
					state.selectedChat.asBytes,
					state.uiStep.guiData.certificateDescription.asBytes,
				)
			}
			// no exception means pass
			return null
		} catch (ex: Exception) {
			// TODO: handle exceptions and failures
			return StepActionResult(StepActionResultStatus.REPEAT)
		}
	}
}
