/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.gui.swing

import org.openecard.gui.UserConsent
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.BackgroundTask
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.testng.annotations.Test

/**
 *
 * @author Tobias Wich
 */
class BackgroundTaskTest {
	// TODO: skip test only in ci
	@Test(enabled = false)
	fun testWait() {
		val uc: UserConsent = SwingUserConsent(SwingDialogWrapper())

		val ucd = UserConsentDescription("Test background wait")
		val s = Step("Wait Step")
		s.getInputInfoUnits().add(Text("Please wait for the background task to complete ..."))
		s.backgroundTask =
			object : BackgroundTask {
				@Throws(Exception::class)
				override fun call(): StepActionResult {
					// first wait
					Thread.sleep(1000)
					// then repeat ;-)
					val replacement = Step("Replacement Step")
					replacement.getInputInfoUnits().add(Text("Super cool it works."))
					replacement.isInstantReturn = true
					return StepActionResult(StepActionResultStatus.REPEAT, replacement)
				}
			}
		ucd.getSteps().add(s)

		val e = ExecutionEngine(uc.obtainNavigator(ucd))
		e.process()
	}
}
