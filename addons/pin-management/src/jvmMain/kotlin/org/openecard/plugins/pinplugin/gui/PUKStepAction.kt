/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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
package org.openecard.plugins.pinplugin.gui

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.DestroyChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.ifd.anytype.PACEInputType
import org.openecard.common.interfaces.Dispatcher
import org.openecard.gui.StepResult
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import javax.xml.parsers.ParserConfigurationException

private val logger = KotlinLogging.logger { }

/**
 * StepAction for performing PACE with the PUK.
 *
 * @author Dirk Petrautzki
 *
 * Create a new instance of PUKStepAction.
 *
 * @param capturePin True if the PIN has to be captured by software else false
 * @param givenSlotHandle The unique SlotHandle for the card to use
 * @param step the step this action belongs to
 * @param dispatcher The Dispatcher to use
 */
class PUKStepAction(
	private val capturePin: Boolean,
	private val givenSlotHandle: ByteArray,
	private val dispatcher: Dispatcher,
	step: Step,
) : StepAction(step) {
	private var puk: String? = null

	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		if (result.isBack()) {
			return StepActionResult(StepActionResultStatus.BACK)
		}

		val paceInput =
			DIDAuthenticationDataType().apply {
				protocol = ECardConstants.Protocol.PACE
			}

		val tmp: AuthDataMap?
		try {
			tmp = AuthDataMap(paceInput)
		} catch (ex: ParserConfigurationException) {
			logger.error(ex) { "Failed to read empty Protocol data." }
			return StepActionResult(StepActionResultStatus.CANCEL)
		}

		val paceInputMap: AuthDataResponse<*> = tmp.createResponse<DIDAuthenticationDataType?>(paceInput)

		if (capturePin) {
			val executionResults = oldResults[stepID]

			if (!verifyUserInput(executionResults)) {
				// let the user enter the pin again, when there is none entered
				// TODO inform user that something with his input is wrong
				return StepActionResult(StepActionResultStatus.REPEAT)
			} else {
				paceInputMap.addElement(PACEInputType.PIN, puk)
			}
		}

		paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PUK)
		paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")
		// perform PACE by sending an EstablishChannel
		val establishChannel =
			EstablishChannel().apply {
				slotHandle = givenSlotHandle
				authenticationProtocolData = paceInputMap.response
				authenticationProtocolData.protocol = ECardConstants.Protocol.PACE
			}

		try {
			val establishChannelResponse = dispatcher.safeDeliver(establishChannel) as EstablishChannelResponse
			checkResult<EstablishChannelResponse>(establishChannelResponse)

			// pace was successfully performed, so get to the next step
			return StepActionResult(StepActionResultStatus.NEXT)
		} catch (ex: WSHelper.WSException) {
			logger.info { "Wrong PUK entered, trying again" }
			// TODO update the step to inform the user that he entered the puk wrong
			return StepActionResult(StepActionResultStatus.REPEAT)
		} finally {
			dispatcher.safeDeliver(DestroyChannel().apply { slotHandle = givenSlotHandle })
		}
	}

	/**
	 * Verify the input of the user (e.g. no empty mandatory fields, pin length, allowed charset).
	 *
	 * @param executionResults The results containing the OutputInfoUnits of interest.
	 * @return True if the input of the user could be verified, else false
	 */
	private fun verifyUserInput(executionResults: ExecutionResults?): Boolean {
		// TODO: check pin length and possibly allowed charset with CardInfo file
		return (executionResults?.getResult(UnblockPINDialog.Companion.PUK_FIELD) as PasswordField?).strOrEmpty().isNotEmpty()
	}

	companion object {
		private const val PIN_ID_PUK = "4"
	}
}
