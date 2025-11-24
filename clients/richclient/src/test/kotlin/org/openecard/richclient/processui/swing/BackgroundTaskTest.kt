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
package org.openecard.richclient.processui.swing

import org.junit.jupiter.api.Disabled
import org.openecard.richclient.processui.UserConsent
import org.openecard.richclient.processui.definition.Step
import org.openecard.richclient.processui.definition.Text
import org.openecard.richclient.processui.definition.UserConsentDescription
import org.openecard.richclient.processui.executor.BackgroundTask
import org.openecard.richclient.processui.executor.ExecutionEngine
import org.openecard.richclient.processui.executor.StepActionResult
import org.openecard.richclient.processui.executor.StepActionResultStatus
import kotlin.test.Test

/**
 *
 * @author Tobias Wich
 */
class BackgroundTaskTest {
	// TODO: skip test only in ci
	@Disabled
	@Test
	fun testWait() {
		val uc: UserConsent = SwingUserConsent(SwingDialogWrapper())

		val ucd = UserConsentDescription("Test background wait")
		val s = Step(title = "Wait Step")
		s.inputInfoUnits.add(Text("Please wait for the background task to complete ..."))
		s.backgroundTask =
			object : BackgroundTask {
				@Throws(Exception::class)
				override fun call(): StepActionResult {
					// first wait
					Thread.sleep(1000)
					// then repeat ;-)
					val replacement = Step(title = "Replacement Step")
					replacement.inputInfoUnits.add(Text("Super cool it works."))
					replacement.isInstantReturn = true
					return StepActionResult(StepActionResultStatus.REPEAT, replacement)
				}
			}
		ucd.steps.add(s)

		val e = ExecutionEngine(uc.obtainNavigator(ucd))
		e.process()
	}
}
