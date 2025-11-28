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

import dev.icerock.moko.resources.format
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.i18n.I18N
import org.openecard.richclient.processui.definition.PasswordField
import org.openecard.richclient.processui.executor.ExecutionResults
import org.openecard.richclient.processui.executor.StepAction
import org.openecard.richclient.processui.executor.StepActionResult
import org.openecard.richclient.processui.executor.StepActionResultStatus
import org.openecard.richclient.tr03124.EacProcessState
import org.openecard.richclient.tr03124.TerminalSelection.waitForNpa
import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.PasswordError
import org.openecard.sal.iface.RemovedDevice
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sc.iface.feature.PaceError

private val log = KotlinLogging.logger { }

private val pin: String = I18N.strings.pace_pin.localized()
private val puk: String = I18N.strings.pace_puk.localized()

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
	private fun Throwable.handleCardExceptions(isSuspendRecovery: Boolean = false): StepActionResult {
		// end any open transaction before continuing
		state.paceDid?.application?.device?.let {
			if (it.isExclusive) {
				runCatching { it.endExclusive() }
			}
		}

		val result =
			when (this) {
				is PaceError -> {
					log.info(this.takeIf { log.isDebugEnabled() }) { "PACE error: ${this.message}" }
					securityError?.let { secErr ->
						// only update when not in suspend resumption
						if (!isSuspendRecovery) {
							state.status = secErr
							step.updateStatus()
						}
						if (secErr.verificationFailed) {
							StepActionResult(StepActionResultStatus.REPEAT)
						} else {
							// display error step with appropriate error message
							val errStep =
								if (secErr.authBlocked) {
									ErrorStep(
										I18N.strings.pace_step_error_title_blocked
											.format(pin)
											.localized(),
										I18N.strings.pace_step_error_pin_blocked
											.format(pin, pin, puk, pin)
											.localized(),
									)
								} else if (secErr.authDeactivated) {
									ErrorStep(
										I18N.strings.pace_step_error_title_deactivated.localized(),
										I18N.strings.pace_step_error_pin_deactivated.localized(),
									)
								} else {
									ErrorStep(
										I18N.strings.pinplugin_action_error_title.localized(),
										I18N.strings.pinplugin_action_error_unknown.localized(),
									)
								}
							StepActionResult(StepActionResultStatus.REPEAT, errStep)
						}
					} ?: StepActionResult(StepActionResultStatus.CANCEL)
				}

				is RemovedDevice,
				is DeviceUnavailable,
				-> {
					log.info(this.takeIf { log.isDebugEnabled() }) { "Device removed during PACE processing: ${this.message}" }
					state.paceDid = null
					StepActionResult(
						StepActionResultStatus.REPEAT,
						ErrorStep(
							I18N.strings.pinplugin_action_error_title.localized(),
							I18N.strings.pinplugin_action_error_card_removed.localized(),
						),
					)
				}

				is PasswordError -> {
					log.info { "Password error in PACE: ${this.message}" }
					StepActionResult(StepActionResultStatus.REPEAT)
				}

				else -> {
					log.info(this.takeIf { log.isDebugEnabled() }) { "Exception triggered cancel: ${this.message}" }
					StepActionResult(StepActionResultStatus.CANCEL)
				}
			}

		return result
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	protected suspend fun performPACEWithPIN(
		oldResults: Map<String, ExecutionResults>,
		state: EacProcessState,
	): StepActionResult =
		runCatching {
			val pace = state.waitForNpa()
			// start transaction before continuing with the logic
			pace.application.device.let {
				if (!it.isExclusive) {
					it.beginExclusive()
				}
			}
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
								it.id == PINStep.PIN_FIELD
							}?.value
							?: throw PinOrCanEmptyException("No PIN value specified")

					pace.establishChannel(
						pinValue.concatToString(),
						state.selectedChat.asBytes,
						state.uiStep.guiData.certificateDescription.asBytes,
					)
				}

			state.paceResponse = result

			StepActionResult(StepActionResultStatus.NEXT)
		}.recover { it.handleCardExceptions() }.getOrThrow()

	@OptIn(ExperimentalUnsignedTypes::class)
	protected suspend fun performPACEWithCAN(
		oldResults: Map<String, ExecutionResults>,
		state: EacProcessState,
	): StepActionResult? =
		runCatching {
			val pacePin = state.waitForNpa()
			// start transaction before continuing with the logic
			pacePin.application.device.let {
				if (!it.isExclusive) {
					it.beginExclusive()
				}
			}

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
							it.id == PINStep.CAN_FIELD
						}?.value
						?: throw PinOrCanEmptyException("No CAN value specified")

				paceCan.establishChannel(
					canValue.concatToString(),
					state.selectedChat.asBytes,
					state.uiStep.guiData.certificateDescription.asBytes,
				)
			}
			// no exception means pass
			null
		}.recover { it.handleCardExceptions(true) }.getOrThrow()
}
