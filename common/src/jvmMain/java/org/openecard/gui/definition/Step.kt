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
package org.openecard.gui.definition

import org.openecard.common.util.ValueGenerators.genBase64Session
import org.openecard.gui.executor.BackgroundTask
import org.openecard.gui.executor.DummyAction
import org.openecard.gui.executor.StepAction

/**
 * Description class for user consent steps.
 * Each step represents one dialog.
 *
 * @author Tobias Wich
 */
open class Step(
	id: String,
	/**
	 * Sets the title of this step.
	 * The title may be used in a progress indicator and in a title element.
	 *
	 * @param title The title of this step.
	 */
	var title: String?,
) {
	var id: String = id

	/**
	 * Gets the title of this step.
	 * The title may be used in a progress indicator and in a title element.
	 *
	 * @return The title of this step.

	 * Gets the description of this step.
	 * The description may be used as a subtitle.
	 *
	 * @return The description of this step.

	 * Sets the description of this step.
	 * The description may be used as a subtitle.
	 *
	 * @param description The description of this step.
	 */
	var description: String? = null

	/**
	 * Sets the action to be associated with this step.
	 * Actions are a way to bind code to the step which is executed after the step is finished. The
	 * [org.openecard.gui.executor.ExecutionEngine] takes care of the action execution.
	 *
	 * @param action The action to be associated with this step, or `null` if the current action should be
	 * removed.
	 */
	var action: StepAction? = null
		/**
		 * Gets the action associated with this step.
		 * Actions are a way to bind code to the step which is executed after the step is finished. The
		 * [org.openecard.gui.executor.ExecutionEngine] takes care of the action execution.
		 *
		 * @return The action associated with this step, or a [DummyAction] if none is set.
		 */
		get() {
			if (field == null) {
				return DummyAction(this)
			}
			return field
		}

	/**
	 * Gets the background task associated with this step.
	 * Background tasks must be started by the GUI implementation in parallel to the display of the step. For detailed
	 * information about background tasks, refer to [BackgroundTask].
	 *
	 * @return The background task associated with this step, or `null` if none is set.

	 * Sets the background task to be associated with this step.
	 * Background tasks must be started by the GUI implementation in parallel to the display of the step. For detailed
	 * information about background tasks, refer to [BackgroundTask].
	 *
	 * @param backgroundTask The background task to be associated with this step, or `null` if the current
	 * background task should be removed.
	 */
	var backgroundTask: BackgroundTask? = null

	/**
	 * Gets whether the step allows to go back to the previous step.
	 *
	 * @return `true` if this step allows to go back, `false` otherwise.

	 * Sets whether the step allows to go back to the previous step.
	 *
	 * @param reversible `true` if this step allows to go back, `false` otherwise.
	 */
	var isReversible: Boolean = true

	/**
	 * Gets whether the step returns instantly after it is shown.
	 * This feature is only useful in combination with step actions. An action can perform lengthy operations. A step
	 * with instant return set can inform the user that such a lengthy operation takes place, but does not want the user
	 * to tell the dialog to proceed, because there is no user interaction necessary.
	 *
	 * @see .getAction
	 * @return `true` if this step is configured to return instantly, `false` otherwise.

	 * Sets whether the step returns instantly after it is shown.
	 * This feature is only useful in combination with step actions. An action can perform lengthy operations. A step
	 * with instant return set can inform the user that such a lengthy operation takes place, but does not want the user
	 * to tell the dialog to proceed, because there is no user interaction necessary.
	 *
	 * @see .getAction
	 * @param instantReturn `true` if this step is configured to return instantly, `false` otherwise.
	 */
	var isInstantReturn: Boolean = false

	/**
	 * Gets whether the elements' values on this step reset to their default values, when the step is shown again.
	 *
	 * @return `true` if this step resets its values, `false` otherwise.

	 * Sets whether the elements' values on this step reset to their default values, when the step is shown again.
	 *
	 * @param resetOnLoad `true` if this step resets its values, `false` otherwise.
	 */
	var isResetOnLoad: Boolean = false
	var inputInfoUnits: List<InputInfoUnit>? = null
		/**
		 * Gets the list of elements of this step.
		 * The returned list is modifiable and can be used to add and remove elements from the step.
		 *
		 * @return Modifiable list of the elements of this step.
		 */
		get() {
			if (field == null) {
				field = ArrayList()
			}
			return field
		}
		private set

	/**
	 * Creates a step with the given title and a generated ID.
	 *
	 * @see .setTitle
	 * @param title Title string of the step.
	 */
	constructor(title: String?) : this(genBase64Session(16), title)

	val isMetaStep: Boolean
		get() = inputInfoUnits!!.isEmpty()
}
