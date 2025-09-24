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

import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step

/**
 * Abstract base for all step actions.
 * Actions are associated with a step and are executed by the [ExecutionEngine].
 *
 * @author Tobias Wich
 */
abstract class StepAction(
	/**
	 * Gets the ID of the associated step.
	 *
	 * @return The step ID that this instance is associated with.
	 */
	val stepID: String,
) {
	/**
	 * Creates a step action and saves the steps ID it is associated with.
	 *
	 * @param step The step whose ID is saved.
	 */
	constructor(step: Step) : this(step.id)

	/**
	 * Runs the action this instance represents.
	 *
	 * @param oldResults Results available before execution of the action. This does not include element values of the
	 * current step.
	 * @param result Result of the current step before execution of the action.
	 * @return Result of the action.
	 */
	abstract fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult
}
