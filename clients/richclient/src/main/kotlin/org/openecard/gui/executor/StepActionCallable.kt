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
import java.util.concurrent.Callable

/**
 * Wrapper class to embed a StepAction into a Callable, so that Futures can be created.
 * This class is used only internally in the [ExecutionEngine] to be able to stop actions if the user cancels the
 * process.
 *
 * @author Tobias Wich
 */
internal class StepActionCallable(
	private val action: StepAction,
	private val oldResults: Map<String, ExecutionResults>,
	private val result: StepResult,
) : Callable<StepActionResult?> {
	@Throws(Exception::class)
	override fun call(): StepActionResult = action.perform(oldResults, result)
}
