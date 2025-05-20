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

import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.i18n.I18N
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
	id: String,
	title: String?,
	capturePin: Boolean,
	state: RecognizedState,
	enteredWrong: Boolean,
	verifyFailed: Boolean,
) : Step(id, title) {
	val i1 =
		Text(
			I18N.strings.pinplugin_action_changepin_userconsent_canstep_notice.localized(),
		)
	val i2 = Text()

	init {
		inputInfoUnits.add(i1)
		inputInfoUnits.add(i2)

		// add description and input fields depending on terminal type
		if (!capturePin) {
			isInstantReturn = true
			i2.text = I18N.strings.pinplugin_action_changepin_userconsent_canstep_native_description.localized()
		} else {
			i2.text = I18N.strings.pinplugin_action_changepin_userconsent_canstep_description.localized()
			val canField = PasswordField(CAN_FIELD)
			canField.description = I18N.strings.pinplugin_action_changepin_userconsent_canstep_can.localized()
			inputInfoUnits.add(canField)
		}

		// return instant if PIN is not suspended
		if (state != RecognizedState.PIN_SUSPENDED) {
			isInstantReturn = true
		}

		if (enteredWrong) {
			// add note for mistyped CAN
			val retryText = Text()
			retryText.text =
				I18N.strings.pinplugin_action_changepin_userconsent_canstepaction_wrong_can.localized()
			inputInfoUnits.add(retryText)
		}

		if (verifyFailed) {
			// add note for incorrect input
			val incorrectInput = Text()
			incorrectInput.text =
				I18N.strings.pinplugin_action_changepin_userconsent_canstepaction_incorrect_input.localized()
			inputInfoUnits.add(incorrectInput)
		}
	}

	companion object {
		// GUI element id's
		const val CAN_FIELD: String = "CAN_FIELD"
	}
}
