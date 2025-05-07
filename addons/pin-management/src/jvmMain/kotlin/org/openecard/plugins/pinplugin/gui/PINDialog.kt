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

import org.openecard.common.ThreadTerminateException
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.Promise
import org.openecard.gui.ResultStatus
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.plugins.pinplugin.CardCapturer

/**
 *
 * @author Hans-Martin Haase
 */
class PINDialog(
	private val gui: UserConsent,
	private val dispatcher: Dispatcher,
	private val cardCapturer: CardCapturer,
	private val errorPromise: Promise<Throwable?>,
) {
	/**
	 * Shows this Dialog.
	 * @return
	 */
	fun show(): ResultStatus {
		val ucr = gui.obtainNavigator(createUserConsentDescription())
		val exec = ExecutionEngine(ucr)
		return try {
			exec.process()
		} catch (_: ThreadTerminateException) {
			ResultStatus.INTERRUPTED
		}
	}

	private fun createUserConsentDescription(): UserConsentDescription {
		val uc = UserConsentDescription("PIN Operation", "pin_change_dialog")
		val gPINStep = GenericPINStep("GenericPINStepID", "GenericPINStep", this.cardCapturer)
		gPINStep.setAction(
			GenericPINAction(
				"PIN Management",
				dispatcher,
				gPINStep,
				cardCapturer,
				errorPromise,
			),
		)
		uc.getSteps().add(gPINStep)

		return uc
	}
}
