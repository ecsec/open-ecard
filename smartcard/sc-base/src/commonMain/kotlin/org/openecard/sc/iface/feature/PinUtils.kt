package org.openecard.sc.iface.feature

import org.openecard.utils.common.throwIf

/**
 * Implements convenience methods for dealing with PINs.
 *
 * @author Johannes Schmoelz
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
object PinUtils {
// 	/**
// 	 * Build a Transmit containing a verify APDU.
// 	 *
// 	 * @param rawPIN the pin as entered by the user
// 	 * @param attributes attributes of the password (e.g. encoding and length)
// 	 * @param template the verify template
// 	 * @param slotHandle slot handle
// 	 * @return Transmit containing the built verify APDU
// 	 * @throws IllegalArgumentException if an pin related error occurs (e.g. wrong PIN length)
// 	 */
// 	fun buildVerifyTransmit(
// 		rawPIN: CharArray,
// 		attributes: PasswordAttributes,
// 		template: ByteArray?,
// 		slotHandle: ByteArray?,
// 	): Transmit {
// 		// concatenate template with encoded pin
// 		val pin = encodePin(rawPIN, attributes)
// 		var pinCmd = ByteUtils.concatenate(template, pin!!.size.toByte())
// 		pinCmd = ByteUtils.concatenate(pinCmd, pin)
// 		Arrays.fill(pin, 0.toByte())
//
// 		val transmit = Transmit()
// 		transmit.slotHandle = slotHandle
// 		val pinApdu = InputAPDUInfoType()
// 		pinApdu.inputAPDU = pinCmd
// 		pinApdu.acceptableStatusCode.add(byteArrayOf(0x90.toByte(), 0x00.toByte()))
// 		transmit.inputAPDUInfo.add(pinApdu)
// 		return transmit
// 	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun encodePin(
		rawPin: String,
		attributes: PasswordAttributes,
	): UByteArray {
		// extract attributes
		val pwdType = attributes.pwdType
		val minLen = attributes.minLength
		val maxLen = attributes.maxLength
		val storedLen = attributes.storedLength

		// check if padding is inferred
		val padChar = attributes.padChar

		when (pwdType) {
			PasswordType.ASCII_NUMERIC -> {
				val textPin = encodeTextPin(rawPin, minLen, storedLen, maxLen, padChar)
				return textPin
			}

			PasswordType.UTF_8 -> {
				val textPin = encodeTextPin(rawPin, minLen, storedLen, maxLen, padChar)
				return textPin
			}

			PasswordType.ISO_9564_1, PasswordType.BCD, PasswordType.HALF_NIBBLE_BCD -> {
				val bcdPin = encodeBcdPin(pwdType, rawPin.toCharArray(), minLen, storedLen, maxLen, padChar)
				return bcdPin
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun createPinMask(attributes: PasswordAttributes): UByteArray? {
		// only proceed if we need to pad
		if (needsPadding(attributes)) {
			// extract attributes
			val pwdType = attributes.pwdType
			val storedLen = requireNotNull(attributes.storedLength) { "PIN mask can only be created when storage size is known" }
			var padChar = requireNotNull(attributes.padChar) { "PIN mask can only be created when pad char is defined" }

			// they are all the same except half nibble which
			if (PasswordType.HALF_NIBBLE_BCD == pwdType) {
				padChar = (padChar.toUInt() or 0xF0u).toUByte()
			}

			val mask = UByteArray(storedLen.toInt()) { padChar }

			// iso needs a sligth correction
			if (PasswordType.ISO_9564_1 == pwdType) {
				mask[0] = 0x20u
			}

			return mask
		} else {
			return null
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun encodeTextPin(
		rawPin: String,
		minLen: UInt,
		storedLen: UInt?,
		maxLen: UInt?,
		padChar: UByte?,
	): UByteArray {
		val needsPadding = padChar != null
		// perform some basic checks
		if (needsPadding) {
			requireNotNull(storedLen)
			throwIf(storedLen <= 0u) { IllegalArgumentException("Padding is required, but no stored length is given") }
		}
		if (rawPin.length.toUInt() < minLen) {
			throw IllegalArgumentException("Entered PIN is too short, enter at least $minLen characters")
		}
		if (maxLen != null && maxLen > 0u && rawPin.length.toUInt() > maxLen) {
			throw IllegalArgumentException("Entered PIN is too long, enter at most $maxLen characters")
		}

		var pinBytes = rawPin.encodeToByteArray().toUByteArray()
		if (storedLen != null && pinBytes.size.toUInt() > storedLen) {
			pinBytes.fill(0u)
			throw IllegalArgumentException("Storage size for PIN exceeded, only $storedLen bytes are allowed")
		}

		// if the pin is too short, append the necessary padding bytes
		if (needsPadding && pinBytes.size.toUInt() < checkNotNull(storedLen)) {
			val missingBytes = storedLen.toInt() - pinBytes.size
			val filler = UByteArray(missingBytes)
			filler.fill(padChar)
			val pinBytesTmp = pinBytes + filler
			// blank array before it was copied
			pinBytes.fill(0u)
			pinBytes = pinBytesTmp
		}

		return pinBytes
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun encodeBcdPin(
		pwdType: PasswordType,
		rawPin: CharArray,
		minLen: UInt,
		storedLen: UInt?,
		maxLen: UInt?,
		padChar: UByte?,
	): UByteArray =
		buildList {
			val pinSize = rawPin.size

			if (PasswordType.ISO_9564_1 == pwdType) {
				val head = (0x20 or (0x0F and pinSize)).toUByte()
				add(head)
			}

			if (PasswordType.HALF_NIBBLE_BCD == pwdType) {
				for (i in 0..<pinSize) {
					val nextChar = rawPin[i]
					val digit = (0xF0 or nextChar.digitToInt()).toUByte()
					add(digit)
				}
			} else if (PasswordType.BCD == pwdType || PasswordType.ISO_9564_1 == pwdType) {
				var i = 0
				while (i < pinSize) {
					require(padChar != null)
					val b1 = (rawPin[i].digitToInt() shl 4).toByte()
					var b2 = (padChar.toInt() and 0x0F).toByte() // lower nibble set to pad byte
					// one char left, replace pad nibble with it
					if (i + 1 < pinSize) {
						b2 = (rawPin[i + 1].digitToInt() and 0x0F).toByte()
					}
					val b = (b1.toInt() or b2.toInt()).toUByte()
					add(b)
					i += 2
				}
			}

			// add padding bytes if needed
			val needsPadding = padChar != null
			if (needsPadding) {
				requireNotNull(storedLen) { "Stored length is missing for a PIN which needs padding" }
				if (size < storedLen.toInt()) {
					val missingBytes = storedLen.toInt() - size
					val filler = UByteArray(missingBytes) { padChar }
					addAll(filler)
				} else if (size > storedLen.toInt()) {
					throw IllegalArgumentException("Size of PIN is bigger than stored length")
				}
			}
		}.toUByteArray()

	private fun needsPadding(attributes: PasswordAttributes): Boolean {
		val pwdType = attributes.pwdType
		if (PasswordType.ISO_9564_1 == pwdType) {
			return true
		} else {
			val needsPadding = attributes.padChar != null
			return needsPadding
		}
	}
}
