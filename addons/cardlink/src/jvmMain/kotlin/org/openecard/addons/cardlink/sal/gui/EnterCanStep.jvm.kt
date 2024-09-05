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
import org.openecard.common.sal.util.InsertCardHelper
import org.openecard.common.util.SysUtils
import org.openecard.gui.StepResult
import org.openecard.gui.StepWithConnection
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.TextField
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.ifd.protocol.pace.common.PasswordID
import org.openecard.mobile.activation.Websocket
import org.openecard.sal.protocol.eac.anytype.PACEInputType
import org.openecard.sal.protocol.eac.gui.CanLengthInvalidException
import org.openecard.sal.protocol.eac.gui.ErrorStep
import org.openecard.sal.protocol.eac.gui.PinOrCanEmptyException

private val LOG = KotlinLogging.logger {}

private const val STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_ENTER_CAN"
private const val title = "Enter CAN"

private const val CAN_ID = "CARDLINK_FIELD_CAN"

private val lang = I18n.getTranslation("pace")
private val langPin = I18n.getTranslation("pinplugin")

private const val ERROR_CARD_REMOVED = "action.error.card.removed"
private const val ERROR_TITLE = "action.error.title"
private const val ERROR_UNKNOWN = "action.error.unknown"


class EnterCanStep(val ws: WsPair, val addonCtx: Context, val sessHandle: ConnectionHandleType) :
	StepWithConnection(STEP_ID, title, sessHandle) {
	init {
		setAction(EnterCanStepAction(this))

		inputInfoUnits.add(TextField(CAN_ID).also {
			it.minLength = 6
			it.maxLength = 6
		})
	}
}

class EnterCanStepAction(val enterCanStep: EnterCanStep) : StepAction(enterCanStep) {

	override fun perform(oldResults: MutableMap<String, ExecutionResults>, result: StepResult): StepActionResult {
		try {
			val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

			val canValue = (oldResults[stepID]!!.getResult(CAN_ID) as TextField).value.concatToString()
			val requiredCardTypes = setOf("http://ws.gematik.de/egk/1.0.0")

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
					// repeat the step
					LOG.info { "Wrong CAN entered, trying again." }
					return StepActionResult(StepActionResultStatus.REPEAT)
				} else if (establishChannelResponse.result.resultMinor == ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE) {
					// card removed
					LOG.info { "Card was removed." }
					return StepActionResult(StepActionResultStatus.REPEAT)
				} else {
					checkResult(establishChannelResponse)
				}
			}

			return StepActionResult(StepActionResultStatus.NEXT)
		} catch (ex: WSException) {
			// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
			if (ex.resultMinor == ECardConstants.Minor.IFD.CANCELLATION_BY_USER) {
				LOG.error(ex) { "User canceled the authentication manually." }
				return StepActionResult(StepActionResultStatus.CANCEL)
			}


			// for people which think they have to remove the card in the process
			if (ex.resultMinor == ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE) {
				LOG.error(ex) {
					"The SlotHandle was invalid so probably the user removed the card or an reset occurred."
				}
				return StepActionResult(
					StepActionResultStatus.REPEAT,
					ErrorStep(
						lang.translationForKey(ERROR_TITLE),
						langPin.translationForKey(ERROR_CARD_REMOVED), ex
					)
				)
			}


			// repeat the step
			LOG.error(ex) { "An unknown error occurred while trying to verify the PIN." }
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				ErrorStep(
					langPin.translationForKey(ERROR_TITLE),
					langPin.translationForKey(ERROR_UNKNOWN), ex
				)
			)

		} catch (ex: InterruptedException) {
			LOG.warn(ex) { "CAN step action interrupted." }
			return StepActionResult(StepActionResultStatus.CANCEL)
		} catch (ex: PinOrCanEmptyException) {
			LOG.warn(ex) { "CAN was empty" }
			return StepActionResult(StepActionResultStatus.REPEAT)
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
