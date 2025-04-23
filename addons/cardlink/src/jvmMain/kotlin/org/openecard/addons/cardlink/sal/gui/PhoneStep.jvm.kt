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
import java.util.*

private val logger = KotlinLogging.logger {}

private const val PHONE_ENTER_STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_PHONE"
private const val PHONE_ENTER_TITLE = "Phone Number Entry"

private const val PHONE_RETRY_STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_PHONE_RETRY"
private const val PHONE_RETRY_TITLE = "Phone Number Retry Entry"

private const val PHONE_ID = "CARDLINK_FIELD_PHONE"

abstract class PhoneStepAbstract(
	open val ws: WsPair,
	stepId: String,
	title: String,
) : Step(stepId, title)

class PhoneStep(
	override val ws: WsPair,
) : PhoneStepAbstract(ws, PHONE_ENTER_STEP_ID, PHONE_ENTER_TITLE) {
	init {
		setAction(PhoneStepAction(this))

		inputInfoUnits.add(
			TextField(PHONE_ID).also {
				it.minLength = 6
			},
		)
	}
}

class PhoneRetryStep(
	override val ws: WsPair,
) : PhoneStepAbstract(ws, PHONE_RETRY_STEP_ID, PHONE_RETRY_TITLE) {
	init {
		setAction(PhoneStepAction(this))

		inputInfoUnits.add(
			TextField(PHONE_ID).also {
				it.minLength = 6
			},
		)
	}
}

class PhoneStepAction(
	private val phoneStep: PhoneStepAbstract,
) : StepAction(phoneStep) {
	override fun perform(
		oldResults: MutableMap<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		val phoneNumber = (oldResults[stepID]!!.getResult(PHONE_ID) as TextField).value.concatToString()
		val sendPhoneStatus = sendPhoneNumber(phoneNumber)

		return sendPhoneStatus
	}

	private fun sendPhoneNumber(phoneNumber: String): StepActionResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		val correlationId = UUID.randomUUID().toString()
		val cardSessionId = dynCtx.get(CardLinkKeys.CARD_SESSION_ID) as String

		val sendPhoneNumber = SendPhoneNumber(phoneNumber)
		val egkEnvelope =
			GematikEnvelope(
				sendPhoneNumber,
				correlationId,
				cardSessionId,
			)
		val egkEnvelopeMsg = cardLinkJsonFormatter.encodeToString(egkEnvelope)
		val ws = phoneStep.ws
		ws.socket.send(egkEnvelopeMsg)

		val wsListener = ws.listener
		val phoneNumberResponse: GematikEnvelope? = wsListener.nextMessageBlocking()

		if (phoneNumberResponse == null) {
			val errorMsg = "Timeout happened during waiting for $REQUEST_SMS_TAN_RESPONSE from CardLink-Service."
			logger.error { errorMsg }
			dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, CardLinkErrorCodes.CardLinkCodes.SERVER_TIMEOUT)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)
			return StepActionResult(
				StepActionResultStatus.CANCEL,
				ErrorStep(
					"CardLink Error",
					errorMsg,
				),
			)
		}

		val egkPayload = phoneNumberResponse.payload
		logger.debug { "egkPayload in phonestep: $egkPayload" }

		if (egkPayload is TasklistErrorPayload) {
			val errorMsg = egkPayload.errormessage ?: "Received an unknown error from CardLink service."
			val errorResultCode =
				CardLinkErrorCodes.CardLinkCodes.byStatus(egkPayload.status) ?: CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR
			logger.warn { "Received '${TASK_LIST_ERROR}': $errorMsg (Result Code: $errorResultCode)" }
			dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, errorResultCode)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)
			throw WSHelper.makeResultError(errorResultCode.name, errorMsg).toException()
		}

		if (egkPayload is ConfirmPhoneNumber) {
			dynCtx.put(CardLinkKeys.CORRELATION_ID_TAN_PROCESS, phoneNumberResponse.correlationId)

			return if (
				(egkPayload.resultCode == ResultCode.SUCCESS && egkPayload.errorMessage == null) ||
				// support old server variants not sending success code
				(egkPayload.resultCode == null && egkPayload.minor == null && egkPayload.errorMessage == null)
			) {
				logger.debug { "Continue with next" }
				StepActionResult(StepActionResultStatus.NEXT)
			} else {
				logger.error {
					"Received error in Phone step from CardLink Service: ${egkPayload.errorMessage} (Status Code: ${egkPayload.resultCode})"
				}

				val resCode = egkPayload.resultCode ?: egkPayload.minor

				dynCtx.put(
					CardLinkKeys.SERVICE_ERROR_CODE,
					resCode?.toCardLinkErrorCode() ?: CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR,
				)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, egkPayload.errorMessage)

				val resultStatus =
					when (resCode) {
						ResultCode.NUMBER_FROM_WRONG_COUNTRY -> StepActionResultStatus.REPEAT
						ResultCode.INVALID_REQUEST -> StepActionResultStatus.REPEAT
						else -> StepActionResultStatus.CANCEL
					}

				val retryStep =
					when (resCode) {
						ResultCode.NUMBER_FROM_WRONG_COUNTRY -> PhoneRetryStep(phoneStep.ws)
						ResultCode.INVALID_REQUEST -> PhoneRetryStep(phoneStep.ws)
						else -> null
					}

				StepActionResult(resultStatus, retryStep)
			}
		} else {
			val errorMsg = "EGK Payload is not from type ConfirmPhoneNumber."
			logger.error { errorMsg }
			dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)
			return StepActionResult(
				StepActionResultStatus.CANCEL,
				ErrorStep(
					"CardLink Error",
					errorMsg,
				),
			)
		}
	}
}
