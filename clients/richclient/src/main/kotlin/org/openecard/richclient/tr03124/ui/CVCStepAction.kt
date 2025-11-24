/****************************************************************************
 * Copyright (C) 2025 ecsec GmbH.
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

package org.openecard.richclient.tr03124.ui

import org.openecard.richclient.processui.StepResult
import org.openecard.richclient.processui.definition.Step
import org.openecard.richclient.processui.executor.ExecutionResults
import org.openecard.richclient.processui.executor.StepAction
import org.openecard.richclient.processui.executor.StepActionResult
import org.openecard.richclient.processui.executor.StepActionResultStatus

/**
 *
 * @author Tobias Wich
 */
class CVCStepAction(
	step: Step,
) : StepAction(step) {
	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		if (result.isBack()) {
			// no going back to the initialization step
			return StepActionResult(StepActionResultStatus.REPEAT)
		}

		return StepActionResult(StepActionResultStatus.NEXT)
	}
}
