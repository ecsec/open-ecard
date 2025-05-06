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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
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
import org.openecard.plugins.pinplugin.RecognizedState
import javax.xml.parsers.ParserConfigurationException

private val logger = KotlinLogging.logger { }

/**
 * StepAction for performing PACE with the CAN.
 * <br></br> If PACE fails the Step for entering CAN will be shown again.
 * <br></br> If PACE succeeds the Step for PIN changing will be shown.
 *
 * @author Dirk Petrautzki
 * @author Tobias WIch
 *
 * Creates a new instance of CANStepAction.
 *
 * @param capturePin True if the PIN has to be captured by software else false.
 * @param conHandle ConnectionHandle identifying the connection to the card.
 * @param dispatcher The Dispatcher to use.
 * @param step Step this Action belongs to.
 * @param state The current state of the PIN.
 */
class CANStepAction(
	private val capturePin: Boolean,
	private val conHandle: ConnectionHandleType,
	private val dispatcher: Dispatcher,
	step: Step,
	private val state: RecognizedState,
) : StepAction(step) {
	private val lang: I18n = I18n.getTranslation("pinplugin")

	private var can: String? = null

	override fun perform(
		oldResults: MutableMap<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		if (result.isBack()) {
			return StepActionResult(StepActionResultStatus.BACK)
		}
		if (state != RecognizedState.PIN_SUSPENDED) {
			return StepActionResult(StepActionResultStatus.NEXT)
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
			val executionResults: ExecutionResults? = oldResults[stepID]

			if (!verifyUserInput(executionResults)) {
				// let the user enter the can again, when input verification failed
				return StepActionResult(
					StepActionResultStatus.REPEAT,
					createReplacementStep(
						enteredWrong = false,
						verifyFailed = true,
					),
				)
			} else {
				paceInputMap.addElement(PACEInputType.PIN, can)
			}
		}
		paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_CAN)
		paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")

		// perform PACE by EstablishChannelCommand
		val establishChannel =
			EstablishChannel().apply {
				slotHandle = conHandle.slotHandle
				authenticationProtocolData = paceInputMap.response
				authenticationProtocolData.protocol = ECardConstants.Protocol.PACE
			}

		try {
			val ecr = dispatcher.safeDeliver(establishChannel) as EstablishChannelResponse
			checkResult<EstablishChannelResponse>(ecr)

			// pace was successfully performed, so get to the next step
			val title: String? = lang.translationForKey(PINSTEP_TITLE)
			val retryCounter = 1
			val replacementStep: Step =
				ChangePINStep(
					"pin-entry",
					title,
					capturePin,
					retryCounter,
					enteredWrong = false,
					verifyFailed = false,
				)
			val pinAction: StepAction = PINStepAction(capturePin, conHandle, dispatcher, replacementStep, retryCounter)
			replacementStep.setAction(pinAction)
			return StepActionResult(StepActionResultStatus.NEXT, replacementStep)
		} catch (ex: WSHelper.WSException) {
			logger.info { "Wrong CAN entered, trying again" }
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				createReplacementStep(
					enteredWrong = true,
					verifyFailed = false,
				),
			)
		}
	}

	/**
	 * Verify the input of the user (e.g. no empty mandatory fields, pin length, allowed charset).
	 *
	 * @param executionResults The results containing the OutputInfoUnits of interest.
	 * @return True if the input of the user could be verified, else false.
	 */
	private fun verifyUserInput(executionResults: ExecutionResults?): Boolean {
		// TODO: check pin length and possibly allowed charset with CardInfo file
		return (executionResults?.getResult(CANEntryStep.Companion.CAN_FIELD) as PasswordField?).strOrEmpty().length == 6
	}

	/**
	 * Create the step that asks the user to insert the CAN.
	 *
	 * @return Step for CAN entry
	 */
	private fun createReplacementStep(
		enteredWrong: Boolean,
		verifyFailed: Boolean,
	) = CANEntryStep(
		"can-entry",
		lang.translationForKey(CANSTEP_TITLE),
		capturePin,
		state,
		enteredWrong,
		verifyFailed,
	).apply {
		setAction(CANStepAction(capturePin, conHandle, dispatcher, this, state) as StepAction)
	}

	companion object {
		// translation constants
		private const val CANSTEP_TITLE = "action.changepin.userconsent.canstep.title"
		private const val PINSTEP_TITLE = "action.changepin.userconsent.pinstep.title"

		private const val PIN_ID_CAN = "2"
	}
}
