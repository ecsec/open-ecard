/****************************************************************************
 * Copyright (C) 2014-2025 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ControlIFD
import iso.std.iso_iec._24727.tech.schema.ControlIFDResponse
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.apdu.ResetRetryCounter
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.ifd.anytype.PACEInputType
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.Promise
import org.openecard.common.util.StringUtils
import org.openecard.gui.StepResult
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.ifd.scio.IFDException
import org.openecard.ifd.scio.reader.PCSCFeatures
import org.openecard.ifd.scio.reader.PCSCPinModify
import org.openecard.plugins.pinplugin.CardCapturer
import org.openecard.plugins.pinplugin.CardStateView
import org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction
import org.openecard.plugins.pinplugin.RecognizedState
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import javax.xml.parsers.ParserConfigurationException

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class GenericPINAction(
	stepID: String,
	private val dispatcher: Dispatcher,
	private val gPINStep: GenericPINStep,
	private val cardCapturer: CardCapturer,
	private val errorPromise: Promise<Throwable?>,
) : StepAction(gPINStep) {
	private val lang: I18n = I18n.getTranslation("pinplugin")

	private val cardView: CardStateView = cardCapturer.aquireView()

	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		if (result.isCancelled()) {
			return StepActionResult(StepActionResultStatus.CANCEL)
		}

		try {
			cardCapturer.updateCardState()
		} catch (ex: WSHelper.WSException) {
			storeTerminationError(ex)
			logger.error(ex) { "Failed to prepare Generic PIN step." }
			return StepActionResult(StepActionResultStatus.CANCEL)
		}
		// clean up values
		clearCorrectValues()

		when (cardView.pinState) {
			RecognizedState.PIN_ACTIVATED_RC3, RecognizedState.PIN_ACTIVATED_RC2 -> return performPINChange(oldResults)
			RecognizedState.PIN_SUSPENDED -> return performResumePIN(oldResults)
			RecognizedState.PIN_RESUMED -> return performPINChange(oldResults)
			RecognizedState.PIN_BLOCKED -> return performUnblockPIN(oldResults)

			// nothing todo here the error message was displayed so just return next.
			RecognizedState.PIN_DEACTIVATED, RecognizedState.PUK_BLOCKED ->
				return StepActionResult(StepActionResultStatus.NEXT)

			RecognizedState.UNKNOWN -> {
				gPINStep.updateState(RecognizedState.UNKNOWN)
				gPINStep.setFailedPUKVerify(wrongFormat = false, failedVerify = true)
				gPINStep.setFailedCANVerify(wrongFormat = false, failedVerify = true)
				gPINStep.setFailedPINVerify(wrongFormat = false, failedVerify = true)
				return StepActionResult(StepActionResultStatus.REPEAT)
			}
		}
	}

	@Throws(ParserConfigurationException::class)
	private fun performPACEWithPIN(oldResults: Map<String, ExecutionResults>): EstablishChannelResponse? {
		val paceInput =
			DIDAuthenticationDataType().apply {
				protocol = ECardConstants.Protocol.PACE
			}
		val tmp = AuthDataMap(paceInput)

		val paceInputMap: AuthDataResponse<*> = tmp.createResponse<DIDAuthenticationDataType?>(paceInput)
		if (this.cardView.capturePin()) {
			val executionResults = oldResults[stepID]
			val oldPINField = executionResults?.getResult(GenericPINStep.OLD_PIN_FIELD) as PasswordField?
			if (oldPINField == null) {
				return null
			}
			val oldPINValue = oldPINField.value

			if (oldPINValue.size > 6 || oldPINValue.size < 5) {
				// let the user enter the can again, when input verification failed
				return null
			} else {
				paceInputMap.addElement(PACEInputType.PIN, String(oldPINValue))
			}
		}
		paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PIN)
		paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")

		// perform PACE by EstablishChannelCommand
		val eChannel = createEstablishChannelStructure(paceInputMap)
		return dispatcher.safeDeliver(eChannel) as EstablishChannelResponse
	}

	@Throws(ParserConfigurationException::class)
	private fun performPACEWithCAN(oldResults: Map<String, ExecutionResults>): EstablishChannelResponse? {
		val paceInput =
			DIDAuthenticationDataType().apply {
				protocol = ECardConstants.Protocol.PACE
			}
		val tmp = AuthDataMap(paceInput)

		val paceInputMap: AuthDataResponse<*> = tmp.createResponse<DIDAuthenticationDataType?>(paceInput)
		if (this.cardView.capturePin()) {
			val executionResults = oldResults[stepID]
			val canField = executionResults?.getResult(GenericPINStep.CAN_FIELD) as? PasswordField
			if (canField == null) {
				return null
			}
			val canValue = String(canField.value)

			if (canValue.length != 6) {
				// let the user enter the can again, when input verification failed
				return null
			} else {
				paceInputMap.addElement(PACEInputType.PIN, canValue)
			}
		}
		paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_CAN)
		paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")

		// perform PACE by EstablishChannelCommand
		val eChannel = createEstablishChannelStructure(paceInputMap)
		return dispatcher.safeDeliver(eChannel) as EstablishChannelResponse
	}

	@Throws(ParserConfigurationException::class)
	private fun performPACEWithPUK(oldResults: Map<String, ExecutionResults>): EstablishChannelResponse? {
		val paceInput =
			DIDAuthenticationDataType().apply {
				protocol = ECardConstants.Protocol.PACE
			}
		val tmp = AuthDataMap(paceInput)

		val paceInputMap: AuthDataResponse<*> = tmp.createResponse<DIDAuthenticationDataType?>(paceInput)
		if (this.cardView.capturePin()) {
			val executionResults = oldResults[stepID]
			val pukField = executionResults?.getResult(GenericPINStep.PUK_FIELD) as PasswordField?
			if (pukField == null) {
				return null
			}
			val pukValue = String(pukField.value)

			if (pukValue.length != 10) {
				// let the user enter the pin again, when there is none entered
				// TODO inform user that something with his input is wrong
				return null
			} else {
				paceInputMap.addElement(PACEInputType.PIN, pukValue)
			}
		}

		paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_PUK)
		paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")

		val eChannel = createEstablishChannelStructure(paceInputMap)
		return dispatcher.safeDeliver(eChannel) as EstablishChannelResponse
	}

	private fun createEstablishChannelStructure(paceInputMap: AuthDataResponse<*>) =
		EstablishChannel().apply {
			slotHandle = cardView.handle.slotHandle
			authenticationProtocolData = paceInputMap.response
			authenticationProtocolData.protocol = ECardConstants.Protocol.PACE
		}

	private fun performPINChange(oldResults: Map<String, ExecutionResults>): StepActionResult {
		var newPinValue: String? = null
		var newPINRepeatValue: String? = null

		if (cardView.capturePin()) {
			try {
				val executionResults = oldResults[stepID]
				val newPINField = executionResults?.getResult(GenericPINStep.NEW_PIN_FIELD) as PasswordField?
				val newPINRepeatField =
					executionResults?.getResult(GenericPINStep.NEW_PIN_REPEAT_FIELD) as PasswordField?
				if (newPINField == null || newPINRepeatField == null) {
					logger.warn { "Expected pin fields were incomplete." }
					gPINStep.updateState(cardView.pinState) // to reset the text fields
					gPINStep.setFailedPINVerify(wrongFormat = true, failedVerify = false)
					return StepActionResult(StepActionResultStatus.REPEAT)
				}
				newPinValue = String(newPINField.value)
				newPINRepeatValue = String(newPINRepeatField.value)

				val pin1 = newPinValue.toByteArray(charset(ISO_8859_1))
				val pin2 = newPINRepeatValue.toByteArray(charset(ISO_8859_1))

				if (!ByteUtils.compare(pin1, pin2)) {
					logger.warn { "New PIN does not match the value from the confirmation field." }
					gPINStep.updateState(cardView.pinState) // to reset the text fields
					return StepActionResult(StepActionResultStatus.REPEAT)
				}
			} catch (ex: UnsupportedEncodingException) {
				logger.error(ex) { "ISO_8859_1 charset is not support." }
				gPINStep.updateState(cardView.pinState) // to reset the text fields
				gPINStep.setFailedPINVerify(wrongFormat = true, failedVerify = false)
				return StepActionResult(StepActionResultStatus.REPEAT)
			}
		}

		try {
			val pinResponse = performPACEWithPIN(oldResults)
			if (pinResponse == null) {
				// the entered pin has a wrong format repeat the entering of the data
				gPINStep.setFailedPINVerify(wrongFormat = true, failedVerify = false)
				return StepActionResult(StepActionResultStatus.REPEAT)
			}

			if (pinResponse.result.resultMajor == ECardConstants.Major.ERROR) {
				when (pinResponse.result.resultMinor) {
					ECardConstants.Minor.IFD.PASSWORD_ERROR -> {
						gPINStep.setFailedPINVerify(wrongFormat = false, failedVerify = true)
						gPINStep.updateState(RecognizedState.PIN_ACTIVATED_RC2)
						return StepActionResult(StepActionResultStatus.REPEAT)
					}

					ECardConstants.Minor.IFD.PASSWORD_SUSPENDED -> {
						gPINStep.setFailedPINVerify(wrongFormat = false, failedVerify = true)
						gPINStep.updateState(RecognizedState.PIN_SUSPENDED)
						return StepActionResult(StepActionResultStatus.REPEAT)
					}

					ECardConstants.Minor.IFD.PASSWORD_BLOCKED -> {
						gPINStep.setFailedPINVerify(wrongFormat = false, failedVerify = true)
						gPINStep.updateState(RecognizedState.PIN_BLOCKED)
						return StepActionResult(StepActionResultStatus.REPEAT)
					}

					else -> checkResult<EstablishChannelResponse>(pinResponse)
				}
			}

			if (this.cardView.capturePin()) {
				// pace with the old pin was successful now modify the pin
				if (newPinValue == newPINRepeatValue && newPinValue?.length == 6) {
					// no result check necessary everything except a 9000 leads to an APDU exception
					sendResetRetryCounter(newPinValue.toByteArray(charset(ISO_8859_1)))
				}
			} else {
				val resp = sendModifyPIN()
				evaluateControlIFDResponse(resp)
			}

			gPINStep.setFailedPINVerify(wrongFormat = false, failedVerify = false)
			gPINStep.updateState(RecognizedState.PIN_ACTIVATED_RC3)
			// PIN modified successfully, proceed with next step
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				generateSuccessStep(lang.translationForKey(CHANGE_SUCCESS)),
			)
		} catch (ex: APDUException) {
			logger.error(ex) { "An internal error occurred while trying to change the PIN" }
			storeTerminationError(ex)
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				generateErrorStep(lang.translationForKey(ERROR_INTERNAL)),
			)
		} catch (ex: IFDException) {
			logger.error(ex) { "An internal error occurred while trying to change the PIN" }
			storeTerminationError(ex)
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				generateErrorStep(lang.translationForKey(ERROR_INTERNAL)),
			)
		} catch (ex: ParserConfigurationException) {
			logger.error(ex) { "An internal error occurred while trying to change the PIN" }
			storeTerminationError(ex)
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				generateErrorStep(lang.translationForKey(ERROR_INTERNAL)),
			)
		} catch (ex: UnsupportedEncodingException) {
			logger.warn(ex) { "The encoding of the PIN is wrong." }
			gPINStep.setFailedPINVerify(wrongFormat = true, failedVerify = false)
			return StepActionResult(StepActionResultStatus.REPEAT)
		} catch (ex: WSHelper.WSException) {
			storeTerminationError(ex)
			when (ex.resultMinor) {
				// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
				ECardConstants.Minor.IFD.CANCELLATION_BY_USER -> {
					logger.error(ex) { "User canceled the authentication manually or removed the card." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_USER_CANCELLATION_OR_CARD_REMOVED)),
					)
				}
				ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE -> {
					logger.error(ex) { "The SlotHandle was invalid so probably the user removed the card or an reset occurred." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)),
					)
				}
				// for users which forgot to type in something
				ECardConstants.Minor.IFD.TIMEOUT_ERROR -> {
					logger.error(ex) { "The terminal timed out no password was entered." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_TIMEOUT)),
					)
				}

				// the verification of the new pin failed
				ECardConstants.Minor.IFD.PASSWORDS_DONT_MATCH -> {
					logger.error(ex) { "The verification of the new PIN failed." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_NON_MATCHING_PASSWORDS)),
					)
				}
				else -> {
					// We don't know what happened so just show a general error message
					logger.error(ex) { "An unknown error occurred while trying to change the PIN." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_UNKNOWN)),
					)
				}
			}
		}
	}

	private fun storeTerminationError(ex: Throwable?) {
		try {
			errorPromise.deliver(ex)
		} catch (ex: IllegalStateException) {
			logger.error(ex) { "Cannot re-deliver error" }
		}
	}

	private fun performResumePIN(oldResults: Map<String, ExecutionResults>): StepActionResult {
		try {
			val canResponse = performPACEWithCAN(oldResults)

			if (canResponse == null) {
				gPINStep.setFailedCANVerify(wrongFormat = true, failedVerify = false)
				gPINStep.updateState(cardView.pinState) // to reset the text fields
				return StepActionResult(StepActionResultStatus.REPEAT)
			}

			if (canResponse.result.resultMajor == ECardConstants.Major.ERROR) {
				if (canResponse.result.resultMinor == ECardConstants.Minor.IFD.AUTHENTICATION_FAILED) {
					gPINStep.setFailedCANVerify(wrongFormat = false, failedVerify = true)
					gPINStep.updateState(this.cardView.pinState) // to reset the text fields
					return StepActionResult(StepActionResultStatus.REPEAT)
				} else {
					checkResult<EstablishChannelResponse>(canResponse)
				}
			}

			gPINStep.setFailedCANVerify(wrongFormat = false, failedVerify = false)
			gPINStep.updateState(RecognizedState.PIN_RESUMED)
			return StepActionResult(StepActionResultStatus.REPEAT)
		} catch (ex: ParserConfigurationException) {
			storeTerminationError(ex)
			logger.error(ex) { "An internal error occurred while trying to resume the PIN." }
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				generateErrorStep(lang.translationForKey(ERROR_INTERNAL)),
			)
		} catch (ex: WSHelper.WSException) {
			this.storeTerminationError(ex)
			when (ex.resultMinor) {
				// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
				ECardConstants.Minor.IFD.CANCELLATION_BY_USER -> {
					logger.error(ex) { "User canceled the authentication manually or removed the card." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_USER_CANCELLATION_OR_CARD_REMOVED)),
					)
				}

				// for people which think they have to remove the card in the process
				ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE -> {
					logger.error { "The SlotHandle was invalid so probably the user removed the card or an reset occurred." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)),
					)
				}

				// for users which forgot to type in something
				ECardConstants.Minor.IFD.TIMEOUT_ERROR -> {
					logger.error(ex) { "The terminal timed out no password was entered." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_TIMEOUT)),
					)
				}

				else -> {
					logger.error(ex) { "An unknown error occurred while trying to verify the CAN." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_UNKNOWN)),
					)
				}
			}
		}
	}

	private fun performUnblockPIN(oldResults: Map<String, ExecutionResults>): StepActionResult {
		try {
			val pukResponse = performPACEWithPUK(oldResults)

			if (pukResponse == null) {
				gPINStep.setFailedPUKVerify(wrongFormat = true, failedVerify = false)
				gPINStep.updateState(this.cardView.pinState) // to reset the text fields
				return StepActionResult(StepActionResultStatus.REPEAT)
			}

			if (pukResponse.result.resultMajor == ECardConstants.Major.ERROR) {
				if (pukResponse.result.resultMinor == ECardConstants.Minor.IFD.AUTHENTICATION_FAILED) {
					// i think we should not display the counter
					// gPINStep.decreasePUKCounter();
					gPINStep.setFailedPUKVerify(wrongFormat = false, failedVerify = true)
					gPINStep.updateState(cardView.pinState) // to reset the text fields
					return StepActionResult(StepActionResultStatus.REPEAT)
				} else {
					checkResult<EstablishChannelResponse>(pukResponse)
				}
			}

			// Here no exception is thrown so sent the ResetRetryCounter command
			val resetRetryCounter = ResetRetryCounter(0x03.toByte())
			val responses: MutableList<ByteArray> =
				mutableListOf(
					byteArrayOf(0x90.toByte(), 0x00.toByte()),
					byteArrayOf(0x69.toByte(), 0x84.toByte()),
				)

			val resetCounterResponse =
				resetRetryCounter.transmit(
					dispatcher,
					cardView.handle.slotHandle,
					responses,
				)
			if (resetCounterResponse.trailer.contentEquals(byteArrayOf(0x69.toByte(), 0x84.toByte()))) {
				gPINStep.updateState(RecognizedState.PUK_BLOCKED)
				gPINStep.setFailedPUKVerify(wrongFormat = false, failedVerify = true)
				return StepActionResult(StepActionResultStatus.REPEAT)
			} else if (resetCounterResponse.trailer.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
				gPINStep.updateState(RecognizedState.PIN_ACTIVATED_RC3)
				gPINStep.setFailedPUKVerify(wrongFormat = false, failedVerify = false)
				return StepActionResult(
					StepActionResultStatus.REPEAT,
					generateSuccessStep(lang.translationForKey(PUK_SUCCESS)),
				)
			} else {
				gPINStep.updateState(RecognizedState.UNKNOWN)
				gPINStep.setFailedPUKVerify(wrongFormat = false, failedVerify = true)
				return StepActionResult(StepActionResultStatus.REPEAT)
			}
		} catch (ex: APDUException) {
			storeTerminationError(ex)
			logger.error(ex) { "An internal error occurred while trying to unblock the PIN." }
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				generateErrorStep(lang.translationForKey(ERROR_INTERNAL)),
			)
		} catch (ex: ParserConfigurationException) {
			storeTerminationError(ex)
			logger.error(ex) { "An internal error occurred while trying to unblock the PIN." }
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				generateErrorStep(lang.translationForKey(ERROR_INTERNAL)),
			)
		} catch (ex: WSHelper.WSException) {
			this.storeTerminationError(ex)
			when (ex.resultMinor) {
				// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
				ECardConstants.Minor.IFD.CANCELLATION_BY_USER -> {
					logger.error(ex) { "User canceled the authentication manually or removed the card." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_USER_CANCELLATION_OR_CARD_REMOVED)),
					)
				}

				// for users which forgot to type in something
				ECardConstants.Minor.IFD.TIMEOUT_ERROR -> {
					logger.error(ex) { "The terminal timed out no password was entered." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_TIMEOUT)),
					)
				}

				// for people which think they have to remove the card in the process
				ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE -> {
					logger.error(ex) { "The SlotHandle was invalid so probably the user removed the card or an reset occurred." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_CARD_REMOVED)),
					)
				}

				// We don't know what happend so just show an general error message
				else -> {
					logger.error(ex) { "An unknown error occurred while trying to verify the PUK." }
					return StepActionResult(
						StepActionResultStatus.REPEAT,
						generateErrorStep(lang.translationForKey(ERROR_UNKNOWN)),
					)
				}
			}
		}
	}

	/**
	 * Send a ModifyPIN-PCSC-Command to the Terminal.
	 *
	 * @throws IFDException If building the Command fails.
	 */
	@Throws(IFDException::class)
	private fun sendModifyPIN(): ControlIFDResponse {
		val pwdAttr: PasswordAttributesType =
			create(true, PasswordTypeType.ASCII_NUMERIC, 6, 6, 6).apply {
				padChar = byteArrayOf(0x3F.toByte())
			}
		val ctrlStruct = PCSCPinModify(pwdAttr, StringUtils.toByteArray("002C0203"))

		val controlIFD =
			ControlIFD().apply {
				setCommand(ByteUtils.concatenate(PCSCFeatures.MODIFY_PIN_DIRECT.toByte(), ctrlStruct.toBytes()))
				setSlotHandle(cardView.handle.slotHandle)
			}
		return dispatcher.safeDeliver(controlIFD) as ControlIFDResponse
	}

	/**
	 * Send a ResetRetryCounter-APDU.
	 *
	 * @throws APDUException if the RRC-APDU could not be sent successfully
	 */
	@Throws(APDUException::class)
	private fun sendResetRetryCounter(newPIN: ByteArray?): CardResponseAPDU? =
		ResetRetryCounter(newPIN, 0x03.toByte()).transmit(dispatcher, cardView.handle.slotHandle)

	private fun generateSuccessStep(successMessage: String) =
		Step("success", lang.translationForKey(SUCCESS_TITLE)).apply {
			isReversible = false
			inputInfoUnits.add(
				Text(successMessage),
			)
		}

	private fun generateErrorStep(errorMessage: String) =
		Step(ERROR_STEP_ID, lang.translationForKey(ERROR_TITLE)).apply {
			isReversible = false
			inputInfoUnits.add(
				Text(errorMessage),
			)
		}

	@Throws(WSHelper.WSException::class)
	private fun evaluateControlIFDResponse(response: ControlIFDResponse) {
		val resp = response.getResponse()
		when (ByteUtils.toInteger(resp)) {
			0x64A1 ->
				response.setResult(
					makeResultError(
						ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE,
						"Card was removed.",
					),
				)

			0x6402 ->
				response.setResult(
					makeResultError(
						ECardConstants.Minor.IFD.PASSWORDS_DONT_MATCH,
						"The entered passwords do not match.",
					),
				)

			0x6401 ->
				response.setResult(
					makeResultError(
						ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
						"The user aborted the password entry.",
					),
				)
		}

		checkResult<ControlIFDResponse>(response)
	}

	private fun clearCorrectValues() {
		DynamicContext.getInstance(GetCardsAndPINStatusAction.DYNCTX_INSTANCE_KEY)!!.apply {
			remove(GetCardsAndPINStatusAction.PIN_CORRECT)
			remove(GetCardsAndPINStatusAction.CAN_CORRECT)
			remove(GetCardsAndPINStatusAction.PUK_CORRECT)
		}
	}

	companion object {
		const val ERROR_STEP_ID: String = "error"
		private const val PIN_ID_CAN = "2"
		private const val PIN_ID_PIN = "3"
		private const val PIN_ID_PUK = "4"
		private const val ISO_8859_1 = "ISO-8859-1"

		// Translation constants
		private const val PUK_SUCCESS = "action.unblockpin.userconsent.pukstep.puk_success"
		private const val CHANGE_SUCCESS = "action.changepin.userconsent.successstep.description"
		private const val ERROR_CARD_REMOVED = "action.error.card.removed"
		private const val ERROR_INTERNAL = "action.error.internal"
		private const val ERROR_NON_MATCHING_PASSWORDS = "action.error.missing_password_match"
		private const val ERROR_TIMEOUT = "action.error.timeout"
		private const val ERROR_TITLE = "action.error.title"
		private const val ERROR_USER_CANCELLATION_OR_CARD_REMOVED = "action.error.user_cancellation"
		private const val SUCCESS_TITLE = "action.success.title"
		private const val ERROR_UNKNOWN = "action.error.unknown"

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
