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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import org.openecard.addon.Context
import org.openecard.addons.cardlink.sal.CardLinkKeys
import org.openecard.addons.cardlink.ws.WsPair
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
import org.openecard.common.WSHelper.WSException
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.ifd.anytype.PACEInputType
import org.openecard.common.sal.util.InsertCardHelper
import org.openecard.common.util.SysUtils
import org.openecard.gui.StepResult
import org.openecard.gui.StepWithConnection
import org.openecard.gui.definition.TextField
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.ifd.protocol.pace.common.PasswordID
import org.openecard.mobile.activation.CardLinkErrorCodes
import org.openecard.sal.protocol.eac.gui.CanLengthInvalidException
import org.openecard.sal.protocol.eac.gui.ErrorStep
import org.openecard.sal.protocol.eac.gui.PinOrCanEmptyException

private val LOG = KotlinLogging.logger {}

private const val CAN_STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_ENTER_CAN"
private const val CAN_TITLE = "Enter CAN"

private const val CAN_RETRY_STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_ENTER_CAN_RETRY"
private const val CAN_RETRY_TITLE = "Enter CAN (Retry)"

private const val CAN_ID = "CARDLINK_FIELD_CAN"

private val lang = I18n.getTranslation("pace")
private val langPin = I18n.getTranslation("pinplugin")

private const val ERROR_CARD_REMOVED = "action.error.card.removed"
private const val ERROR_TITLE = "action.error.title"
private const val ERROR_UNKNOWN = "action.error.unknown"


abstract class EnterCanStepAbstract(
	open val ws: WsPair,
	open val addonCtx: Context,
	open val sessHandle: ConnectionHandleType,
	stepId: String,
	title: String
) : StepWithConnection(stepId, title, sessHandle)

class EnterCanStep(override val ws: WsPair, override val addonCtx: Context, override val sessHandle: ConnectionHandleType)
		: EnterCanStepAbstract(ws, addonCtx, sessHandle, CAN_STEP_ID, CAN_TITLE) {
	init {
		setAction(EnterCanStepAction(this))

		inputInfoUnits.add(TextField(CAN_ID).also {
			it.minLength = 6
			it.maxLength = 6
		})
	}
}

class EnterCanRetryStep(override val ws: WsPair, override val addonCtx: Context, override val sessHandle: ConnectionHandleType)
	: EnterCanStepAbstract(ws, addonCtx, sessHandle, CAN_RETRY_STEP_ID, CAN_RETRY_TITLE) {
	init {
		setAction(EnterCanStepAction(this))

		inputInfoUnits.add(TextField(CAN_ID).also {
			it.minLength = 6
			it.maxLength = 6
		})
	}
}

class EnterCanStepAction(val enterCanStep: EnterCanStepAbstract) : StepAction(enterCanStep) {

	override fun perform(oldResults: MutableMap<String, ExecutionResults>, result: StepResult): StepActionResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

		try {
			val canValue = (oldResults[stepID]!!.getResult(CAN_ID) as TextField).value.concatToString()
			val requiredCardTypes = setOf("http://ws.gematik.de/egk/1.0.0")

			if (canValue.isBlank()) {
				val errorMessage = "Empty CAN provided."
				// repeat the step
				LOG.info { errorMessage }

				dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CAN_EMPTY)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

				return StepActionResult(
					StepActionResultStatus.REPEAT,
					EnterCanRetryStep(enterCanStep.ws, enterCanStep.addonCtx, enterCanStep.sessHandle)
				)
			}

			try {
				canValue.toInt()
			} catch (ex: NumberFormatException) {
				val errorMessage = "Provided CAN is not numeric."
				// repeat the step
				LOG.info { errorMessage }

				dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CAN_NOT_NUMERIC)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

				return StepActionResult(
					StepActionResultStatus.REPEAT,
					EnterCanRetryStep(enterCanStep.ws, enterCanStep.addonCtx, enterCanStep.sessHandle)
				)
			}

			if (canValue.length > 6) {
				val errorMessage = "Wrong size of CAN: ${canValue.length}."
				// repeat the step
				LOG.info { errorMessage }

				dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CAN_TOO_LONG)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

				return StepActionResult(
					StepActionResultStatus.REPEAT,
					EnterCanRetryStep(enterCanStep.ws, enterCanStep.addonCtx, enterCanStep.sessHandle)
				)
			}

			val conHandle =
				dynCtx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType? ?: enterCanStep.sessHandle
			val ph = InsertCardHelper(enterCanStep.addonCtx, conHandle)

			if (SysUtils.isMobileDevice()) {
				// mobile device, pick only available reader and proceed
				ph.useMobileReader()
			}
			// connect card as we need to use it right away
			val cardHandle = ph.connectCardIfNeeded(requiredCardTypes)

			// safe for later process
			dynCtx.put(TR03112Keys.CONNECTION_HANDLE, cardHandle)

			val establishChannelResponse = performPACEWithCAN(canValue, cardHandle)

			if (establishChannelResponse.result.resultMajor == ECardConstants.Major.ERROR) {
				if (establishChannelResponse.result.resultMinor == ECardConstants.Minor.IFD.AUTHENTICATION_FAILED) {
					val errorMessage = "Wrong CAN entered, trying again."
					// repeat the step
					LOG.info { errorMessage }

					dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CAN_INCORRECT)
					dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

					return StepActionResult(
						StepActionResultStatus.REPEAT,
						EnterCanRetryStep(enterCanStep.ws, enterCanStep.addonCtx, enterCanStep.sessHandle)
					)
				} else if (establishChannelResponse.result.resultMinor == ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE) {
					val errorMessage = "Card was removed."
					// repeat the step
					LOG.info { errorMessage }

					dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CARD_REMOVED)
					dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

					return StepActionResult(
						StepActionResultStatus.REPEAT,
						EnterCanRetryStep(enterCanStep.ws, enterCanStep.addonCtx, enterCanStep.sessHandle)
					)
				} else {
					checkResult(establishChannelResponse)
				}
			}

			return StepActionResult(StepActionResultStatus.NEXT)
		} catch (ex: WSException) {
			// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
			if (ex.resultMinor == ECardConstants.Minor.IFD.CANCELLATION_BY_USER) {
				val errorMessage = "User canceled the authentication manually."
				LOG.error(ex) { errorMessage }

				dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.OTHER_CLIENT_ERROR)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

				return StepActionResult(StepActionResultStatus.CANCEL)
			}

			// for people which think they have to remove the card in the process
			if (ex.resultMinor == ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE) {
				val errorMessage = "The SlotHandle was invalid so probably the user removed the card or an reset occurred."
				LOG.error(ex) { errorMessage }

				dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.INVALID_SLOT_HANDLE)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

				return StepActionResult(
					StepActionResultStatus.REPEAT,
					ErrorStep(
						lang.translationForKey(ERROR_TITLE),
						langPin.translationForKey(ERROR_CARD_REMOVED), ex
					)
				)
			}

			// repeat the step
			val errorMessage = "An unknown error occurred while trying to verify the PIN."
			LOG.error(ex) { errorMessage }

			dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.OTHER_PACE_ERROR)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

			return StepActionResult(
				StepActionResultStatus.REPEAT,
				ErrorStep(
					langPin.translationForKey(ERROR_TITLE),
					langPin.translationForKey(ERROR_UNKNOWN), ex
				)
			)
		} catch (ex: InterruptedException) {
			val errorMessage = "CAN step action interrupted."
			LOG.info { errorMessage }

			dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CAN_STEP_INTERRUPTED)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

			return StepActionResult(
				StepActionResultStatus.REPEAT,
				EnterCanRetryStep(enterCanStep.ws, enterCanStep.addonCtx, enterCanStep.sessHandle)
			)
		} catch (ex: PinOrCanEmptyException) {
			val errorMessage = "CAN was empty."
			LOG.info { errorMessage }

			dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CAN_EMPTY)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

			return StepActionResult(
				StepActionResultStatus.REPEAT,
				EnterCanRetryStep(enterCanStep.ws, enterCanStep.addonCtx, enterCanStep.sessHandle)
			)
		}
	}

	@Throws(WSException::class, InterruptedException::class, CanLengthInvalidException::class)
	private fun performPACEWithCAN(
		canValue: String,
		conHandle: ConnectionHandleType
	): EstablishChannelResponse {
		val pinIdCan = "2"
		val paceInput = DIDAuthenticationDataType()
		paceInput.protocol = ECardConstants.Protocol.PACE
		val paceInputMap = AuthDataMap(paceInput).createResponse(paceInput)

		paceInputMap.addElement(PACEInputType.PIN, canValue)
		paceInputMap.addElement(PACEInputType.PIN_ID, PasswordID.CAN.byteAsString)

		// perform PACE by EstablishChannelCommand
		val eChannel = createEstablishChannelStructure(conHandle, paceInputMap)
		val res = enterCanStep.addonCtx.dispatcher.safeDeliver(eChannel) as EstablishChannelResponse

		return res
	}

	private fun createEstablishChannelStructure(
		conHandle: ConnectionHandleType,
		paceInputMap: AuthDataResponse<*>
	): EstablishChannel {
		// EstablishChannel
		return EstablishChannel().apply {
			slotHandle = conHandle.slotHandle
			authenticationProtocolData = paceInputMap.response
			authenticationProtocolData.protocol = ECardConstants.Protocol.PACE
		}
	}
}
