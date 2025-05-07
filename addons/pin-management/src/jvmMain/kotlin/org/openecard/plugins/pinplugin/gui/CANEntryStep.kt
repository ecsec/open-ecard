/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

import org.openecard.common.I18n
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.plugins.pinplugin.RecognizedState

/**
 * The Step for entering the CAN.
 * This step simply requests the user to enter the CAN.
 *
 * @param id The ID to initialize the step with.
 * @param title Title string of the step.
 * @param capturePin True if the PIN has to be captured by software else false.
 * @param state The current state of the PIN.
 * @param enteredWrong True if the user entered the CAN wrong before and a corresponding text should be displayed.
 * @param verifyFailed True if user input verification failed else false.
 *
 * @author Dirk Petrautzki
 */
class CANEntryStep(
	id: String?,
	title: String?,
	capturePin: Boolean,
	state: RecognizedState,
	enteredWrong: Boolean,
	verifyFailed: Boolean,
) : Step(id, title) {
	private val lang: I18n = I18n.getTranslation("pinplugin")

	val i1 = Text(lang.translationForKey(CANSTEP_NOTICE))
	val i2 = Text()

	init {
		inputInfoUnits.add(i1)
		inputInfoUnits.add(i2)

		// add description and input fields depending on terminal type
		if (!capturePin) {
			isInstantReturn = true
			i2.setText(lang.translationForKey(CANSTEP_NATIVE_DESCRIPTION))
		} else {
			i2.setText(lang.translationForKey(CANSTEP_DESCRIPTION))
			val canField = PasswordField(CAN_FIELD)
			canField.description = lang.translationForKey(CANSTEP_CAN)
			inputInfoUnits.add(canField)
		}

		// return instant if PIN is not suspended
		if (state != RecognizedState.PIN_SUSPENDED) {
			isInstantReturn = true
		}

		if (enteredWrong) {
			// add note for mistyped CAN
			val retryText = Text()
			retryText.setText(lang.translationForKey(WRONG_CAN))
			inputInfoUnits.add(retryText)
		}

		if (verifyFailed) {
			// add note for incorrect input
			val incorrectInput = Text()
			incorrectInput.setText(lang.translationForKey(INCORRECT_INPUT))
			inputInfoUnits.add(incorrectInput)
		}
	}

	companion object {
		// translation constants
		private const val CANSTEP_NOTICE = "action.changepin.userconsent.canstep.notice"
		private const val CANSTEP_CAN = "action.changepin.userconsent.canstep.can"
		private const val CANSTEP_DESCRIPTION = "action.changepin.userconsent.canstep.description"
		private const val CANSTEP_NATIVE_DESCRIPTION = "action.changepin.userconsent.canstep.native_description"
		private const val WRONG_CAN = "action.changepin.userconsent.canstepaction.wrong_can"
		private const val INCORRECT_INPUT = "action.changepin.userconsent.canstepaction.incorrect_input"

		// GUI element id's
		const val CAN_FIELD: String = "CAN_FIELD"
	}
}
