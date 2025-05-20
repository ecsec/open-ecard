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
 **************************************************************************/
package org.openecard.plugins.pinplugin.gui

import dev.icerock.moko.resources.format
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.i18n.I18N

/**
 * The Step for changing the PIN.
 * This step simply requests the user to enter the old and the new PIN.
 *
 * @param id The ID to initialize the step with.
 * @param title Title string of the step.
 * @param capturePin True if the PIN has to be captured by software else false.
 * @param retryCounter The current retry counter for the PIN.
 * @param enteredWrong True if the user entered the PIN wrong before and a corresponding text should be displayed.
 * @param verifyFailed*
 *
 * @author Dirk Petrautzki
 */
class ChangePINStep(
	id: String,
	title: String?,
	capturePin: Boolean,
	retryCounter: Int,
	enteredWrong: Boolean,
	verifyFailed: Boolean,
) : Step(id, title) {
	init {
		isReversible = false

		if (retryCounter < 1) {
			// show unblocking required message and return
			inputInfoUnits.add(
				Text(
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_unblocking_required.localized(),
				),
			)
		} else {

			val i1 = Text()
			inputInfoUnits.add(i1)

			if (!capturePin) {
				isInstantReturn = true
				i1.text = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_native_description.localized()
			} else {
				i1.text =
					I18N.strings.pinplugin_action_changepin_userconsent_pinstep_description.localized()

				inputInfoUnits.add(
					PasswordField(OLD_PIN_FIELD).apply {
						description = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_oldpin.localized()
					},
				)

				inputInfoUnits.add(
					PasswordField(NEW_PIN_FIELD).apply {
						description = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_newpin.localized()
					},
				)
				inputInfoUnits.add(
					PasswordField(NEW_PIN_REPEAT_FIELD).apply {
						description = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_newpinrepeat.localized()
					},
				)
			}

			if (enteredWrong) {
				// add note for mistyped PIN
				inputInfoUnits.add(
					Text(
						I18N.strings.pinplugin_action_changepin_userconsent_pinstep_wrong_entry.localized(),
					),
				)
			}

			if (verifyFailed) {
				// add note for incorrect input
				inputInfoUnits.add(
					Text(
						I18N.strings.pinplugin_action_changepin_userconsent_pinstep_incorrect_input.localized(),
					),
				)
			}

			if (retryCounter < 3) {
				// display the remaining attempts
				inputInfoUnits.add(
					Text(
						I18N.strings.pinplugin_action_changepin_userconsent_pinstep_remaining_attempts
							.format(retryCounter)
							.localized(),
					),
				)
			}
		}
	}

	companion object {
		// GUI element IDs
		const val OLD_PIN_FIELD: String = "OLD_PIN_FIELD"
		const val NEW_PIN_FIELD: String = "NEW_PIN_FIELD"
		const val NEW_PIN_REPEAT_FIELD: String = "NEW_PIN_REPEAT_FIELD"
	}
}
