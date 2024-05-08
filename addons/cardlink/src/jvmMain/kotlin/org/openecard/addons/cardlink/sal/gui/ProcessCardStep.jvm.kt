/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.addons.cardlink.sal.gui

import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.mobile.activation.Websocket

private val title = "Process Card"

class ProcessCardStep(val ws: Websocket) : Step(title) {
	init {
		setAction(ProcessCardStepAction(this))
	}
}

class ProcessCardStepAction(val processCardStep: ProcessCardStep) : StepAction(processCardStep) {

	override fun perform(oldResults: MutableMap<String, ExecutionResults>?, result: StepResult?): StepActionResult {
//		val requiredCardTypes = setOf("http://ws.gematik.de/egk/1.0.0")
//		val cardHandle = waitForCard(requiredCardTypes)
//		authCard(cardHandle)
//		val cardData = readPatientData(cardHandle)
//		sendPatientData(cardData)

		TODO("Not yet implemented")
	}

}
