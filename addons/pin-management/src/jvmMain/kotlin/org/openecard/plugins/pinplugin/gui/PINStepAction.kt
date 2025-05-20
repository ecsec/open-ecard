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
import iso.std.iso_iec._24727.tech.schema.ControlIFD
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.apdu.ResetRetryCounter
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.ifd.anytype.PACEInputType
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.StringUtils
import org.openecard.gui.StepResult
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.i18n.I18N
import org.openecard.ifd.scio.IFDException
import org.openecard.ifd.scio.reader.PCSCFeatures
import org.openecard.ifd.scio.reader.PCSCPinModify
import org.openecard.plugins.pinplugin.RecognizedState
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import javax.xml.parsers.ParserConfigurationException

private val logger = KotlinLogging.logger { }

/**
 * Create a new instance of PINStepAction.
 *
 * @param capturePin True if the PIN has to be captured by software else false
 * @param conHandle The unique ConnectionHandle for the card connection
 * @param step the step this action belongs to
 * @param dispatcher The Dispatcher to use
 * @param retryCounter RetryCounter of the PIN
 *
 * StepAction for performing PACE with the PIN and modify it.
 * <br></br> This StepAction tries to perform PACE with the PIN as often as possible in dependence of the retry counter.
 * <br></br> If PACE was executed successful the PIN is modified.
 * <br></br> If the retry counter reaches 1 the CANEntryStep will be shown.
 *
 * @author Dirk Petrautzki
 */
class PINStepAction(
	private val capturePin: Boolean,
	private val conHandle: ConnectionHandleType,
	private val dispatcher: Dispatcher,
	step: Step,
	private var retryCounter: Int,
) : StepAction(step) {
	private var oldPIN: String? = null
	private var newPIN: ByteArray? = null
	private var newPINRepeat: ByteArray? = null

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
				// let the user enter the pin again, when input verification failed
				return StepActionResult(
					StepActionResultStatus.REPEAT,
					createPINReplacementStep(
						enteredWrong = false,
						verifyFailed = true,
					),
				)
			} else {
				paceInputMap.addElement(PACEInputType.PIN, oldPIN)
			}
		}
		paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PIN)
		paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")

		// perform PACE by EstablishChannel
		val establishChannel =
			EstablishChannel().apply {
				slotHandle = conHandle.getSlotHandle()
				authenticationProtocolData = paceInputMap.response
				authenticationProtocolData.protocol = ECardConstants.Protocol.PACE
			}

		try {
			val establishChannelResponse =
				dispatcher.safeDeliver(establishChannel) as EstablishChannelResponse
			checkResult<EstablishChannelResponse>(establishChannelResponse)
			// PACE completed successfully, we now modify the pin
			if (capturePin) {
				sendResetRetryCounter()
			} else {
				sendModifyPIN()
			}
			// PIN modified successfully, proceed with next step
			return StepActionResult(StepActionResultStatus.NEXT)
		} catch (ex: WSHelper.WSException) {
			if (capturePin) {
				retryCounter--
				logger.info { "Wrong PIN entered, trying again (remaining tries: $retryCounter)." }
				return if (retryCounter == 1) {
					StepActionResult(StepActionResultStatus.BACK, createCANReplacementStep())
				} else {
					StepActionResult(
						StepActionResultStatus.REPEAT,
						createPINReplacementStep(enteredWrong = true, verifyFailed = false),
					)
				}
			} else {
				logger.warn { "PIN not entered successfully in terminal." }
				return StepActionResult(StepActionResultStatus.CANCEL)
			}
		} catch (ex: APDUException) {
			logger.error(ex) { "Failed to transmit Reset Retry Counter APDU." }
			return StepActionResult(StepActionResultStatus.CANCEL)
		} catch (ex: IllegalArgumentException) {
			logger.error(ex) { "Failed to transmit Reset Retry Counter APDU." }
			return StepActionResult(StepActionResultStatus.CANCEL)
		} catch (ex: IFDException) {
			logger.error(ex) { "Failed to transmit Reset Retry Counter APDU." }
			return StepActionResult(StepActionResultStatus.CANCEL)
		}
	}

	/**
	 * Create the step that asks the user to insert the CAN.
	 *
	 * @return Step for CAN entry
	 */
	private fun createCANReplacementStep() =
		CANEntryStep(
			id = "can-entry",
			I18N.strings.pinplugin_action_changepin_userconsent_canstep_title.localized(),
			capturePin,
			state = RecognizedState.PIN_SUSPENDED,
			enteredWrong = false,
			verifyFailed = false,
		).apply {
			action = CANStepAction(capturePin, conHandle, dispatcher, this, RecognizedState.PIN_SUSPENDED)
		}

	/**
	 * Send a ModifyPIN-PCSC-Command to the Terminal.
	 *
	 * @throws IFDException If building the Command fails.
	 */
	@Throws(IFDException::class)
	private fun sendModifyPIN() {
		val pwdAttr: PasswordAttributesType =
			create(true, PasswordTypeType.ASCII_NUMERIC, 6, 6, 6).apply {
				padChar = byteArrayOf(0x3F.toByte())
			}
		val ctrlStruct = PCSCPinModify(pwdAttr, StringUtils.toByteArray("002C0203"))
		val controlIFD =
			ControlIFD().apply {
				command = ByteUtils.concatenate(PCSCFeatures.MODIFY_PIN_DIRECT.toByte(), ctrlStruct.toBytes())
				slotHandle = conHandle.slotHandle
			}
		dispatcher.safeDeliver(controlIFD)
	}

	/**
	 * Send a ResetRetryCounter-APDU.
	 *
	 * @throws APDUException if the RRC-APDU could not be sent successfully
	 */
	@Throws(APDUException::class)
	private fun sendResetRetryCounter() {
		ResetRetryCounter(newPIN, 0x03.toByte())
			.transmit(dispatcher, conHandle.getSlotHandle())
	}

	/**
	 * Verify the input of the user (e.g. no empty mandatory fields, pin length, allowed charset).
	 *
	 * @param executionResults The results containing the OutputInfoUnits of interest.
	 * @return True if the input of the user could be verified, else false.
	 */
	private fun verifyUserInput(executionResults: ExecutionResults?): Boolean {
		// TODO: check pin length and possibly allowed charset with CardInfo file

		val fieldOldPIN = executionResults?.getResult(ChangePINStep.OLD_PIN_FIELD) as? PasswordField
		val fieldNewPIN = executionResults?.getResult(ChangePINStep.NEW_PIN_FIELD) as? PasswordField
		val fieldNewPINRepeat = executionResults?.getResult(ChangePINStep.Companion.NEW_PIN_REPEAT_FIELD) as? PasswordField

		oldPIN = fieldOldPIN.strOrEmpty()

		if (oldPIN?.isEmpty() == true) {
			return false
		}
		if (fieldNewPIN.strOrEmpty().isEmpty()) {
			return false
		} else {
			try {
				newPIN = fieldNewPIN.strOrEmpty().toByteArray(charset(ISO_8859_1))
				if (newPIN == null) {
					return false
				}
			} catch (e: UnsupportedEncodingException) {
				return false
			}
		}
		if (fieldNewPINRepeat.strOrEmpty().isEmpty()) {
			return false
		} else {
			try {
				newPINRepeat = fieldNewPINRepeat.strOrEmpty().toByteArray(charset(ISO_8859_1))
				if (newPINRepeat == null) {
					return false
				}
			} catch (e: UnsupportedEncodingException) {
				return false
			}
		}

		return ByteUtils.compare(newPIN, newPINRepeat)
	}

	/**
	 * Create the step that asks the user to insert the old and new pins.
	 *
	 * @return Step for PIN entry
	 */
	private fun createPINReplacementStep(
		enteredWrong: Boolean,
		verifyFailed: Boolean,
	) = ChangePINStep(
		id = "pin-entry",
		I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized(),
		capturePin,
		retryCounter,
		enteredWrong,
		verifyFailed,
	).apply {
		action = PINStepAction(capturePin, conHandle, dispatcher, this, retryCounter)
	}

	companion object {
		// translation constants
		private const val PINSTEP_TITLE = "action.changepin.userconsent.pinstep.title"
		private const val CANSTEP_TITLE = "action.changepin.userconsent.canstep.title"

		private const val ISO_8859_1 = "ISO-8859-1"
		private const val PIN_ID_PIN = "3"

		private fun create(
			needsPadding: Boolean,
			givenPwdType: PasswordTypeType?,
			minLen: Int,
			storedLen: Int,
			maxLen: Int,
		) = PasswordAttributesType().apply {
			minLength = BigInteger.valueOf(minLen.toLong())
			storedLength = BigInteger.valueOf(storedLen.toLong())
			pwdType = givenPwdType

			if (needsPadding) {
				pwdFlags.add("needs-padding")
			}
			maxLength = BigInteger.valueOf(maxLen.toLong())
		}
	}
}
