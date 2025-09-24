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
package org.openecard.gui

import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.Step

/**
 * Interface for a step result with status and result values.
 *
 * @author Tobias Wich
 */
interface StepResult {
	/**
	 * Get step definition of the matching step.
	 *
	 * @return The step instance which belongs to this result.
	 */
	fun getStep(): Step?

	/**
	 * Get ID value of the matching step.
	 *
	 * @return ID of the step.
	 */
	fun getStepID(): String?

	/**
	 * Get result status of the step this result belongs to.
	 * The invocation of this function may block, if the result is not ready at the time this function is called. In the
	 * [org.openecard.gui.executor.ExecutionEngine], the StepResult is returned right after calling the
	 * [UserConsentNavigator.next] function. After that, the results status is checked, but the user might not
	 * have produced a result yet.
	 *
	 * @return Result status of the step.
	 */
	fun getStatus(): ResultStatus?

	/**
	 * Convenience method for [.getStatus] with check if the status is [ResultStatus.OK].
	 *
	 * @return `true` if result status is OK, `false` otherwise.
	 */
	fun isOK(): Boolean

	/**
	 * Convenience method for [.getStatus] with check if the status is [ResultStatus.BACK].
	 *
	 * @return `true` if result status is BACK, `false` otherwise.
	 */
	fun isBack(): Boolean

	/**
	 * Convenience method for [.getStatus] with check if the status is [ResultStatus.CANCEL].
	 *
	 * @return `true` if result status is CANCEL, `false` otherwise.
	 */
	fun isCancelled(): Boolean

	/**
	 * Convenience method for [.getStatus] with check if the status is [ResultStatus.RELOAD].
	 *
	 * @return `true` if result status is RELOAD, `false` otherwise.
	 */
	fun isReload(): Boolean

	fun getResults(): List<OutputInfoUnit>

	/**
	 * Returns the replacement step for the next display.
	 * This method blocks if the dialog is still displayed. The blocking behaviour is defined in the documentation for
	 * `getStatus`.
	 *
	 * @return The replacement step, or `null` if no replacement should be performed.
	 */
	fun getReplacement(): Step?
}

val StepResult.step: Step?
	get() = this.getStep()

val StepResult.stepID: String?
	get() = this.getStepID()

val StepResult.status: ResultStatus?
	get() = this.getStatus()

val StepResult.results: List<OutputInfoUnit>
	get() = this.getResults()

val StepResult.replacement: Step?
	get() = this.getReplacement()
