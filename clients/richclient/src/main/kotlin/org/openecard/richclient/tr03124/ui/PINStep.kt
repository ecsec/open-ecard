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

import dev.icerock.moko.resources.format
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.i18n.I18N
import org.openecard.richclient.tr03124.EacProcessState
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.utils.common.cast

/**
 * PIN GUI step for EAC.
 * This GUI step behaves differently
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 */
class PINStep(
	private val state: EacProcessState,
) : Step(STEP_ID, "Dummy-Title") {
	private val hasAttemptsCounter: Boolean = state.uiStep.guiData.pinType != PacePinId.CAN

	private val hasCanEntry = false

	init {
		val pinType =
			state.uiStep.guiData.pinType
				.toPinType()

		title =
			I18N.strings.pace_step_pace_title
				.format(pinType)
				.localized()
		description =
			I18N.strings.pace_step_pace_description
				.format(pinType)
				.localized()
		isReversible = false

		// TransactionInfo
		state.uiStep.guiData.transactionInfo?.let { transactionInfo ->
			val transactionInfoField = Text()
			transactionInfoField.text =
				I18N.strings.eac_transaction_info
					.format(transactionInfo)
					.localized()
			inputInfoUnits.add(transactionInfoField)
		}

		// create step elements
		if (!state.nativePace) {
			addSoftwareElements(pinType)
		} else {
			addTerminalElements(pinType)
		}

		updateStatus()
	}

	fun updateStatus() {
		updateAttemptsDisplay()
		updateCanData()
	}

	private fun addSoftwareElements(pinType: String) {
		isResetOnLoad = true
		val description = Text()
		description.text =
			I18N.strings.pace_step_pace_description
				.format(pinType)
				.localized()
		inputInfoUnits.add(description)

		val pinInputField = PasswordField(PIN_FIELD)
		pinInputField.description = pinType
		pinInputField.minLength =
			state.uiStep.guiData.pinType
				.minLen()
		pinInputField.maxLength =
			state.uiStep.guiData.pinType
				.maxLen()
		inputInfoUnits.add(pinInputField)

		if (hasAttemptsCounter) {
			val attemptCount = Text()
			attemptCount.text =
				I18N.strings.pace_step_pin_retrycount
					.format(3)
					.localized()
			attemptCount.id = PIN_ATTEMPTS_ID
			inputInfoUnits.add(attemptCount)
		}

		val notice = Text()
		notice.text =
			I18N.strings.eac_forward_notice
				.format(pinType)
				.localized()
		inputInfoUnits.add(notice)
	}

	private fun addTerminalElements(pinType: String) {
		isInstantReturn = true
		val description = Text()
		description.text =
			I18N.strings.pace_step_pace_native_description
				.format(pinType)
				.localized()
		inputInfoUnits.add(description)

		val notice = Text()
		notice.text =
			I18N.strings.eac_forward_notice
				.format(pinType)
				.localized()
		inputInfoUnits.add(notice)

		if (hasAttemptsCounter) {
			val attemptCount = Text()
			attemptCount.text =
				I18N.strings.pace_step_pin_retrycount
					.format(3)
					.localized()
			attemptCount.id = PIN_ATTEMPTS_ID
			inputInfoUnits.add(attemptCount)
		}
	}

	private fun addCANEntry() {
		val inputInfoUnits = inputInfoUnits
		var hasCanField = false
		var hasCanNotice = false
		for (inputInfoUnit in inputInfoUnits) {
			if (CAN_FIELD == inputInfoUnit.id) {
				hasCanField = true
			}
			if (CAN_NOTICE_ID == inputInfoUnit.id) {
				hasCanNotice = true
			}
		}
		if (!hasCanField) {
			val canField = PasswordField(CAN_FIELD)
			canField.description = I18N.strings.pace_can.localized()
			canField.maxLength = 6
			canField.minLength = 6
			inputInfoUnits.add(canField)
		}
		if (!hasCanNotice) {
			val canNotice = Text()
			canNotice.text = I18N.strings.eac_can_notice.localized()
			canNotice.id = CAN_NOTICE_ID
			inputInfoUnits.add(canNotice)
		}
	}

	private fun addNativeCANNotice() {
		val canNotice = Text()
		canNotice.text = I18N.strings.eac_can_notice_native.localized()
		canNotice.id = CAN_NOTICE_ID
		inputInfoUnits.add(canNotice)
	}

	private fun updateCanData() {
		val isRequestCan = state.status?.cast<SecurityCommandFailure>()?.retries == 1
		if (!hasCanEntry && isRequestCan) {
			ensureCanData()
		}
	}

	private fun ensureCanData() {
		if (!state.nativePace) {
			addCANEntry()
		} else {
			addNativeCANNotice()
		}
	}

	private fun updateAttemptsDisplay() {
		val retries: Int =
			when (val status = state.status) {
				// no status available or card status is good
				null,
				is SecurityCommandSuccess,
				-> 3
				is SecurityCommandFailure -> {
					status.retries ?: 0
				}
			}

		inputInfoUnits
			.filterIsInstance<Text>()
			.find { it.id == PIN_ATTEMPTS_ID }
			?.let { unit ->
				unit.text =
					I18N.strings.pace_step_pin_retrycount
						.format(retries)
						.localized()
			}
	}

	companion object {
		// step id
		const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_PIN"

		// GUI element IDs
		internal const val PIN_FIELD: String = "PACE_PIN_FIELD"
		internal const val CAN_FIELD: String = "PACE_CAN_FIELD"

		private const val CAN_NOTICE_ID = "PACE_CAN_NOTICE"
		private const val PIN_ATTEMPTS_ID = "PACE_PIN_ATTEMPTS"

		private val PIN = I18N.strings.pace_pin.localized()
		private val PUK = I18N.strings.pace_puk.localized()

		private fun PacePinId.toPinType(): String =
			when (this) {
				PacePinId.MRZ -> I18N.strings.pace_mrz.localized()
				PacePinId.CAN -> I18N.strings.pace_can.localized()
				PacePinId.PIN -> I18N.strings.pace_pin.localized()
				PacePinId.PUK -> I18N.strings.pace_puk.localized()
			}

		private fun PacePinId.minLen(): Int =
			when (this) {
				PacePinId.MRZ -> 30
				PacePinId.CAN -> 6
				PacePinId.PIN -> 6
				PacePinId.PUK -> 8
			}

		private fun PacePinId.maxLen(): Int =
			when (this) {
				PacePinId.MRZ -> 256
				PacePinId.CAN -> 6
				PacePinId.PIN -> 6
				PacePinId.PUK -> 8
			}

		fun createDummy(pinId: PacePinId): Step {
			val s = Step(STEP_ID)
			val pinType = pinId.toPinType()

			s.title =
				I18N.strings.pace_step_pace_title
					.format(pinType)
					.localized()
			s.description =
				I18N.strings.pace_step_pace_description
					.format(pinType)
					.localized()
			return s
		}

		fun buildPinStep(state: EacProcessState): Step {
			val status = state.status?.cast<SecurityCommandFailure>()

			if (status?.authBlocked == true) {
				return ErrorStep(
					I18N.strings.pace_step_error_title_blocked
						.format(PIN as Any)
						.localized(),
					I18N.strings.pace_step_error_pin_blocked
						.format(PIN, PIN, PUK as Any, PIN)
						.localized(),
				)
			} else if (status?.authDeactivated == true) {
				return ErrorStep(
					I18N.strings.pace_step_error_title_deactivated.localized(),
					I18N.strings.pace_step_error_pin_deactivated.localized(),
				)
			} else {
				val pinStep = PINStep(state)
				val pinAction =
					if (state.uiStep.guiData.pinType == PacePinId.CAN) {
						CANStepAction(pinStep, state)
					} else {
						PINStepAction(pinStep, state)
					}
				pinStep.action = pinAction
				return pinStep
			}
		}
	}
}
