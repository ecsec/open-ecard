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

import dev.icerock.moko.resources.format
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.common.AppVersion.name
import org.openecard.common.interfaces.Dispatcher
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.i18n.I18N
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
	private fun createUserConsentDescription() =
		UserConsentDescription(
			I18N.strings.pinplugin_action_changepin_userconsent_title
				.format(name)
				.localized(),
		).apply {
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
		Step(
			"success",
			I18N.strings.pinplugin_action_changepin_userconsent_successstep_title.localized(),
		).apply {
			isReversible = false
			inputInfoUnits.add(
				Text(
					I18N.strings.pinplugin_action_changepin_userconsent_successstep_description.localized(),
				),
			)
		}

	/**
	 * Create the step that informs the user that something went wrong.
	 *
	 * @return Step with error description
	 */
	private fun createErrorStep() =
		Step(
			"error",
			I18N.strings.pinplugin_action_changepin_userconsent_errorstep_title.localized(),
		).apply {
			isReversible = false
			inputInfoUnits.add(
				Text(
					when (state) {
						RecognizedState.PIN_BLOCKED ->
							I18N.strings
								.pinplugin_action_changepin_userconsent_errorstep_blocked
								.localized()
						RecognizedState.PIN_DEACTIVATED ->
							I18N.strings
								.pinplugin_action_changepin_userconsent_errorstep_deactivated
								.localized()
						else -> I18N.strings.pinplugin_action_changepin_userconsent_errorstep_unknown.localized()
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
			title = I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized(),
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
			title = I18N.strings.pinplugin_action_changepin_userconsent_canstep_title.localized(),
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
}
