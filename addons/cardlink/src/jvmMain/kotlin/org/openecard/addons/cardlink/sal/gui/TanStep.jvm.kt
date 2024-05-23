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

import kotlinx.serialization.encodeToString
import org.openecard.addons.cardlink.sal.CardLinkKeys
import org.openecard.addons.cardlink.ws.*
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.TextField
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.mobile.activation.Websocket
import java.util.*

private const val STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_TAN"
private const val title = "TAN Verification"

private const val TAN_ID = "CARDLINK_FIELD_TAN"

class TanStep(val ws: Websocket) : Step(STEP_ID, title) {
	init {
		setAction(TanStepAction(this))

		inputInfoUnits.add(TextField(TAN_ID).also {
			it.minLength = 6
			// TODO: add descriptive data
		})
	}
}

class TanStepAction(private val tanStep: TanStep) : StepAction(tanStep) {

	override fun perform(oldResults: MutableMap<String, ExecutionResults>, result: StepResult): StepActionResult {
		val tan = (oldResults[stepID]!!.getResult(TAN_ID) as TextField).value.concatToString()
		val sendTanStatus = sendTan(tan)

		return StepActionResult(sendTanStatus)
	}

	private fun sendTan(tan: String): StepActionResultStatus {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		val cardSessionId = dynCtx.get(CardLinkKeys.WS_SESSION_ID) as String

		val sendTan = SendTan(tan)
		val egkEnvelope = EgkEnvelope(
			cardSessionId,
			null,
			sendTan,
			CONFIRM_TAN
		)
		val egkEnvelopeMsg = cardLinkJsonFormatter.encodeToString(egkEnvelope)

		val ws = tanStep.ws
		ws.connect()
		ws.send(egkEnvelopeMsg)

		TODO("Not implemented: wait for cardlink service answer if TAN is correct")

		return StepActionResultStatus.NEXT
	}

}
