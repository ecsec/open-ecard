package org.openecard.sc.iface.feature

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
		// extract attributes
		val pwdType = attributes.pwdType
		val storedLen = attributes.storedLength

		// only proceed if we need to pad
		return attributes.padChar?.let {
			var padChar = it
			if (storedLen <= 0) {
				throw IllegalArgumentException("PIN mask can only be created when storage size is known.")
			}

			// they are all the same except half nibble which
			if (PasswordType.HALF_NIBBLE_BCD == pwdType) {
				padChar = (padChar.toUInt() or 0xF0u).toUByte()
			}

			val mask = UByteArray(storedLen) { padChar }

			// iso needs a sligth correction
			if (PasswordType.ISO_9564_1 == pwdType) {
				mask[0] = 0x20u
			}

			mask
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun encodeTextPin(
		rawPin: String,
		minLen: Int,
		storedLen: Int,
		maxLen: Int,
		padChar: UByte?,
	): UByteArray {
		val needsPadding = padChar != null
		// perform some basic checks
		if (needsPadding && storedLen <= 0) {
			throw IllegalArgumentException("Padding is required, but no stored length is given.")
		}
		if (rawPin.length < minLen) {
			throw IllegalArgumentException("Entered PIN is too short, enter at least $minLen characters.")
		}
		if (maxLen > 0 && rawPin.length > maxLen) {
			throw IllegalArgumentException("Entered PIN is too long, enter at most $maxLen characters.")
		}

		var pinBytes = rawPin.encodeToByteArray().toUByteArray()
		if (pinBytes.size > storedLen) {
			pinBytes.fill(0u)
			throw IllegalArgumentException("Storage size for PIN exceeded, only $storedLen bytes are allowed.")
		}

		// if the pin is too short, append the necessary padding bytes
		if (needsPadding && pinBytes.size < storedLen) {
			val missingBytes = storedLen - pinBytes.size
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
		minLen: Int,
		storedLen: Int,
		maxLen: Int,
		padChar: UByte?,
	): UByteArray {
		val needsPadding = padChar != null

		return buildList<UByte> {
			val pinSize = rawPin.size

			if (PasswordType.ISO_9564_1 == pwdType) {
				val head = (0x20 or (0x0F and pinSize)).toUByte()
				add(head)
			}

			if (PasswordType.HALF_NIBBLE_BCD == pwdType) {
				for (i in 0..<pinSize) {
					val nextChar = rawPin[i]
					val digit = (0xF0 or getByte(nextChar).toInt()).toUByte()
					add(digit)
				}
			} else if (PasswordType.BCD == pwdType || PasswordType.ISO_9564_1 == pwdType) {
				var i = 0
				while (i < pinSize) {
					require(padChar != null)
					val b1 = (getByte(rawPin[i]).toInt() shl 4).toByte()
					var b2 = (padChar.toInt() and 0x0F).toByte() // lower nibble set to pad byte
					// one char left, replace pad nibble with it
					if (i + 1 < pinSize) {
						b2 = (getByte(rawPin[i + 1]).toInt() and 0x0F).toByte()
					}
					val b = (b1.toInt() or b2.toInt()).toUByte()
					add(b)
					i += 2
				}
			}

			// add padding bytes if needed
			if (needsPadding && size < storedLen) {
				val missingBytes = storedLen - size
				val filler = UByteArray(missingBytes) { padChar }
				addAll(filler)
			}
		}.toUByteArray()
	}

	private fun getByte(c: Char): Byte {
		if (c >= '0' && c <= '9') {
			return (c.code - '0'.code).toByte()
		} else {
			throw IllegalArgumentException("Entered PIN contains invalid characters.")
		}
	}

	private fun needsPadding(attributes: PasswordAttributes): Boolean {
		val pwdType = attributes.pwdType
		if (PasswordType.ISO_9564_1 == pwdType) {
			return true
		} else {
			val needsPadding = attributes.pwdFlags.contains(PasswordFlags.NEEDS_PADDING)
			return needsPadding
		}
	}
}
