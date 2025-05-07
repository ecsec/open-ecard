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

import org.openecard.common.I18n
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text

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
	id: String?,
	title: String?,
	capturePin: Boolean,
	retryCounter: Int,
	enteredWrong: Boolean,
	verifyFailed: Boolean,
) : Step(id, title) {
	private val lang: I18n = I18n.getTranslation("pinplugin")

	init {
		isReversible = false

		if (retryCounter < 1) {
			// show unblocking required message and return
			inputInfoUnits.add(
				Text(
					lang.translationForKey(UNBLOCKING_REQUIRED),
				),
			)
		} else {

			val i1 = Text()
			inputInfoUnits.add(i1)

			if (!capturePin) {
				isInstantReturn = true
				i1.setText(lang.translationForKey(PINSTEP_NATIVE_DESCRIPTION))
			} else {
				i1.setText(lang.translationForKey(PINSTEP_DESCRIPTION))

				inputInfoUnits.add(
					PasswordField(OLD_PIN_FIELD).apply {
						description = lang.translationForKey(PINSTEP_OLDPIN)
					},
				)

				inputInfoUnits.add(
					PasswordField(NEW_PIN_FIELD).apply {
						description = lang.translationForKey(PINSTEP_NEWPIN)
					},
				)
				inputInfoUnits.add(
					PasswordField(NEW_PIN_REPEAT_FIELD).apply {
						description = lang.translationForKey(PINSTEP_NEWPINREPEAT)
					},
				)
			}

			if (enteredWrong) {
				// add note for mistyped PIN
				inputInfoUnits.add(
					Text(
						lang.translationForKey(WRONG_ENTRY),
					),
				)
			}

			if (verifyFailed) {
				// add note for incorrect input
				inputInfoUnits.add(
					Text(
						lang.translationForKey(INCORRECT_INPUT),
					),
				)
			}

			if (retryCounter < 3) {
				// display the remaining attempts
				inputInfoUnits.add(
					Text(
						lang.translationForKey(REMAINING_ATTEMPTS, retryCounter),
					),
				)
			}
		}
	}

	companion object {
		// translation constants
		private const val PINSTEP_NEWPINREPEAT = "action.changepin.userconsent.pinstep.newpinrepeat"
		private const val PINSTEP_NEWPIN = "action.changepin.userconsent.pinstep.newpin"
		private const val PINSTEP_OLDPIN = "action.changepin.userconsent.pinstep.oldpin"
		private const val PINSTEP_DESCRIPTION = "action.changepin.userconsent.pinstep.description"
		private const val PINSTEP_NATIVE_DESCRIPTION = "action.changepin.userconsent.pinstep.native_description"
		private const val UNBLOCKING_REQUIRED = "action.changepin.userconsent.pinstep.unblocking_required"
		private const val REMAINING_ATTEMPTS = "action.changepin.userconsent.pinstep.remaining_attempts"
		private const val WRONG_ENTRY = "action.changepin.userconsent.pinstep.wrong_entry"
		private const val INCORRECT_INPUT = "action.changepin.userconsent.pinstep.incorrect_input"

		// GUI element IDs
		const val OLD_PIN_FIELD: String = "OLD_PIN_FIELD"
		const val NEW_PIN_FIELD: String = "NEW_PIN_FIELD"
		const val NEW_PIN_REPEAT_FIELD: String = "NEW_PIN_REPEAT_FIELD"
	}
}
