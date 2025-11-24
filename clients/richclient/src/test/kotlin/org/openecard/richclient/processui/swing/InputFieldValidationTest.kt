/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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
import org.openecard.richclient.processui.UserConsentNavigator
import org.openecard.richclient.processui.definition.Step
import org.openecard.richclient.processui.definition.Text
import org.openecard.richclient.processui.definition.TextField
import org.openecard.richclient.processui.definition.UserConsentDescription
import org.openecard.richclient.processui.executor.ExecutionEngine
import org.openecard.richclient.processui.executor.StepAction
import kotlin.test.Test

/**
 * Test class for manual test of the input field validation.
 *
 * @author Dirk Petrautzki
 */
class InputFieldValidationTest {
	@Disabled
	@Test
	fun test() {
		// create wait action
		val action = WaitAction("step1", WAIT_TIME)
		// create GUI
		val nav = createNavigator(action)
		val exec = ExecutionEngine(nav)

		exec.process()
	}

	private fun createNavigator(waitAction: StepAction): UserConsentNavigator {
		// create step
		val ucd = UserConsentDescription("consent title")

		val s = Step(title = "step title")
		ucd.steps.add(s)
		s.id = "step1"

		s.action = waitAction
		val desc1 = Text()
		s.inputInfoUnits.add(desc1)
		desc1.text =
			"This test shows a text input field to the user with a minimum length of 4 and a maximum " +
			"length of 6 to test the input validation."

		val input = TextField("input1")
		input.maxLength = 6
		input.minLength = 4
		s.inputInfoUnits.add(input)

		val sc = SwingUserConsent(SwingDialogWrapper())
		return sc.obtainNavigator(ucd)
	}
}

// 2 seconds
private const val WAIT_TIME = 2 * 1000L
