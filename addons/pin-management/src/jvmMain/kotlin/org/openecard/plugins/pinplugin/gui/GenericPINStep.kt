/****************************************************************************
 * Copyright (C) 2014-2025 ecsec GmbH.
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
package org.openecard.plugins.pinplugin.gui

import dev.icerock.moko.resources.format
import org.openecard.common.DynamicContext
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.i18n.I18N
import org.openecard.plugins.pinplugin.CardCapturer
import org.openecard.plugins.pinplugin.CardStateView
import org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction
import org.openecard.plugins.pinplugin.RecognizedState

/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class GenericPINStep(
	id: String,
	title: String?,
	private val cardCapturer: CardCapturer,
) : Step(id, title) {
	// indicators set by the action
	private var wrongPINFormat = false
	private var failedPINVerify = false
	private var wrongCANFormat = false
	private var failedCANVerify = false
	private var wrongPUKFormat = false
	private var failedPUKVerify = false
	private var canSuccess = false

	private var retryCounterPIN = 0
	private var retryCounterPUK = 10

	private val capturedState: CardStateView = cardCapturer.aquireView()

	fun getConHandle() = capturedState.handle

	fun getPinState() = capturedState.pinState

	init {
		generateGenericGui()
	}

	fun capturePin() = capturedState.capturePin()

	private fun generateGenericGui() {
		when (capturedState.pinState) {
			RecognizedState.PIN_ACTIVATED_RC3 -> generateGuiPinActivatedRc3()
			RecognizedState.PIN_ACTIVATED_RC2 -> {
				title = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized()
				retryCounterPIN = 2
				if (capturePin()) {
					createPINChangeGui()
				} else {
					createPINChangeGuiNative()
				}
			}

			RecognizedState.PIN_BLOCKED -> {
				title = I18N.strings.pinplugin_action_unblockpin_userconsent_pukstep_title.localized()
				retryCounterPIN = 0
				if (capturePin()) {
					createPUKGui()
				} else {
					createPUKGuiNative()
				}
			}

			RecognizedState.PIN_SUSPENDED -> {
				title = I18N.strings.pinplugin_action_changepin_userconsent_title.localized()
				retryCounterPIN = 1
				if (capturePin()) {
					createCANGui()
				} else {
					createCANGuiNative()
				}
			}

			RecognizedState.PIN_RESUMED -> {
				title = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized()
				retryCounterPIN = 1
				canSuccess = true
				if (capturePin()) {
					createPINChangeGui()
				} else {
					createPINChangeGuiNative()
				}
			}

			RecognizedState.PIN_DEACTIVATED -> {
				title = I18N.strings.pinplugin_action_changepin_userconsent_errorstep_title.localized()
				retryCounterPIN = -1
				createErrorGui()
			}

			RecognizedState.UNKNOWN -> {
				title = I18N.strings.pinplugin_action_changepin_userconsent_errorstep_title.localized()
				retryCounterPIN = -2
				createErrorGui()
			}

			RecognizedState.PUK_BLOCKED -> {
				title = I18N.strings.pinplugin_action_changepin_userconsent_errorstep_title.localized()
				createErrorGui()
				retryCounterPUK = 0
			}
		}
	}

	fun generateGuiPinActivatedRc3() {
		title = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized()
		retryCounterPIN = 3
		if (capturePin()) {
			createPINChangeGui()
		} else {
			createPINChangeGuiNative()
		}
	}

	fun updateState(newState: RecognizedState) {
		cardCapturer.notifyCardStateChange(newState)
		inputInfoUnits.clear()
		generateGenericGui()
	}

	private fun createPINChangeGuiNative() {
		inputInfoUnits.add(
			Text(
				if (canSuccess) {
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_description_after_can.localized()
				} else {
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_native_start_description.localized()
				},
			),
		)

		if (failedPINVerify || canSuccess) {
			addVerifyFailed("PIN")
		}

		addRemainingAttempts()
	}

	private fun createPINChangeGui() {
		inputInfoUnits.add(
			Text(
				if (canSuccess) {
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_description_after_can
						.format("PIN")
						.localized()
				} else {
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_description
						.format("PIN")
						.localized()
				},
			),
		)

		inputInfoUnits.add(Text(" "))
		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_oldpin.localized(),
			),
		)
		inputInfoUnits.add(
			PasswordField(OLD_PIN_FIELD).apply {
				minLength = 5
				maxLength = 6
			},
		)

		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_newpin.localized(),
			),
		)
		inputInfoUnits.add(
			PasswordField(NEW_PIN_FIELD).apply {
				minLength = 6
				maxLength = 6
			},
		)

		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_newpinrepeat.localized(),
			),
		)
		inputInfoUnits.add(
			PasswordField(NEW_PIN_REPEAT_FIELD).apply {
				minLength = 6
				maxLength = 6
			},
		)

		if (wrongPINFormat) {
			// add note for mistyped PIN
			inputInfoUnits.add(
				Text(
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_wrong_entry
						.format("PIN")
						.localized(),
				),
			)
		}

		if (failedPINVerify) {
			// add note for incorrect input
			addVerifyFailed("PIN")
		}

		addRemainingAttempts()
	}

	private fun createPUKGuiNative() {
		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_native_start_description.localized(),
			),
		)
		// show the puk try counter
		// 	Text pukTryCounter = new Text();
		// 	pukTryCounter.setText(lang.translationForKey(REMAINING_ATTEMPTS, retryCounterPUK));
		// 	inputInfoUnits.add(pukTryCounter);
		if (failedPUKVerify) {
			addVerifyFailed("PUK")
		}
	}

	private fun createPUKGui() {
		val i1 = Text()
		inputInfoUnits.add(i1)

		i1.text = I18N.strings.pinplugin_action_unblockpin_userconsent_pukstep_description.localized()
		inputInfoUnits.add(
			PasswordField(PUK_FIELD).apply {
				maxLength = 10
				minLength = 10
				description = I18N.strings.pinplugin_action_unblockpin_userconsent_pukstep_puk.localized()
			},
		)

		// show the puk try counter
		// Text pukTryCounter = new Text();
		// pukTryCounter.setText(lang.translationForKey(REMAINING_ATTEMPTS, retryCounterPUK));
		// inputInfoUnits.add(pukTryCounter);
		if (wrongPUKFormat) {
			// add note for mistyped PUK
			inputInfoUnits.add(
				Text(
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_wrong_entry
						.format("PUK")
						.localized(),
				),
			)
		}

		if (failedPUKVerify) {
			// add note for incorrect input
			addVerifyFailed("PUK")
		}
	}

	private fun createCANGuiNative() {
		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_native_start_description.localized(),
			),
		)
		if (failedCANVerify) {
			addVerifyFailed("CAN")
		}
	}

	private fun createCANGui() {
		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_canstep_notice.localized(),
			),
		)
		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_canstep_description.localized(),
			),
		)

		// add description and input fields depending on terminal type
		inputInfoUnits.add(
			PasswordField(CAN_FIELD).apply {
				minLength = 6
				maxLength = 6
				description = I18N.strings.pinplugin_action_changepin_userconsent_canstep_can.localized()
			},
		)

		if (wrongCANFormat) {
			// add note for mistyped CAN
			inputInfoUnits.add(
				Text(
					I18N.strings.pinplugin_action_changepin_userconsent_canstepaction_wrong_can.localized(),
				),
			)
		}

		if (failedCANVerify) {
			// add note for incorrect input
			addVerifyFailed("CAN")
		}
	}

	private fun createErrorGui() {
		id = "error"
		isReversible = false
		inputInfoUnits.add(
			when (capturedState.pinState) {
				RecognizedState.PIN_DEACTIVATED ->
					Text(
						I18N.strings.pinplugin_action_changepin_userconsent_errorstep_deactivated.localized(),
					)
				RecognizedState.PUK_BLOCKED ->
					Text(
						I18N.strings.pinplugin_action_changepin_userconsent_errorstep_puk_blocked.localized(),
					)
				RecognizedState.UNKNOWN ->
					Text(
						I18N.strings.pinplugin_action_changepin_userconsent_errorstep_unknown.localized(),
					)

				RecognizedState.PIN_ACTIVATED_RC3,
				RecognizedState.PIN_ACTIVATED_RC2,
				RecognizedState.PIN_SUSPENDED,
				RecognizedState.PIN_RESUMED,
				RecognizedState.PIN_BLOCKED,
				-> {
					TODO()
				}
			},
		)
	}

	fun setFailedPINVerify(
		wrongFormat: Boolean,
		failedVerify: Boolean,
	) {
		wrongPINFormat = wrongFormat
		failedPINVerify = failedVerify
		exportPinCorrect(!wrongFormat && !failedVerify)
	}

	fun setFailedCANVerify(
		wrongFormat: Boolean,
		failedVerify: Boolean,
	) {
		wrongCANFormat = wrongFormat
		failedCANVerify = failedVerify
		exportCanCorrect(!wrongFormat && !failedVerify)
	}

	fun setFailedPUKVerify(
		wrongFormat: Boolean,
		failedVerify: Boolean,
	) {
		wrongPUKFormat = wrongFormat
		failedPUKVerify = failedVerify
		exportPukCorrect(!wrongFormat && !failedVerify)
	}

	private fun exportPinCorrect(isCorrect: Boolean) {
		val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)!!
		ctx.put(GetCardsAndPINStatusAction.Companion.PIN_CORRECT, isCorrect)
	}

	private fun exportCanCorrect(isCorrect: Boolean) {
		val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)!!
		ctx.put(GetCardsAndPINStatusAction.Companion.CAN_CORRECT, isCorrect)
	}

	private fun exportPukCorrect(isCorrect: Boolean) {
		val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)!!
		ctx.put(GetCardsAndPINStatusAction.Companion.PUK_CORRECT, isCorrect)
	}

	private fun decreasePUKCounter() {
		retryCounterPUK--
		inputInfoUnits.clear()
		generateGenericGui()
	}

	private fun addRemainingAttempts() {
		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_remaining_attempts
					.format(retryCounterPIN)
					.localized(),
			),
		)
	}

	private fun addVerifyFailed(did: String?) {
		inputInfoUnits.add(
			Text(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_incorrect_input
					.format(did as Any)
					.localized(),
			),
		)
	}

	companion object {
		// protected GUI element IDs
		const val OLD_PIN_FIELD: String = "OLD_PIN_FIELD"
		const val NEW_PIN_FIELD: String = "NEW_PIN_FIELD"
		const val NEW_PIN_REPEAT_FIELD: String = "NEW_PIN_REPEAT_FIELD"
		const val PUK_FIELD: String = "PUK_FIELD"
		const val CAN_FIELD: String = "CAN_FIELD"
	}
}
