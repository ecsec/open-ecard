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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.common.AppVersion.name
import org.openecard.common.I18n
import org.openecard.common.interfaces.Dispatcher
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.plugins.pinplugin.RecognizedState

/**
 * Implements a dialog for changing the PIN.
 * <br></br> This dialog guides the user through the process needed for changing the PIN.
 *
 * @author Dirk Petrautzki
 *
 * Creates a new instance of ChangePINDialog.
 *
 * @param gui The UserConsent to show on
 * @param capturePin True if the PIN has to be captured by software else false
 * @param conHandle to get the requested card type from
 * @param dispatcher The Dispatcher to use
 * @param state The State of the PIN
 */
class ChangePINDialog(
	private val gui: UserConsent,
	private val dispatcher: Dispatcher,
	private val conHandle: ConnectionHandleType,
	private val state: RecognizedState,
	private val capturePin: Boolean,
) {
	private val lang: I18n = I18n.getTranslation("pinplugin")

	private fun createUserConsentDescription() =
		UserConsentDescription(lang.translationForKey(TITLE, name)).apply {
			steps.addAll(createSteps())
		}

	/**
	 * Create the list of steps depending on the state of the pin.
	 *
	 * @return list of steps for the Dialog
	 */
	private fun createSteps(): MutableList<Step> {
		val steps: MutableList<Step> = mutableListOf()

		if (state == RecognizedState.PIN_BLOCKED ||
			state == RecognizedState.PIN_DEACTIVATED ||
			state == RecognizedState.UNKNOWN
		) {
			return steps.apply { add(createErrorStep()) }
		}

		return steps.apply {
			add(createCANStep())
			add(createChangePINStep())
			add(createSuccessStep())
		}
	}

	/**
	 * Create the step that informs the user that everything went fine.
	 *
	 * @return Step showing success message
	 */
	private fun createSuccessStep() =
		Step("success", lang.translationForKey(SUCCESSSTEP_TITLE)).apply {
			isReversible = false
			inputInfoUnits.add(
				Text(lang.translationForKey(SUCCESSSTEP_DESCRIPTION)),
			)
		}

	/**
	 * Create the step that informs the user that something went wrong.
	 *
	 * @return Step with error description
	 */
	private fun createErrorStep() =
		Step("error", lang.translationForKey(ERRORSTEP_TITLE)).apply {
			isReversible = false
			inputInfoUnits.add(
				Text(
					when (state) {
						RecognizedState.PIN_BLOCKED -> lang.translationForKey(ERRORSTEP_BLOCKED)
						RecognizedState.PIN_DEACTIVATED -> lang.translationForKey(ERRORSTEP_DEACTIVATED)
						else -> lang.translationForKey(ERRORSTEP_UNKNOWN)
					},
				),
			)
		}

	/**
	 * Create the step that asks the user to insert the old and new pins.
	 *
	 * @return Step for PIN entry
	 */
	private fun createChangePINStep() =
		ChangePINStep(
			"pin-entry",
			title = lang.translationForKey(PINSTEP_TITLE),
			capturePin,
			retryCounter = getRetryCounterFromState(state),
			enteredWrong = false,
			verifyFailed = false,
		).apply {
			action =
				PINStepAction(
					capturePin,
					conHandle,
					dispatcher,
					this,
					getRetryCounterFromState(state),
				)
		}

	/**
	 * Create the step that asks the user to insert the CAN.
	 *
	 * @return Step for CAN entry
	 */
	private fun createCANStep() =
		CANEntryStep(
			"can-entry",
			title = lang.translationForKey(CANSTEP_TITLE),
			capturePin,
			state,
			enteredWrong = false,
			verifyFailed = false,
		).apply {
			action = CANStepAction(capturePin, conHandle, dispatcher, this, state)
		}

	/**
	 * Shows this Dialog.
	 */
	fun show() {
		ExecutionEngine(
			gui.obtainNavigator(createUserConsentDescription()),
		).process()
	}

	/**
	 * Converts the state to the corresponding retry counter.
	 *
	 * @param state The current state of the PIN.
	 * @return The corresponding retry counter.
	 */
	private fun getRetryCounterFromState(state: RecognizedState) =
		when (state) {
			RecognizedState.PIN_ACTIVATED_RC3 -> 3
			RecognizedState.PIN_ACTIVATED_RC2 -> 2
			RecognizedState.PIN_SUSPENDED -> 1
			else -> 0
		}

	companion object {
		// translation constants
		private const val ERRORSTEP_UNKNOWN = "action.changepin.userconsent.errorstep.unknown"
		private const val ERRORSTEP_DEACTIVATED = "action.changepin.userconsent.errorstep.deactivated"
		private const val ERRORSTEP_BLOCKED = "action.changepin.userconsent.errorstep.blocked"
		private const val SUCCESSSTEP_DESCRIPTION = "action.changepin.userconsent.successstep.description"
		private const val CANSTEP_TITLE = "action.changepin.userconsent.canstep.title"
		private const val PINSTEP_TITLE = "action.changepin.userconsent.pinstep.title"
		private const val ERRORSTEP_TITLE = "action.changepin.userconsent.errorstep.title"
		private const val SUCCESSSTEP_TITLE = "action.changepin.userconsent.successstep.title"
		private const val TITLE = "action.changepin.userconsent.title"
	}
}
