/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.sal.protocol.eac.gui

import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.I18n
import org.openecard.common.ifd.PacePinStatus
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.ifd.protocol.pace.common.PasswordID
import org.openecard.ifd.protocol.pace.common.PasswordID.Companion.parse
import org.openecard.sal.protocol.eac.EACData
import org.openecard.sal.protocol.eac.EACProtocol
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType

private val LANG_EAC: I18n = I18n.getTranslation("eac")
private val LANG_PACE: I18n = I18n.getTranslation("pace")

// GUI translation constants
private const val TITLE = "step_pace_title"
private const val STEP_DESCRIPTION = "step_pace_step_description"
private const val DESCRIPTION = "step_pace_description"
private const val DESCRIPTION_NATIVE = "step_pace_native_description"
private const val NOTICE = "eac_forward_notice"
private const val TRANSACTION_INFO = "transaction_info"
private const val CAN_NOTICE_ID = "PACE_CAN_NOTICE"
private const val PIN_ATTEMPTS_ID = "PACE_PIN_ATTEMPTS"

/**
 * PIN GUI step for EAC.
 * This GUI step behaves differently
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 */
class PINStep(
	eacData: EACData,
	private val capturePin: Boolean,
	private val paceMarker: PACEMarkerType,
) : Step(STEP_ID, "Dummy-Title") {
	private val pinType = LANG_PACE.translationForKey(parse(eacData.pinID)!!.name)
	private val hasAttemptsCounter: Boolean = eacData.pinID != PasswordID.CAN.byte

	private var _status: PinState
	private val hasCanEntry = false

	init {
		title = LANG_PACE.translationForKey(TITLE, pinType)
		description = LANG_PACE.translationForKey(STEP_DESCRIPTION)
		isReversible = false

		// TransactionInfo
		val transactionInfo = eacData.transactionInfo
		if (transactionInfo != null) {
			val transactionInfoField = Text()
			transactionInfoField.text = LANG_EAC.translationForKey(TRANSACTION_INFO, transactionInfo)
			inputInfoUnits.add(transactionInfoField)
		}

		val ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
		this._status = ctx.get(EACProtocol.Companion.PIN_STATUS) as PinState

		// create step elements
		if (capturePin) {
			addSoftwareElements()
		} else {
			addTerminalElements()
		}
		updateCanData()
		updateAttemptsDisplay()
	}

	fun getStatus(): PinState = _status

	fun setStatus(status: PacePinStatus) {
		this._status.update(status)
		updateAttemptsDisplay()
		updateCanData()
	}

	fun setStatus(status: PinState) {
		this._status = status
		updateAttemptsDisplay()
		updateCanData()
	}

	private fun addSoftwareElements() {
		isResetOnLoad = true
		val description = Text()
		description.text = LANG_PACE.translationForKey(DESCRIPTION, pinType)
		inputInfoUnits.add(description)

		val pinInputField = PasswordField(PIN_FIELD)
		pinInputField.description = pinType
		pinInputField.minLength = paceMarker.getMinLength()
		pinInputField.maxLength = paceMarker.getMaxLength()
		inputInfoUnits.add(pinInputField)

		if (hasAttemptsCounter) {
			val attemptCount = Text()
			attemptCount.text = LANG_PACE.translationForKey("step_pin_retrycount", 3)
			attemptCount.id = PIN_ATTEMPTS_ID
			inputInfoUnits.add(attemptCount)
		}

		val notice = Text()
		notice.text = LANG_EAC.translationForKey(NOTICE, pinType)
		inputInfoUnits.add(notice)
	}

	private fun addTerminalElements() {
		isInstantReturn = true
		val description = Text()
		description.text = LANG_PACE.translationForKey(DESCRIPTION_NATIVE, pinType)
		inputInfoUnits.add(description)

		val notice = Text()
		notice.text = LANG_EAC.translationForKey(NOTICE, pinType)
		inputInfoUnits.add(notice)

		if (hasAttemptsCounter) {
			val attemptCount = Text()
			attemptCount.text = LANG_PACE.translationForKey("step_pin_retrycount", 3)
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
			canField.description = LANG_PACE.translationForKey("can")
			canField.maxLength = 6
			canField.minLength = 6
			inputInfoUnits.add(canField)
		}
		if (!hasCanNotice) {
			val canNotice = Text()
			canNotice.text = LANG_EAC.translationForKey("eac_can_notice")
			canNotice.id = CAN_NOTICE_ID
			inputInfoUnits.add(canNotice)
		}
	}

	private fun addNativeCANNotice() {
		val canNotice = Text()
		canNotice.text = LANG_EAC.translationForKey("eac_can_notice_native")
		canNotice.id = CAN_NOTICE_ID
		inputInfoUnits.add(canNotice)
	}

	private fun updateCanData() {
		if (!hasCanEntry && _status.isRequestCan) {
			ensureCanData()
		}
	}

	fun ensureCanData() {
		if (capturePin) {
			addCANEntry()
		} else {
			addNativeCANNotice()
		}
	}

	private fun updateAttemptsDisplay() {
		for (unit in inputInfoUnits) {
			if (unit.id == PIN_ATTEMPTS_ID) {
				val newValue: Int =
					when (_status.getState()) {
						PacePinStatus.RC3 -> 3
						PacePinStatus.RC2 -> 2
						PacePinStatus.RC1 -> 1
						else -> 0
					}

				val text = unit as Text
				text.text = LANG_PACE.translationForKey("step_pin_retrycount", newValue)
			}
		}
	}

	companion object {
		// step id
		const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_PIN"

		// GUI element IDs
		const val PIN_FIELD: String = "PACE_PIN_FIELD"
		const val CAN_FIELD: String = "PACE_CAN_FIELD"

		fun createDummy(pinId: Byte): Step {
			val s = Step(STEP_ID)
			val pinType: String? = LANG_PACE.translationForKey(parse(pinId)!!.name)
			s.title = LANG_PACE.translationForKey(TITLE, pinType)
			s.description = LANG_PACE.translationForKey(STEP_DESCRIPTION)
			return s
		}
	}
}
