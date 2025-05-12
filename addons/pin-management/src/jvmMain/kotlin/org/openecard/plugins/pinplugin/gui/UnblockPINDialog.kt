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
import org.openecard.common.AppVersion
import org.openecard.common.I18n
import org.openecard.common.interfaces.Dispatcher
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.plugins.pinplugin.RecognizedState

/**
 * Implements a dialog for unblocking the PIN.
 * This dialog guides the user through the process needed for unblocking the PIN.
 *
 * @author Dirk Petrautzki
 * Creates a new instance of UnblockPINUserConsent.
 *
 * @param gui The UserConsent to show on
 * @param capturePin True if the PIN has to be captured by software else false
 * @param conHandle to get the requested card type from
 * @param dispatcher The Dispatcher to use
 * @param state The State of the PIN
 */
class UnblockPINDialog(
	private val gui: UserConsent,
	private val dispatcher: Dispatcher,
	private val conHandle: ConnectionHandleType,
	private val state: RecognizedState,
	private val capturePin: Boolean,
) {
	private val lang: I18n = I18n.getTranslation("pinplugin")

	private fun createUserConsentDescription() =
		UserConsentDescription(lang.translationForKey(TITLE, AppVersion.name)).apply {
			steps.addAll(createSteps())
		}

	/**
	 * Create the list of steps depending on the state of the pin.
	 *
	 * @return list of steps for the Dialog
	 */
	private fun createSteps() =
		if (state == RecognizedState.PIN_BLOCKED) {
			listOf(createPUKStep(), createSuccessStep())
		} else {
			listOf(createErrorStep())
		}

	/**
	 * Create the step that informs the user that everything went fine.
	 *
	 * @return Step showing success message
	 */
	private fun createSuccessStep() =
		Step("success", lang.translationForKey(SUCCESSSTEP_TITLE)).apply {
			inputInfoUnits.add(Text(lang.translationForKey(SUCCESSSTEP_DESCRIPTION)))
		}

	/**
	 * Create the step that informs the user that something went wrong.
	 *
	 * @return Step with error description
	 */
	private fun createErrorStep() =
		Step("insert-card", lang.translationForKey(ERRORSTEP_TITLE)).apply {
			inputInfoUnits.add(
				Text(lang.translationForKey(ERRORSTEP_DESCRIPTION)),
			)
		}

	/**
	 * Create the step that asks the user to insert the PUK.
	 *
	 * @return Step for PUK entry
	 */
	private fun createPUKStep(): Step {
		val pukStep = Step("insert-card", lang.translationForKey(PUKSTEP_TITLE))
		val i1 = Text()
		pukStep.inputInfoUnits.add(i1)

		if (!capturePin) {
			pukStep.isInstantReturn = true
			i1.text = lang.translationForKey(PUKSTEP_NATIVE_DESCRIPTION)
		} else {
			i1.text = lang.translationForKey(PUKSTEP_DESCRIPTION)
			pukStep.inputInfoUnits.add(
				PasswordField(PUK_FIELD).apply {
					description = lang.translationForKey(PUKSTEP_PUK)
				},
			)
		}

		pukStep.action = PUKStepAction(capturePin, conHandle.getSlotHandle(), dispatcher, pukStep)
		return pukStep
	}

	/**
	 * Shows this Dialog.
	 */
	fun show() {
		val ucr = gui.obtainNavigator(createUserConsentDescription())
		val exec = ExecutionEngine(ucr)
		exec.process()
	}

	companion object {
		private const val TITLE = "action.unblockpin.userconsent.title"
		private const val PUKSTEP_DESCRIPTION = "action.unblockpin.userconsent.pukstep.description"
		private const val PUKSTEP_NATIVE_DESCRIPTION = "action.unblockpin.userconsent.pukstep.native_description"
		private const val PUKSTEP_TITLE = "action.unblockpin.userconsent.pukstep.title"
		private const val PUKSTEP_PUK = "action.unblockpin.userconsent.pukstep.puk"
		private const val ERRORSTEP_TITLE = "action.unblockpin.userconsent.errorstep.title"
		private const val ERRORSTEP_DESCRIPTION = "action.unblockpin.userconsent.errorstep.description"
		private const val SUCCESSSTEP_TITLE = "action.unblockpin.userconsent.successstep.title"
		private const val SUCCESSSTEP_DESCRIPTION = "action.unblockpin.userconsent.successstep.description"

		// GUI element IDs
		const val PUK_FIELD: String = "PUK_FIELD"
	}
}
