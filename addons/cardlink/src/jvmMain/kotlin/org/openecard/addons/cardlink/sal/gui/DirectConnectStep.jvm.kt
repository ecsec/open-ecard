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
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.addon.Context
import org.openecard.addons.cardlink.sal.CardLinkKeys
import org.openecard.addons.cardlink.ws.WsPair
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
import org.openecard.common.WSHelper.WSException
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.sal.util.InsertCardHelper
import org.openecard.gui.StepResult
import org.openecard.gui.StepWithConnection
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.mobile.activation.CardLinkErrorCodes
import org.openecard.sal.protocol.eac.gui.ErrorStep

private val LOG = KotlinLogging.logger {}

private const val STEP_ID = "PROTOCOL_CARDLINK_GUI_STEP_DIRECT_CONNECT"
private const val STEP_TITLE = "Direct connect"

private val lang = I18n.getTranslation("pace")
private val langPin = I18n.getTranslation("pinplugin")

private const val ERROR_CARD_REMOVED = "action.error.card.removed"
private const val ERROR_TITLE = "action.error.title"
private const val ERROR_UNKNOWN = "action.error.unknown"

class DirectConnectStep(
	val ws: WsPair,
	val addonCtx: Context,
	val sessHandle: ConnectionHandleType,
	stepId: String = STEP_ID,
	title: String = STEP_TITLE,
) : StepWithConnection(stepId, title, sessHandle) {
	init {
		setAction(DirectConnectStepAction(this))
	}
}

class DirectConnectStepAction(
	private val directConnectStep: DirectConnectStep,
) : StepAction(directConnectStep) {
	override fun perform(
		oldResults: MutableMap<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

		try {
			val requiredCardTypes = setOf("http://ws.gematik.de/egk/1.0.0")
			val conHandle =
				dynCtx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType? ?: directConnectStep.sessHandle
			val ph = InsertCardHelper(directConnectStep.addonCtx, conHandle)
			// connect card as we need to use it right away
			val cardHandle = ph.connectCardIfNeeded(requiredCardTypes)
			// safe for later process
			dynCtx.put(TR03112Keys.CONNECTION_HANDLE, cardHandle)

			// if we have a wired card we can procede with cardlink
			return if (isWired(cardHandle, dynCtx)) {
				StepActionResult(StepActionResultStatus.NEXT)
				// we have radio based connection, we will net CAN
			} else {
				StepActionResult(
					StepActionResultStatus.REPEAT,
					EnterCanStep(
						directConnectStep.ws,
						directConnectStep.addonCtx,
						directConnectStep.sessHandle,
					),
				)
			}
		} catch (ex: WSException) {
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
						langPin.translationForKey(ERROR_CARD_REMOVED),
						ex,
					),
				)
			}

			val errorMessage = "An unknown error occurred."
			LOG.error(ex) { errorMessage }

			dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.OTHER_PACE_ERROR)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

			return StepActionResult(
				StepActionResultStatus.CANCEL,
				ErrorStep(
					langPin.translationForKey(ERROR_TITLE),
					langPin.translationForKey(ERROR_UNKNOWN),
					ex,
				),
			)
		} catch (ex: InterruptedException) {
			val errorMessage = "Connect step action interrupted."
			LOG.info { errorMessage }

			dynCtx.put(CardLinkKeys.CLIENT_ERROR_CODE, CardLinkErrorCodes.ClientCodes.CAN_STEP_INTERRUPTED)
			dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMessage)

			return StepActionResult(StepActionResultStatus.REPEAT)
		}
	}

	@Throws(WSException::class)
	private fun isWired(
		conHandle: ConnectionHandleType,
		dynCtx: DynamicContext,
	): Boolean {
		val req =
			GetIFDCapabilities().apply {
				contextHandle = conHandle.contextHandle
				ifdName = conHandle.ifdName
			}

		val capabilitiesResponse = directConnectStep.addonCtx.dispatcher.safeDeliver(req) as GetIFDCapabilitiesResponse
		checkResult(capabilitiesResponse)

		val isWired =
			capabilitiesResponse.ifdCapabilities.slotCapability
				.flatMap { it.protocol }
				.firstOrNull { p -> ECardConstants.IFD.Protocol.isWired(p) }

		return isWired != null
	}
}
