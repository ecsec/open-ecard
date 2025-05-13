/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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
package org.openecard.sal.protocol.eac.gui

import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.WSHelper
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.sal.protocol.eac.EACProtocol

/**
 * Implements a Step which displays an error message.
 *
 * @author Hans-Martin Haase
 */
class ErrorStep(
	title: String?,
	errorText: String,
	paceException: WSHelper.WSException? = null,
) : Step(title) {
	private val ctx: DynamicContext

	init {
		id = STEP_ID

		ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

		// TODO: remove instantreturn to actually display an error
		// setInstantReturn(true);
		isReversible = false
		val pinBlockedNote = Text(errorText)
		getInputInfoUnits().add(pinBlockedNote)

		setAction(
			object : StepAction(id) {
				override fun perform(
					oldResults: MutableMap<String, ExecutionResults>,
					result: StepResult?,
				): StepActionResult {
					if (paceException != null) {
						ctx.put(EACProtocol.Companion.PACE_EXCEPTION, paceException)
					}
					return StepActionResult(StepActionResultStatus.CANCEL)
				}
			},
		)
	}

	companion object {
		const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_ERROR"
	}
}
