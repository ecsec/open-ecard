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
 */
package org.openecard.gui.executor

import org.openecard.gui.ResultStatus
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step

/**
 * Dummy action to produce step results for the execution engine. <br></br>
 * The DummyAction is a no-OP action, which always returns a result according to the following mapping:
 *
 *  * [org.openecard.gui.ResultStatus.BACK] → [StepActionResultStatus.BACK]
 *  * [org.openecard.gui.ResultStatus.OK] → [StepActionResultStatus.NEXT]
 *  * * → [StepActionResultStatus.CANCEL]
 *
 *
 * @author Tobias Wich
 */
class DummyAction(
	step: Step,
) : StepAction(step) {
	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		// REPEAT must be performed explicitly
		return when (result.getStatus()) {
			ResultStatus.BACK -> StepActionResult(StepActionResultStatus.BACK)
			ResultStatus.OK -> StepActionResult(StepActionResultStatus.NEXT)
			ResultStatus.CANCEL -> StepActionResult(StepActionResultStatus.CANCEL)
			else -> StepActionResult(StepActionResultStatus.CANCEL)
		}
	}
}
