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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.sal.protocol.eac.EACProtocol
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Action waiting for the EAC process to finish.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
private val LOG = KotlinLogging.logger { }

class ProcessingStepAction(
	step: Step,
) : StepAction(step) {
	private val ctx: DynamicContext = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

	override fun perform(
		oldResults: MutableMap<String?, ExecutionResults?>?,
		result: StepResult?,
	): StepActionResult {
		val pAuthDone = ctx.getPromise(EACProtocol.AUTHENTICATION_DONE)
		try {
			pAuthDone.deref(120, TimeUnit.SECONDS)
			return StepActionResult(StepActionResultStatus.NEXT)
		} catch (ex: InterruptedException) {
			LOG.error(ex) { "ProcessingStepAction interrupted by the user or an other thread." }
			ctx.put(
				EACProtocol.PACE_EXCEPTION,
				createException(
					makeResultError(
						ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
						"User canceled the EAC dialog.",
					),
				),
			)
			return StepActionResult(StepActionResultStatus.CANCEL)
		} catch (ex: TimeoutException) {
			LOG.info(ex) { "Timeout while waiting for the authentication to finish." }
			ctx.put(
				EACProtocol.PACE_EXCEPTION,
				createException(
					makeResultError(
						ECardConstants.Minor.Disp.TIMEOUT,
						"Timeout during EAC process.",
					),
				),
			)
			return StepActionResult(StepActionResultStatus.CANCEL)
		}
	}
}
