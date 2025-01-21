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
package org.openecard.gui.swing

import org.openecard.gui.UserConsentNavigator
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.TextField
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.gui.executor.StepAction
import org.testng.annotations.Test

/**
 * Test class for manual test of the input field validation.
 *
 * @author Dirk Petrautzki
 */
class InputFieldValidationTest {
    @Test(enabled = false)
    fun test() {
        // create wait action
        val action = WaitAction("step1", WAIT_TIME)
        // create GUI
        val nav = createNavigator(action)
        val exec = ExecutionEngine(nav)

        exec.process()
    }


    private fun createNavigator(waitAction: StepAction?): UserConsentNavigator {
        // create step
        val ucd = UserConsentDescription("consent title")

        val s = Step("step title")
        ucd.getSteps().add(s)
		s.id = "step1"

        s.setAction(waitAction)
        val desc1 = Text()
        s.getInputInfoUnits().add(desc1)
        desc1.setText(
            "This test shows a text input field to the user with a minimum length of 4 and a maximum " +
                    "length of 6 to test the input validation."
        )

        val input = TextField("input1")
		input.maxLength = 6
		input.minLength = 4
        s.getInputInfoUnits().add(input)

        val sc = SwingUserConsent(SwingDialogWrapper())
        return sc.obtainNavigator(ucd)
    }

}

// 2 seconds
private const val WAIT_TIME = 2 * 1000L
