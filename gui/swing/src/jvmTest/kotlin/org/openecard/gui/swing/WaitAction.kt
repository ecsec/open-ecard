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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus

private val LOG = KotlinLogging.logger { }

class WaitAction(
	stepId: String?,
	private val sleepTime: Long,
) : StepAction(stepId) {
	var startTime: Long = 0
		private set

	constructor(step: Step, sleepTime: Long) : this(step.id, sleepTime)

	override fun perform(
		oldResults: Map<String, ExecutionResults>?,
		result: StepResult?,
	): StepActionResult {
		LOG.info { "sleeping for $sleepTime ms." }
		startTime = System.currentTimeMillis()
		try {
			Thread.sleep(sleepTime)
		} catch (e: InterruptedException) {
			// ignore in test
		}
		val actionResult = StepActionResult(StepActionResultStatus.NEXT)
		return actionResult
	}
}
