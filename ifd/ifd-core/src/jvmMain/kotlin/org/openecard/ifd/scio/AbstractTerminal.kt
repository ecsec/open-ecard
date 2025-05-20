/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
package org.openecard.ifd.scio

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.AltVUMessagesType
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType
import iso.std.iso_iec._24727.tech.schema.OutputInfoType
import iso.std.iso_iec._24727.tech.schema.Transmit
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import iso.std.iso_iec._24727.tech.schema.VerifyUser
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.WSHelper.makeResultOK
import org.openecard.common.WSHelper.makeResultUnknownError
import org.openecard.common.apdu.common.CardCommandStatus
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.util.PINUtils
import org.openecard.common.util.UtilException
import org.openecard.gui.ResultStatus
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.gui.executor.StepAction
import org.openecard.i18n.I18N
import org.openecard.ifd.scio.wrapper.ChannelManager
import org.openecard.ifd.scio.wrapper.SingleThreadChannel
import org.openecard.ifd.scio.wrapper.TerminalInfo
import java.math.BigInteger

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
internal class AbstractTerminal(
	private val ifd: IFD,
	private val cm: ChannelManager,
	private val channel: SingleThreadChannel,
	private val gui: UserConsent?,
	private val ctxHandle: ByteArray?,
	private val displayIdx: BigInteger?,
) {
	private val terminalInfo: TerminalInfo = TerminalInfo(cm, channel)

	private var capabilities: IFDCapabilitiesType? = null
	private var canBeep: Boolean? = null
	private var canBlink: Boolean? = null
	private var canDisplay: Boolean? = null
	private var canEnter: Boolean? = null
	private val keyIdx: BigInteger? = null

	@Throws(IFDException::class)
	fun output(
		ifdName: String?,
		outInfo: OutputInfoType,
	) {
		getCapabilities()

		// extract values from outInfo for convenience
		var didx = outInfo.getDisplayIndex()
		if (didx == null) {
			didx = BigInteger.valueOf(0)
		}
		val msg = outInfo.getMessage()
		val timeout = outInfo.getTimeout()
		var acoustic = outInfo.isAcousticalSignal
		if (acoustic == null) {
			acoustic = java.lang.Boolean.FALSE
		}
		var optic = outInfo.isOpticalSignal
		if (optic == null) {
			optic = java.lang.Boolean.FALSE
		}

		if (acoustic) {
			if (canBeep() || this.isVirtual) {
				beep()
			} else {
				val ex = IFDException("No device to output a beep available.")
				LOG.warn(ex) { ex.message }
				throw ex
			}
		}
		if (optic) {
			if (canBlink() || this.isVirtual) {
				blink()
			} else {
				val ex = IFDException("No device to output a blink available.")
				LOG.warn(ex) { ex.message }
				throw ex
			}
		}
		if (msg != null) {
			if (canDisplay() || this.isVirtual) {
				display(msg, timeout)
			} else {
				val ex = IFDException("No device to output a message available.")
				LOG.warn(ex) { ex.message }
				throw ex
			}
		}
	}

	@Throws(SCIOException::class, IFDException::class, InterruptedException::class)
	fun verifyUser(verify: VerifyUser): VerifyUserResponse {
		val handle = verify.getSlotHandle()
		// get capabilities
		getCapabilities()

		// check if is possible to perform PinCompare protocol
		val protoList = this.capabilities!!.getSlotCapability()[0].getProtocol()
		if (!protoList.contains(ECardConstants.Protocol.PIN_COMPARE)) {
			throw IFDException("PinCompare protocol is not supported by this IFD.")
		}

		// get values from requested command
		val inputUnit = verify.getInputUnit()
		val allMsgs: AltVUMessagesType = getMessagesOrDefaults(verify.getAltVUMessages())
		var firstTimeout = verify.getTimeoutUntilFirstKey()
		firstTimeout = firstTimeout ?: BigInteger.valueOf(60000)
		var otherTimeout = verify.getTimeoutAfterFirstKey()
		otherTimeout = otherTimeout ?: BigInteger.valueOf(15000)
		val template = verify.getTemplate()

		var response: VerifyUserResponse?
		var result: Result
		// check which type of authentication to perform
		if (inputUnit.getBiometricInput() != null) {
			// TODO: implement
			val msg = "Biometric authentication not supported by IFD."
			val ex = IFDException(ECardConstants.Minor.IFD.IO.UNKNOWN_INPUT_UNIT, msg)
			LOG.warn(ex) { ex.message }
			throw ex
		} else if (inputUnit.getPinInput() != null) {
			val pinInput = inputUnit.getPinInput()

			// we have a sophisticated card reader
			if (terminalInfo.supportsPinCompare()) {
				// create custom pinAction to submit pin to terminal
				val pinAction = NativePinStepAction("enter-pin", pinInput, channel, terminalInfo, template)
				// display message instructing user what to do
				val uc = pinUserConsent(pinAction)
				val ucr = gui!!.obtainNavigator(uc)
				val exec = ExecutionEngine(ucr)
				// run gui
				val status = exec.process()
				if (status == ResultStatus.CANCEL) {
					val msg = "PIN entry cancelled by user."
					LOG.warn { msg }
					result = makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg)
					response =
						WSHelper.makeResponse(
							VerifyUserResponse::class.java,
							result,
						)
				} else if (pinAction.exception != null) {
					LOG.warn(pinAction.exception) { pinAction.exception!!.message }
					result =
						makeResultError(
							ECardConstants.Minor.IFD.AUTHENTICATION_FAILED,
							pinAction.exception!!.message,
						)
					response =
						WSHelper.makeResponse(
							VerifyUserResponse::class.java,
							result,
						)
				} else {
					// input by user
					val verifyResponse = pinAction.response
					// evaluate result
					result = checkNativePinVerify(verifyResponse!!)
					response =
						WSHelper.makeResponse(
							VerifyUserResponse::class.java,
							result,
						)
					response.setResponse(verifyResponse)
				}

				return response
			} else if (this.isVirtual) { // software method
				// get pin, encode and send
				val minLength = pinInput.getPasswordAttributes().getMinLength().toInt()
				val maxLength = pinInput.getPasswordAttributes().getMaxLength().toInt()
				val uc = pinUserConsent(minLength, maxLength)
				val ucr = gui!!.obtainNavigator(uc)
				val exec = ExecutionEngine(ucr)
				val status = exec.process()
				if (status == ResultStatus.CANCEL) {
					val msg = "PIN entry cancelled by user."
					LOG.warn { msg }
					result = makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg)
					response =
						WSHelper.makeResponse(
							VerifyUserResponse::class.java,
							result,
						)
					return response
				}

				val rawPIN: CharArray = getPinFromUserConsent(exec)
				val attributes = pinInput.getPasswordAttributes()
				var verifyTransmit: Transmit?

				try {
					verifyTransmit = PINUtils.buildVerifyTransmit(rawPIN, attributes, template, handle)
				} catch (e: UtilException) {
					val msg = "Failed to create the verifyTransmit message."
					LOG.error(e) { msg }
					result = makeResultError(ECardConstants.Minor.IFD.UNKNOWN_ERROR, msg)
					response =
						WSHelper.makeResponse(
							VerifyUserResponse::class.java,
							result,
						)
					return response
				} finally {
					rawPIN.fill(' ')
				}

				// send to reader
				var transResp: TransmitResponse
				try {
					transResp = ifd.transmit(verifyTransmit)
				} finally {
					// blank PIN APDU
					for (apdu in verifyTransmit.getInputAPDUInfo()) {
						val rawApdu = apdu.getInputAPDU()
						rawApdu?.fill(0.toByte())
					}
				}

				// produce messages
				if (transResp.getResult().getResultMajor() == ECardConstants.Major.ERROR) {
					if (transResp.getOutputAPDU().isEmpty()) {
						result =
							makeResultError(
								ECardConstants.Minor.IFD.AUTHENTICATION_FAILED,
								transResp.getResult().getResultMessage().getValue(),
							)
						response =
							WSHelper.makeResponse(
								VerifyUserResponse::class.java,
								result,
							)
						return response
					} else {
						response =
							WSHelper.makeResponse(
								VerifyUserResponse::class.java,
								transResp.getResult()!!,
							)
						response.setResponse(transResp.getOutputAPDU()[0])

						// repeat if the response apdu signals that there are tries left
						// TODO: move this code to the PIN Compare protocol
						if (response.getResponse() != null) {
							val resApdu = CardResponseAPDU(response.getResponse())
							val statusBytes = resApdu.statusBytes
							val isMainStatus = statusBytes[0] == 0x63.toByte()
							val isMinorStatus =
								(statusBytes[1].toInt() and 0xF0.toByte().toInt()) == 0xC0.toByte().toInt()
							val triesLeft = statusBytes[1].toInt() and 0x0F
							if (isMainStatus && isMinorStatus && triesLeft > 0) {
								LOG.info { "PIN not entered successful. There are ${statusBytes[1].toInt() and 0x0F} tries left." }
								return verifyUser(verify)
							}
						}

						return response
					}
				} else {
					response =
						WSHelper.makeResponse(
							VerifyUserResponse::class.java,
							transResp.getResult()!!,
						)
					response.setResponse(transResp.getOutputAPDU()[0])
					return response
				}
			} else {
				val ex = IFDException("No input unit available to perform PinCompare protocol.")
				LOG.warn(ex) { ex.message }
				throw ex
			}
		} else {
			val msg = "Unsupported authentication input method requested."
			val ex = IFDException(ECardConstants.Minor.IFD.IO.UNKNOWN_INPUT_UNIT, msg)
			LOG.warn(ex) { ex.message }
			throw ex
		}
	}

	private fun beep() {
		if (canBeep()) {
			// TODO: implement
		}
	}

	private fun blink() {
		if (canBlink()) {
			// TODO: implement
		}
	}

	private fun display(
		msg: String?,
		timeout: BigInteger?,
	) {
		if (canDisplay()) {
			// TODO: implement
		}
	}

	private fun canBeep(): Boolean {
		if (canBeep == null) {
			canBeep = capabilities!!.isAcousticSignalUnit
		}
		return canBeep!!
	}

	private fun canBlink(): Boolean {
		if (canBlink == null) {
			canBlink = capabilities!!.isOpticalSignalUnit
		}
		return canBlink!!
	}

	private fun canDisplay(): Boolean {
		if (canDisplay == null) {
			canDisplay = java.lang.Boolean.FALSE
			if (displayIdx == null && !capabilities!!.getDisplayCapability().isEmpty()) {
				canDisplay = java.lang.Boolean.TRUE
			} else {
				for (disp in capabilities!!.getDisplayCapability()) {
					if (disp.getIndex() == displayIdx) {
						canDisplay = java.lang.Boolean.TRUE
						break
					}
				}
			}
		}
		return canDisplay!!
	}

	private val displayCapabilities: DisplayCapabilityType?
		get() {
			if (canDisplay()) {
				if (displayIdx == null) {
					val disp =
						capabilities!!.getDisplayCapability()[0]
					return disp
				} else {
					for (disp in capabilities!!.getDisplayCapability()) {
						if (disp.getIndex() == displayIdx) {
							return disp
						}
					}
					return null
				}
			} else {
				return null
			}
		}

	private fun canEnter(): Boolean {
		if (canEnter == null) {
			canEnter = java.lang.Boolean.FALSE
			if (keyIdx == null && !capabilities!!.getKeyPadCapability().isEmpty()) {
				canEnter = java.lang.Boolean.TRUE
			} else {
				for (key in capabilities!!.getKeyPadCapability()) {
					if (key.getIndex() == keyIdx) {
						canEnter = java.lang.Boolean.TRUE
						break
					}
				}
			}
		}
		return canEnter!!
	}

	private val keypadCapabilities: KeyPadCapabilityType?
		get() {
			if (canEnter()) {
				if (keyIdx == null) {
					val key =
						capabilities!!.getKeyPadCapability()[0]
					return key
				} else {
					for (key in capabilities!!.getKeyPadCapability()) {
						if (key.getIndex() == keyIdx) {
							return key
						}
					}
					return null
				}
			} else {
				return null
			}
		}

	private val isVirtual: Boolean
		get() = gui != null

	@Throws(IFDException::class)
	private fun getCapabilities() {
		val capabilitiesReq = GetIFDCapabilities()
		capabilitiesReq.setContextHandle(ctxHandle)
		capabilitiesReq.setIFDName(terminalInfo.name)

		val cap = ifd.getIFDCapabilities(capabilitiesReq)
		val r = cap.getResult()
		if (r.getResultMajor() == ECardConstants.Major.ERROR) {
			val ex = IFDException(r)
			LOG.warn(ex) { ex.message }
			throw ex
		}
		this.capabilities = cap.getIFDCapabilities()
	}

	private fun pinUserConsent(
		minLength: Int,
		maxLength: Int,
	): UserConsentDescription {
		// title always "action.changepin.userconsent.pinstep.title
		val uc =
			UserConsentDescription(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized(),
				"pin_entry_dialog",
			)
		// create step
		val s =
			Step(
				"enter-pin",
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized(),
			)
		uc.steps.add(s)
		// add text instructing user
		// add text instructing user
		val i1 = Text()
		s.inputInfoUnits.add(i1)
		i1.text = (
			I18N.strings.pinplugin_action_pinentry_userconsent_pinstep_enter_pin.localized()
		)

		val i2 = PasswordField("pin")
		s.inputInfoUnits.add(i2)
		i2.description = "PIN"
		i2.minLength = minLength
		i2.maxLength = maxLength

		return uc
	}

	private fun pinUserConsent(action: StepAction): UserConsentDescription {
		val uc =
			UserConsentDescription(
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized(),
				"pin_entry_dialog",
			)
		// create step
		val s =
			Step(
				"enter-pin",
				I18N.strings.pinplugin_action_changepin_userconsent_pinstep_title.localized(),
			)
		s.action = action
		uc.steps.add(s)
		s.isInstantReturn = true
		// add text instructing user
		val i1 = Text()
		s.inputInfoUnits.add(i1)
		i1.text = I18N.strings.pinplugin_action_pinentry_userconsent_pinstep_enter_pin_term.localized()
		return uc
	}
}

private fun getMessagesOrDefaults(messages: AltVUMessagesType?): AltVUMessagesType {
	val allMsgs = AltVUMessagesType()

	if (messages == null || messages.getAuthenticationRequestMessage() == null) {
		allMsgs.setAuthenticationRequestMessage("Enter secret:")
	} else {
		allMsgs.setAuthenticationRequestMessage(messages.getAuthenticationRequestMessage())
	}
	if (messages == null || messages.getSuccessMessage() == null) {
		allMsgs.setSuccessMessage("Secret entered successfully.")
	} else {
		allMsgs.setSuccessMessage(messages.getSuccessMessage())
	}
	if (messages == null || messages.getAuthenticationFailedMessage() == null) {
		allMsgs.setAuthenticationFailedMessage("Secret not entered successfully.")
	} else {
		allMsgs.setAuthenticationFailedMessage(messages.getAuthenticationFailedMessage())
	}
	if (messages == null || messages.getRequestConfirmationMessage() == null) {
		allMsgs.setRequestConfirmationMessage("Enter secret again:")
	} else {
		allMsgs.setRequestConfirmationMessage(messages.getRequestConfirmationMessage())
	}
	if (messages == null || messages.getCancelMessage() == null) {
		allMsgs.setCancelMessage("Canceled secret input.")
	} else {
		allMsgs.setCancelMessage(messages.getCancelMessage())
	}

	return allMsgs
}

private fun checkNativePinVerify(response: ByteArray): Result {
	val sw1 = response[0]
	val sw2 = response[1]
	if (sw1 == 0x64.toByte()) {
		if (sw2 == 0x00.toByte()) {
			return makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Verify operation timed out.")
		} else if (sw2 == 0x01.toByte()) {
			return makeResultError(
				ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
				"Verify operation was cancelled with the cancel button.",
			)
		} else if (sw2 == 0x02.toByte()) {
			return makeResultUnknownError("Modify PIN operation failed because two PINs were different.")
		} else if (sw2 == 0x03.toByte()) {
			return makeResultUnknownError("PIN has wrong length.")
		}
	} else if (sw1 == 0x6b.toByte()) {
		if (sw2 == 0x80.toByte()) {
			return makeResultUnknownError("Invalid parameter passed to verify command.")
		}
	} else if (sw1 == 0x90.toByte()) {
		if (sw2 == 0x00.toByte()) {
			return makeResultOK()
		}
	}
	return makeResultUnknownError(CardCommandStatus.getMessage(response))
}

private fun getPinFromUserConsent(response: ExecutionEngine): CharArray {
	val p = response.results["enter-pin"]!!.getResult("pin") as PasswordField
	return p.value
}
