/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

import org.openecard.gui.UserConsentNavigator
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.gui.executor.StepAction
import org.testng.Assert
import org.testng.annotations.Test

// TODO: make Selenium test which really proves, that the GUI works correctly

/**
 *
 * @author Tobias Wich
 */
class InstantReturnTest {
	// TODO: skip test only in ci

	/**
	 * Test if the GUI closes itself after executing an action with instantreturn set.
	 * There is no way to determine whether the GUI is displayed at all. This check must be part of a Selenium test.
	 */
	@Test(enabled = false)
	fun testInstantReturn() {
		// create wait action
		val action = WaitAction("step1", DIFF_TIME)
		// create GUI
		val nav = createNavigator(action)
		val exec = ExecutionEngine(nav)

		exec.process()
		// eliminate most of the GUI overhead by retrieving start time from action
		val startTime = action.startTime
		val stopTime = System.currentTimeMillis()

		val act = stopTime - startTime
		val diff: Long = act - DIFF_TIME
		val msg = "Display time of dialog differs ${diff}ms from reference value (${VARIANCE}ms allowed)."
		Assert.assertTrue(diff <= VARIANCE, msg)
	}

	private fun createNavigator(waitAction: StepAction?): UserConsentNavigator {
		// create step
		val ucd = UserConsentDescription("consent title")

		val s = Step("step title")
		ucd.getSteps().add(s)
		s.id = "step1"
		s.isInstantReturn = true
		s.setAction(waitAction)

		val desc1 = Text()
		s.getInputInfoUnits().add(desc1)
		desc1.setText("This test opens a step with instantreturn set. An action waits for two seconds.")
		val desc2 = Text()
		s.getInputInfoUnits().add(desc2)
		desc2.setText("If the window is closed after these two seconds, the test is successful.")

		val sc = SwingUserConsent(SwingDialogWrapper())
		return sc.obtainNavigator(ucd)
	}
}

// 2 seconds
private val DIFF_TIME = 2 * 1000L
private const val VARIANCE: Long = 1000
