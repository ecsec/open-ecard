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
import org.openecard.gui.StepResult
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.i18n.I18N
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
					performPACEWithCAN(oldResults, state)?. let {
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

// 		val pinState: PinState = ctx.get(EACProtocol.Companion.PIN_STATUS) as PinState
// 		var conHandle = this.ctx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType
// 		try {
// 			val ph = PaceCardHelper(addonCtx, conHandle)
// 			conHandle = ph.connectCardIfNeeded(setOf(ECardConstants.NPA_CARD_TYPE))
// 			this.ctx.put(TR03112Keys.CONNECTION_HANDLE, conHandle)
// 			val currentState = ph.pinStatus
//
// 			pinState.update(currentState)
// 		} catch (ex: WSHelper.WSException) {
// 			if (SysUtils.isMobileDevice &&
// 				minorIsOneOf<WSHelper.WSException>(
// 					ex,
// 					ECardConstants.Minor.IFD.Terminal.PREPARE_DEVICES_ERROR,
// 					ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
// 					ECardConstants.Minor.IFD.Terminal.WAIT_FOR_DEVICE_TIMEOUT,
// 				)
// 			) {
// 				// repeat the step
// 				return StepActionResult(StepActionResultStatus.REPEAT)
// 			}
// 			logger.error { "An unknown error occured while trying to verify the PIN." }
// 			return StepActionResult(
// 				StepActionResultStatus.REPEAT,
// 				ErrorStep(
// 					I18N.strings.pinplugin_action_error_title.localized(),
// 					I18N.strings.pinplugin_action_error_unknown.localized(),
// 					ex,
// 				),
// 			)
// 		} catch (ex: InterruptedException) {
// 			logger.warn(ex) { "PIN step action interrupted." }
// 			return StepActionResult(StepActionResultStatus.CANCEL)
// 		}
// 		if (pinState.isRequestCan) {
// 			try {
// 				val response = performPACEWithCAN(oldResults, conHandle)
// 				if (response == null) {
// 					logger.warn { "The CAN does not meet the format requirements." }
// 					step.setStatus(PacePinStatus.RC1)
// 					return StepActionResult(StepActionResultStatus.REPEAT)
// 				}
//
// 				if (response.getResult().getResultMajor() == ECardConstants.Major.ERROR) {
// 					if (response.getResult().getResultMinor() == ECardConstants.Minor.IFD.AUTHENTICATION_FAILED) {
// 						logger.error { "Failed to authenticate with the given CAN." }
// 						step.setStatus(PacePinStatus.RC1)
// 						return StepActionResult(StepActionResultStatus.REPEAT)
// 					} else {
// 						checkResult<EstablishChannelResponse>(response)
// 					}
// 				}
// 			} catch (ex: WSHelper.WSException) {
// 				// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
// 				if (ex.resultMinor == ECardConstants.Minor.IFD.CANCELLATION_BY_USER) {
// 					logger.error(ex) { "User canceled the authentication manually." }
// 					return StepActionResult(StepActionResultStatus.CANCEL)
// 				}
//
// 				// for people which think they have to remove the card in the process
// 				if (ex.resultMinor == ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE) {
// 					logger.error(
// 						ex,
// 					) { "The SlotHandle was invalid so probably the user removed the card or an reset occurred." }
// 					return StepActionResult(
// 						StepActionResultStatus.REPEAT,
// 						ErrorStep(
// 							I18N.strings.pinplugin_action_error_title.localized(),
// 							I18N.strings.pinplugin_action_error_card_removed.localized(),
// 							ex,
// 						),
// 					)
// 				}
// 			} catch (ex: InterruptedException) {
// 				logger.warn(ex) { "PIN+CAN step action interrupted." }
// 				return StepActionResult(StepActionResultStatus.CANCEL)
// 			} catch (ex: CanLengthInvalidException) {
// 				step.ensureCanData()
// 				logger.warn { "Can did  not contain 6 digits." }
// 				return StepActionResult(StepActionResultStatus.REPEAT)
// 			}
// 		}
//
// 		try {
// 			val establishChannelResponse = performPACEWithPIN(oldResults, conHandle)
//
// 			if (establishChannelResponse.getResult().getResultMajor() == ECardConstants.Major.ERROR) {
// 				if (establishChannelResponse.getResult().getResultMinor() == ECardConstants.Minor.IFD.PASSWORD_ERROR) {
// 					// update step display
// 					logger.info { "Wrong PIN entered, trying again (try number $retryCounter)." }
// 					this.step.setStatus(PacePinStatus.RC2)
// 					// repeat the step
// 					return StepActionResult(StepActionResultStatus.REPEAT)
// 				} else if (establishChannelResponse
// 						.getResult()
// 						.getResultMinor() == ECardConstants.Minor.IFD.PASSWORD_SUSPENDED
// 				) {
// 					// update step display
// 					step.setStatus(PacePinStatus.RC1)
// 					logger.info { "Wrong PIN entered, trying again (try number $retryCounter)." }
// 					// repeat the step
// 					return StepActionResult(StepActionResultStatus.REPEAT)
// 				} else if (establishChannelResponse
// 						.getResult()
// 						.getResultMinor() == ECardConstants.Minor.IFD.PASSWORD_BLOCKED
// 				) {
// 					logger.warn { "Wrong PIN entered. The PIN is blocked." }
// 					pinState.update(PacePinStatus.BLOCKED)
// 					return StepActionResult(
// 						StepActionResultStatus.REPEAT,
// 						ErrorStep(
// 							I18N.strings.pace_step_error_title_blocked
// 								.format(pin)
// 								.localized(),
// 							I18N.strings.pace_step_error_pin_blocked
// 								.format(pin, pin, puk, pin)
// 								.localized(),
// 							createException(establishChannelResponse.getResult()),
// 						),
// 					)
// 				} else {
// 					checkResult<EstablishChannelResponse>(establishChannelResponse)
// 				}
// 			}
//
// 			eacData.paceResponse = establishChannelResponse
// 			// PACE completed successfully, proceed with next step
// 			ctx.put(EACProtocol.Companion.PACE_EXCEPTION, null)
// 			return StepActionResult(StepActionResultStatus.NEXT)
// 		} catch (ex: WSHelper.WSException) {
// 			// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
// 			if (ex.resultMinor == ECardConstants.Minor.IFD.CANCELLATION_BY_USER) {
// 				logger.error(ex) { "User canceled the authentication manually." }
// 				return StepActionResult(StepActionResultStatus.CANCEL)
// 			}
//
// 			// for people which think they have to remove the card in the process
// 			if (ex.resultMinor == ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE) {
// 				logger.error(ex) { "The SlotHandle was invalid so probably the user removed the card or an reset occurred." }
// 				return StepActionResult(
// 					StepActionResultStatus.REPEAT,
// 					ErrorStep(
// 						I18N.strings.pinplugin_action_error_title.localized(),
// 						I18N.strings.pinplugin_action_error_card_removed.localized(),
// 						ex,
// 					),
// 				)
// 			}
//
// 			// repeat the step
// 			logger.error { "An unknown error occured while trying to verify the PIN." }
// 			return StepActionResult(
// 				StepActionResultStatus.REPEAT,
// 				ErrorStep(
// 					I18N.strings.pinplugin_action_error_title.localized(),
// 					I18N.strings.pinplugin_action_error_unknown.localized(),
// 					ex,
// 				),
// 			)
// 		} catch (ex: InterruptedException) {
// 			logger.warn(ex) { "PIN step action interrupted." }
// 			return StepActionResult(StepActionResultStatus.CANCEL)
// 		} catch (ex: PinOrCanEmptyException) {
// 			logger.warn(ex) { "PIN was empty" }
// 			return StepActionResult(StepActionResultStatus.REPEAT)
// 		}
	}
}
