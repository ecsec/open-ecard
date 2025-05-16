package org.openecard.sc.iface.feature

import org.openecard.sc.iface.ResponseApdu
import org.openecard.sc.utils.UsbLang
import org.openecard.sc.utils.UsbLangId
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

interface ModifyPinFeature : Feature {
	fun modifyPin(request: PinModify): ResponseApdu
}

interface VerifyPinFeature : Feature {
	fun verifyPin(request: PinVerify): ResponseApdu
}

data class PinVerify(
	/** timeout in seconds (00 means use default timeout) */
	val timeoutProcess: UByte = 0u,
	/** timeout in seconds after first key stroke */
	val timeoutAfterFirstKeypress: UByte = 0u,
	/** formatting options USB_CCID_PIN_FORMAT_xxx */
	val formatString: UByte,
	/**
	 * - bits 7-4 bit size of PIN length in APDU
	 * - bits 3-0 PIN block size in bytes after justification and formatting
	 */
	val pinBlockString: UByte,
	/**
	 * - bits 7-5 RFU, bit 4 set if system units are bytes clear if system units are bits
	 * - bits 3-0 PIN length position in system units
	 */
	val pinLengthFormat: UByte,
	/** minimum PIN size in digits */
	val pinMinSize: UByte,
	/** maximum PIN size in digits */
	val pinMaxSize: UByte,
	/** Conditions under which PIN entry should be considered complete */
	val entryValidationCondition: UByte = EntryValidationCondition.VALIDATION_KEY_PRESSED.code,
	/**
	 * Number of messages to display for verification
	 *
	 * - 0x0 no string
	 * - 0x1 Message indicated by msg idx
	 * - 0xFF default CCID message
	 */
	val numberVerificationMessages: UByte = 1u,
	/** Language for messages */
	val langIndex: UShort = UsbLang.ENGLISH_UNITED_STATES.code,
	/**
	 * Message index (should be 00)
	 */
	val messageIndex: UByte = MessageIndex.PIN_INSERT.code,
	/** T=1 I-block prologue field to use (fill with 00) */
	val blockPrologue: UInt = 0u,
	/** Data to send to the ICC */
	val template: PrintableUByteArray,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	val bytes: UByteArray by lazy {
		buildList {
			add(timeoutProcess)
			add(timeoutAfterFirstKeypress)
			add(formatString)
			add(pinBlockString)
			add(pinLengthFormat)
			add(pinMaxSize)
			add(pinMinSize)
			add(entryValidationCondition)
			add(numberVerificationMessages)
			addAll(langIndex.toUByteArray())
			add(messageIndex)
			addAll(blockPrologue.toUByteArray().drop(1))
			addAll(
				template.v.size
					.toUInt()
					.toUByteArray(),
			)
			addAll(template.v)
		}.toUByteArray()
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromParams(
			pwAttr: PasswordAttributes,
			cmdTemplate: UByteArray,
			validationCondition: EntryValidationCondition = EntryValidationCondition.VALIDATION_KEY_PRESSED,
			lang: UsbLangId = UsbLangId(UsbLang.ENGLISH_UNITED_STATES.code),
		): PinVerify {
			val pinTemplate = PinUtils.createPinMask(pwAttr)
			val template =
				pinTemplate?.let {
					cmdTemplate + pinTemplate.size.toUByte() + pinTemplate
				} ?: cmdTemplate

			return PinVerify(
				template = template.toPrintable(),
				formatString = pwAttr.toFormatString(),
				pinBlockString = pwAttr.toPinBlockString(),
				pinLengthFormat = pwAttr.toPinLengthFormat(),
				pinMinSize = pwAttr.minLength.toUByte(),
				pinMaxSize = pwAttr.maxLength.toUByte(),
				langIndex = lang.code,
				entryValidationCondition = validationCondition.code,
			)
		}
	}
}

data class PinModify(
	/** timeout in seconds (00 means use default timeout) */
	val timeoutProcess: UByte = 0u,
	/** timeout in seconds after first key stroke */
	val timeoutAfterFirstKeypress: UByte = 0u,
	/** formatting options USB_CCID_PIN_FORMAT_xxx */
	val formatString: UByte,
	/**
	 * - bits 7-4 bit size of PIN length in APDU
	 * - bits 3-0 PIN block size in bytes after justification and formatting
	 */
	val pinBlockString: UByte,
	/**
	 * - bits 7-5 RFU, bit 4 set if system units are bytes clear if system units are bits
	 * - bits 3-0 PIN length position in system units
	 */
	val pinLengthFormat: UByte,
	/** Insertion position offset in bytes for the current PIN */
	val offsetOldPin: UByte = 0u,
	/** Insertion position offset in bytes for the new PIN */
	val offsetNewPin: UByte = 0u,
	/** minimum PIN size in digits */
	val pinMinSize: UByte,
	/** maximum PIN size in digits */
	val pinMaxSize: UByte,
	/** Flags governing need for confirmation of new PIN */
	val confirmPin: UByte = EntryValidationCondition.VALIDATION_KEY_PRESSED.code,
	/** Conditions under which PIN entry should be considered complete */
	val entryValidationCondition: UByte = EntryValidationCondition.VALIDATION_KEY_PRESSED.code,
	/** Number of messages to display for verification */
	val numberVerificationMessages: UByte = 2u,
	/** Language for messages */
	val langIndex: UShort = UsbLang.ENGLISH_UNITED_STATES.code,
	/** Index of 1st prompting message */
	val messageIndex1: UByte = MessageIndex.PIN_INSERT.code,
	/** Index of 2nd prompting message */
	val messageIndex2: UByte = MessageIndex.PIN_MODIFY.code,
	/** Index of 3rd prompting message */
	val messageIndex3: UByte = MessageIndex.NEW_PIN.code,
	/** T=1 I-block prologue field to use (fill with 00) */
	val blockPrologue: UInt = 0u,
	/** Data to send to the ICC */
	val template: PrintableUByteArray,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	val bytes: UByteArray by lazy {
		buildList {
			add(timeoutProcess)
			add(timeoutAfterFirstKeypress)
			add(formatString)
			add(pinBlockString)
			add(pinLengthFormat)
			add(offsetOldPin)
			add(offsetNewPin)
			add(pinMaxSize)
			add(pinMinSize)
			add(confirmPin)
			add(entryValidationCondition)
			add(numberVerificationMessages)
			addAll(langIndex.toUByteArray())
			add(messageIndex1)
			add(messageIndex2)
			add(messageIndex3)
			addAll(blockPrologue.toUByteArray().drop(1))
			addAll(
				template.v.size
					.toUInt()
					.toUByteArray(),
			)
			addAll(template.v)
		}.toUByteArray()
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromParams(
			pwAttr: PasswordAttributes,
			cmdTemplate: UByteArray,
			validationCondition: EntryValidationCondition = EntryValidationCondition.VALIDATION_KEY_PRESSED,
			lang: UsbLangId = UsbLangId(UsbLang.ENGLISH_UNITED_STATES.code),
		): PinModify {
			val pinTemplate = PinUtils.createPinMask(pwAttr)
			val template =
				pinTemplate?.let {
					cmdTemplate + pinTemplate.size.toUByte() + pinTemplate
				} ?: cmdTemplate

			return PinModify(
				template = template.toPrintable(),
				formatString = pwAttr.toFormatString(),
				pinBlockString = pwAttr.toPinBlockString(),
				pinLengthFormat = pwAttr.toPinLengthFormat(),
				pinMinSize = pwAttr.minLength.toUByte(),
				pinMaxSize = pwAttr.maxLength.toUByte(),
				langIndex = lang.code,
				entryValidationCondition = validationCondition.code,
			)
		}
	}
}

enum class EntryValidationCondition(
	val code: UByte,
) {
	MAX_SIZE_REACHED(0x1u),
	VALIDATION_KEY_PRESSED(0x2u),
	TIMEOUT_OCCURRED(0x4u),
}

enum class MessageIndex(
	val code: UByte,
) {
	PIN_INSERT(0x0u),
	PIN_MODIFY(0x1u),
	NEW_PIN(0x2u),
}

private fun PasswordAttributes.toFormatString(): UByte {
	// prepare bmFormatString
	val nibbleHandling = pwdType == PasswordType.BCD || pwdType == PasswordType.ISO_9564_1
	val bmSysUnits = 1u // bytes
	val bmPinPos = (if (isIsoPin) 1u else 0u)
	val bmJustify = 0u // left
	val bmPinType =
		if (nibbleHandling) {
			1u
		} else if (pwdType == PasswordType.ASCII_NUMERIC || pwdType == PasswordType.UTF_8) {
			2u
		} else {
			0u
		}
	val formatString =
		(bmSysUnits shl 7) or (bmPinPos shl 3) or (bmJustify shl 2) or bmPinType

	return formatString.toUByte()
}

private fun PasswordAttributes.toPinBlockString(): UByte {
	val bmPinManagement = if (isIsoPin) 4 else 0 // number of bits of the length field
	val pinSize = if (isIsoPin) storedLength - 1 else storedLength
	return ((bmPinManagement shl 4) or pinSize).toUByte()
}

private fun PasswordAttributes.toPinLengthFormat(): UByte {
	val bmPinLengthUnit = 0 // bits
	val bmPinBytePos = if (isIsoPin) 4 else 0
	return ((bmPinLengthUnit shl 4) or bmPinBytePos).toUByte()
}
