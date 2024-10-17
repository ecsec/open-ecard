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

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import org.openecard.addons.cardlink.sal.CardLinkKeys
import org.openecard.addons.cardlink.ws.*
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.WSHelper
import org.openecard.common.toException
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.TextField
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.mobile.activation.CardLinkErrorCodes
import org.openecard.sal.protocol.eac.gui.ErrorStep


private val logger = KotlinLogging.logger {}

private const val TAN_ENTER_STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_TAN"
private const val TAN_ENTER_TITLE = "TAN Verification"

private const val TAN_RETRY_STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_TAN_RETRY"
private const val TAN_RETRY_TITLE = "TAN Retry Verification"

private const val TAN_ID = "CARDLINK_FIELD_TAN"


abstract class TanStepAbstract(
	open val ws: WsPair,
	stepId: String,
	title: String
) : Step(stepId, title)

class TanStep(override val ws: WsPair) : TanStepAbstract(ws, TAN_ENTER_STEP_ID, TAN_ENTER_TITLE) {
	init {
		setAction(TanStepAction(this))

		inputInfoUnits.add(TextField(TAN_ID).also {
			it.minLength = 6
			it.description = "Please enter the TAN you received via SMS."
		})
	}
}

class TanRetryStep(override val ws: WsPair) : TanStepAbstract(ws, TAN_RETRY_STEP_ID, TAN_RETRY_TITLE) {
	init {
		setAction(TanStepAction(this))

		inputInfoUnits.add(TextField(TAN_ID).also {
			it.minLength = 6
			it.description = "Please enter the TAN you received via SMS."
		})
	}
}

class TanStepAction(private val tanStep: TanStepAbstract) : StepAction(tanStep) {

	override fun perform(oldResults: MutableMap<String, ExecutionResults>, result: StepResult): StepActionResult {
		val tan = (oldResults[stepID]!!.getResult(TAN_ID) as TextField).value.concatToString()
		val sendTanStatus = sendTan(tan)

		return sendTanStatus
	}

	private fun sendTan(tan: String): StepActionResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		val correlationId = dynCtx.get(CardLinkKeys.CORRELATION_ID_TAN_PROCESS) as String
		val cardSessionId = dynCtx.get(CardLinkKeys.CARD_SESSION_ID) as String

		val sendTan = SendTan(tan)
		val egkEnvelope = GematikEnvelope(
			sendTan,
			correlationId,
			cardSessionId,
		)
		val egkEnvelopeMsg = cardLinkJsonFormatter.encodeToString(egkEnvelope)
		val ws = tanStep.ws
		ws.socket.send(egkEnvelopeMsg)

		val wsListener = ws.listener
		val tanConfirmResponse : GematikEnvelope? = wsListener.nextMessageBlocking()

		if (tanConfirmResponse == null) {
			val errorMsg = "Timeout happened during waiting for $CONFIRM_TAN_RESPONSE from CardLink-Service."
			logger.error { errorMsg }

			dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)
			return StepActionResult(
				StepActionResultStatus.CANCEL,
				ErrorStep(
					"CardLink Error",
					errorMsg,
				)
			)
		}

		if (tanConfirmResponse.correlationId != correlationId) {
			val errorMsg = "Correlation-ID does not match with Correlation-ID from CardLink-Service."
			logger.error { errorMsg }
			return StepActionResult(
				StepActionResultStatus.CANCEL,
				ErrorStep(
					"CardLink Error",
					errorMsg,
				)
			)
		}

		val egkPayload = tanConfirmResponse.payload

		if (egkPayload is TasklistErrorPayload) {
			val errorMsg = egkPayload.errormessage ?: "Received an unknown error from CardLink service."
			val errorResultCode = CardLinkErrorCodes.CardLinkCodes.byStatus(egkPayload.status) ?: CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR
			logger.warn { "Received '${TASK_LIST_ERROR}': $errorMsg (Result Code: $errorResultCode)" }
			dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, errorResultCode)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)
			throw WSHelper.makeResultError(errorResultCode.name, errorMsg).toException()
		}

		if (tanConfirmResponse.correlationId != correlationId) {
			val errorMsg = "Received TAN-Confirm response where Correlation-ID does not match."
			logger.error { errorMsg }
			dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)
			return StepActionResult(
				StepActionResultStatus.CANCEL,
				ErrorStep(
					"CardLink Error",
					errorMsg,
				)
			)
		}

		if (egkPayload is ConfirmTan) {
			return if (egkPayload.resultCode == ResultCode.SUCCESS && egkPayload.errorMessage == null) {
				StepActionResult(StepActionResultStatus.NEXT)
			} else {
				logger.error { "Received error in Tan step from CardLink Service: ${egkPayload.errorMessage} (Status Code: ${egkPayload.resultCode})" }

				dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, egkPayload.resultCode.toCardLinkErrorCode())
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, egkPayload.errorMessage)

				val resultStatus = when (egkPayload.resultCode) {
					ResultCode.TAN_INCORRECT -> StepActionResultStatus.REPEAT
					ResultCode.TAN_EXPIRED -> StepActionResultStatus.BACK
					ResultCode.TAN_RETRY_LIMIT_EXCEEDED -> StepActionResultStatus.BACK
					else -> StepActionResultStatus.CANCEL
				}

				val retryStep = when (egkPayload.resultCode) {
					ResultCode.TAN_INCORRECT -> TanRetryStep(tanStep.ws)
					ResultCode.TAN_EXPIRED -> PhoneStep(tanStep.ws)
					ResultCode.TAN_RETRY_LIMIT_EXCEEDED -> PhoneStep(tanStep.ws)
					else -> null
				}

				StepActionResult(resultStatus, retryStep)
			}
		} else {
			val errorMsg = "EGK Payload is not from type ConfirmTan."
			logger.error { errorMsg }
			dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)
			return StepActionResult(
				StepActionResultStatus.CANCEL,
				ErrorStep(
					"CardLink Error",
					errorMsg,
				)
			)
		}
	}
}
