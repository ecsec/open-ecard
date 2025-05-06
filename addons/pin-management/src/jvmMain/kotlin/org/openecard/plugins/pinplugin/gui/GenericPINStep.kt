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

import org.openecard.common.DynamicContext
import org.openecard.common.I18n
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
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
	id: String?,
	title: String?,
	private val cardCapturer: CardCapturer,
) : Step(id, title) {
	private val lang: I18n = I18n.getTranslation("pinplugin")

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
				title = lang.translationForKey(CHANGE_PIN_TITLE)
				retryCounterPIN = 2
				if (capturePin()) {
					createPINChangeGui()
				} else {
					createPINChangeGuiNative()
				}
			}

			RecognizedState.PIN_BLOCKED -> {
				title = lang.translationForKey(PUKSTEP_TITLE)
				retryCounterPIN = 0
				if (capturePin()) {
					createPUKGui()
				} else {
					createPUKGuiNative()
				}
			}

			RecognizedState.PIN_SUSPENDED -> {
				title = lang.translationForKey(CANSTEP_TITLE)
				retryCounterPIN = 1
				if (capturePin()) {
					createCANGui()
				} else {
					createCANGuiNative()
				}
			}

			RecognizedState.PIN_RESUMED -> {
				title = lang.translationForKey(CHANGE_PIN_TITLE)
				retryCounterPIN = 1
				canSuccess = true
				if (capturePin()) {
					createPINChangeGui()
				} else {
					createPINChangeGuiNative()
				}
			}

			RecognizedState.PIN_DEACTIVATED -> {
				title = lang.translationForKey(ERROR_TITLE)
				retryCounterPIN = -1
				createErrorGui()
			}

			RecognizedState.UNKNOWN -> {
				title = lang.translationForKey(ERROR_TITLE)
				retryCounterPIN = -2
				createErrorGui()
			}

			RecognizedState.PUK_BLOCKED -> {
				title = lang.translationForKey(ERROR_TITLE)
				createErrorGui()
				retryCounterPUK = 0
			}
		}
	}

	fun generateGuiPinActivatedRc3() {
		title = lang.translationForKey(CHANGE_PIN_TITLE)
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
					lang.translationForKey(PINSTEP_NATIV_CHANGE_DESCRIPTION_AFTER_CAN)
				} else {
					lang.translationForKey(PINSTEP_NATIV_CHANGE_DESCRIPTION)
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
					lang.translationForKey(PINSTEP_DESCRIPTION_AFTER_CAN, "PIN")
				} else {
					lang.translationForKey(PINSTEP_DESCRIPTION, "PIN")
				},
			),
		)

		inputInfoUnits.add(Text(" "))
		inputInfoUnits.add(Text(lang.translationForKey(PINSTEP_OLDPIN)))
		inputInfoUnits.add(
			PasswordField(OLD_PIN_FIELD).apply {
				minLength = 5
				maxLength = 6
			},
		)

		inputInfoUnits.add(Text(lang.translationForKey(PINSTEP_NEWPIN)))
		inputInfoUnits.add(
			PasswordField(NEW_PIN_FIELD).apply {
				minLength = 6
				maxLength = 6
			},
		)

		inputInfoUnits.add(Text(lang.translationForKey(PINSTEP_NEWPINREPEAT)))
		inputInfoUnits.add(
			PasswordField(NEW_PIN_REPEAT_FIELD).apply {
				minLength = 6
				maxLength = 6
			},
		)

		if (wrongPINFormat) {
			// add note for mistyped PIN
			inputInfoUnits.add(Text(lang.translationForKey(WRONG_ENTRY, "PIN")))
		}

		if (failedPINVerify) {
			// add note for incorrect input
			addVerifyFailed("PIN")
		}

		addRemainingAttempts()
	}

	private fun createPUKGuiNative() {
		inputInfoUnits.add(Text(lang.translationForKey(PUKSTEP_START_NATIV_DESCRIPTION)))
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

		i1.setText(lang.translationForKey(PUKSTEP_DESCRIPTION))
		inputInfoUnits.add(
			PasswordField(PUK_FIELD).apply {
				maxLength = 10
				minLength = 10
				description = lang.translationForKey(PUKSTEP_PUK)
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
					lang.translationForKey(WRONG_ENTRY, "PUK"),
				),
			)
		}

		if (failedPUKVerify) {
			// add note for incorrect input
			addVerifyFailed("PUK")
		}
	}

	private fun createCANGuiNative() {
		inputInfoUnits.add(Text(lang.translationForKey(CANSTEP_START_NATIV_DESCRIPTION)))
		if (failedCANVerify) {
			addVerifyFailed("CAN")
		}
	}

	private fun createCANGui() {
		inputInfoUnits.add(
			Text(
				lang.translationForKey(CANSTEP_NOTICE),
			),
		)
		inputInfoUnits.add(
			Text(lang.translationForKey(CANSTEP_DESCRIPTION)),
		)

		// add description and input fields depending on terminal type
		inputInfoUnits.add(
			PasswordField(CAN_FIELD).apply {
				minLength = 6
				maxLength = 6
				description = lang.translationForKey(CANSTEP_CAN)
			},
		)

		if (wrongCANFormat) {
			// add note for mistyped CAN
			inputInfoUnits.add(
				Text(
					lang.translationForKey(WRONG_CAN),
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
				RecognizedState.PIN_DEACTIVATED -> Text(lang.translationForKey(ERRORSTEP_DEACTIVATED))
				RecognizedState.PUK_BLOCKED -> Text(lang.translationForKey(ERRORSTEP_PUK_BLOCKED))
				RecognizedState.UNKNOWN -> Text(lang.translationForKey(ERRORSTEP_UNKNOWN))

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
		val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)
		ctx.put(GetCardsAndPINStatusAction.Companion.PIN_CORRECT, isCorrect)
	}

	private fun exportCanCorrect(isCorrect: Boolean) {
		val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)
		ctx.put(GetCardsAndPINStatusAction.Companion.CAN_CORRECT, isCorrect)
	}

	private fun exportPukCorrect(isCorrect: Boolean) {
		val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)
		ctx.put(GetCardsAndPINStatusAction.Companion.PUK_CORRECT, isCorrect)
	}

	private fun decreasePUKCounter() {
		retryCounterPUK--
		inputInfoUnits.clear()
		generateGenericGui()
	}

	private fun addRemainingAttempts() {
		inputInfoUnits.add(
			Text(lang.translationForKey(REMAINING_ATTEMPTS, retryCounterPIN)),
		)
	}

	private fun addVerifyFailed(did: String?) {
		inputInfoUnits.add(
			Text(lang.translationForKey(INCORRECT_INPUT, did)),
		)
	}

	companion object {
		// translation constants PIN Change
		private const val CHANGE_PIN_TITLE = "action.changepin.userconsent.pinstep.title"
		private const val PINSTEP_NEWPINREPEAT = "action.changepin.userconsent.pinstep.newpinrepeat"
		private const val PINSTEP_NEWPIN = "action.changepin.userconsent.pinstep.newpin"
		private const val PINSTEP_OLDPIN = "action.changepin.userconsent.pinstep.oldpin"
		private const val PINSTEP_DESCRIPTION = "action.changepin.userconsent.pinstep.description"
		private const val PINSTEP_DESCRIPTION_AFTER_CAN = "action.changepin.userconsent.pinstep.description_after_can"
		private const val REMAINING_ATTEMPTS = "action.changepin.userconsent.pinstep.remaining_attempts"
		private const val WRONG_ENTRY = "action.changepin.userconsent.pinstep.wrong_entry"
		private const val INCORRECT_INPUT = "action.changepin.userconsent.pinstep.incorrect_input"
		private const val PINSTEP_NATIV_CHANGE_DESCRIPTION =
			"action.changepin.userconsent.pinstep.native_start_description"
		private const val PINSTEP_NATIV_CHANGE_DESCRIPTION_AFTER_CAN =
			"action.changepin.userconsent.pinstep.native_start_description_after_can"

		// translation constants PUK entring
		private const val PUKSTEP_DESCRIPTION = "action.unblockpin.userconsent.pukstep.description"
		private const val PUKSTEP_TITLE = "action.unblockpin.userconsent.pukstep.title"
		private const val PUKSTEP_PUK = "action.unblockpin.userconsent.pukstep.puk"
		private const val PUKSTEP_START_NATIV_DESCRIPTION =
			"action.unblockpin.userconsent.pukstep.nativ_start_description"

		// translation constants CAN entering
		private const val CANSTEP_TITLE = "action.changepin.userconsent.canstep.title"
		private const val CANSTEP_NOTICE = "action.changepin.userconsent.canstep.notice"
		private const val CANSTEP_CAN = "action.changepin.userconsent.canstep.can"
		private const val CANSTEP_DESCRIPTION = "action.changepin.userconsent.canstep.description"
		private const val WRONG_CAN = "action.changepin.userconsent.canstepaction.wrong_can"
		private const val CANSTEP_START_NATIV_DESCRIPTION =
			"action.changepin.userconsent.canstepaction.nativ_start_description"

		private const val ERROR_TITLE = "action.changepin.userconsent.errorstep.title"
		private const val ERRORSTEP_DEACTIVATED = "action.changepin.userconsent.errorstep.deactivated"
		private const val ERRORSTEP_PUK_BLOCKED = "action.changepin.userconsent.errorstep.puk_blocked"
		private const val ERRORSTEP_UNKNOWN = "action.changepin.userconsent.errorstep.unknown"

		// protected GUI element IDs
		const val OLD_PIN_FIELD: String = "OLD_PIN_FIELD"
		const val NEW_PIN_FIELD: String = "NEW_PIN_FIELD"
		const val NEW_PIN_REPEAT_FIELD: String = "NEW_PIN_REPEAT_FIELD"
		const val PUK_FIELD: String = "PUK_FIELD"
		const val CAN_FIELD: String = "CAN_FIELD"
	}
}
