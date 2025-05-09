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
 */
package org.openecard.common.util

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType
import iso.std.iso_iec._24727.tech.schema.Transmit
import org.openecard.common.ECardConstants.Minor
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.Arrays

/**
 * Implements convenience methods for dealing with PINs.
 *
 * @author Johannes Schmoelz
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */

private val LOG = KotlinLogging.logger { }

object PINUtils {
	/**
	 * Build a Transmit containing a verify APDU.
	 *
	 * @param rawPIN the pin as entered by the user
	 * @param attributes attributes of the password (e.g. encoding and length)
	 * @param template the verify template
	 * @param slotHandle slot handle
	 * @return Transmit containing the built verify APDU
	 * @throws UtilException if an pin related error occurs (e.g. wrong PIN length)
	 */
	@JvmStatic
	fun buildVerifyTransmit(
		rawPIN: CharArray,
		attributes: PasswordAttributesType,
		template: ByteArray?,
		slotHandle: ByteArray?,
	): Transmit {
		// concatenate template with encoded pin
		val pin = encodePin(rawPIN, attributes)
		var pinCmd = ByteUtils.concatenate(template, pin!!.size.toByte())
		pinCmd = ByteUtils.concatenate(pinCmd, pin)
		Arrays.fill(pin, 0.toByte())

		val transmit = Transmit()
		transmit.slotHandle = slotHandle
		val pinApdu = InputAPDUInfoType()
		pinApdu.inputAPDU = pinCmd
		pinApdu.acceptableStatusCode.add(byteArrayOf(0x90.toByte(), 0x00.toByte()))
		transmit.inputAPDUInfo.add(pinApdu)
		return transmit
	}
	@JvmStatic
	fun encodePin(
		rawPin: CharArray,
		attributes: PasswordAttributesType,
	): ByteArray? {
		// extract attributes
		val pwdType = attributes.pwdType
		val minLen = attributes.minLength.toInt()
		val maxLen = if (attributes.maxLength == null) 0 else attributes.maxLength.toInt()
		val storedLen = attributes.storedLength.toInt()
		val needsPadding = needsPadding(attributes)

		// check if padding is inferred
		val padChar = getPadChar(attributes, needsPadding)

		// helper variables
		var encoding = "UTF-8"

		try {
			when (pwdType) {
				PasswordTypeType.ASCII_NUMERIC -> {
					encoding = "US-ASCII"
					val textPin = encodeTextPin(encoding, rawPin, minLen, storedLen, maxLen, needsPadding, padChar)
					return textPin
				}

				PasswordTypeType.UTF_8 -> {
					val textPin = encodeTextPin(encoding, rawPin, minLen, storedLen, maxLen, needsPadding, padChar)
					return textPin
				}

				PasswordTypeType.ISO_9564_1, PasswordTypeType.BCD, PasswordTypeType.HALF_NIBBLE_BCD -> {
					val bcdPin = encodeBcdPin(pwdType, rawPin, minLen, storedLen, maxLen, needsPadding, padChar)
					return bcdPin
				}

				else -> {
					val msg = "Unsupported PIN encoding requested."
					val ex = UtilException(Minor.IFD.IO.UNKNOWN_PIN_FORMAT, msg)
					LOG.error(ex) { ex.message }
					throw ex
				}
			}
		} catch (ex: UnsupportedEncodingException) {
			throw UtilException(ex)
		} catch (ex: IOException) {
			throw UtilException(ex)
		}
	}
	@JvmStatic
	fun createPinMask(attributes: PasswordAttributesType): ByteArray {
		// extract attributes
		val pwdType = attributes.pwdType
		val minLen = attributes.minLength.toInt()
		val maxLen = if (attributes.maxLength == null) 0 else attributes.maxLength.toInt()
		val storedLen = attributes.storedLength.toInt()
		val needsPadding = needsPadding(attributes)

		// opt out if needs-padding is not on
		if (!needsPadding) {
			return ByteArray(0)
		}

		var padChar = getPadChar(attributes, needsPadding)

		if (storedLen <= 0) {
			throw UtilException("PIN mask can only be created when storage size is known.")
		}

		// they are all the same except half nibble which
		if (PasswordTypeType.HALF_NIBBLE_BCD == pwdType) {
			padChar = (padChar.toInt() or 0xF0).toByte()
		}

		val mask = ByteArray(storedLen)
		Arrays.fill(mask, padChar)

		// iso needs a sligth correction
		if (PasswordTypeType.ISO_9564_1 == pwdType) {
			mask[0] = 0x20
		}

		return mask
	}

	@JvmStatic
	@Suppress("DefaultLocale")
	fun encodeTextPin(
		encoding: String?,
		rawPin: CharArray,
		minLen: Int,
		storedLen: Int,
		maxLen: Int,
		needsPadding: Boolean,
		padChar: Byte,
	): ByteArray? {
		// perform some basic checks
		if (needsPadding && storedLen <= 0) {
			val msg = "Padding is required, but no stored length is given."
			throw UtilException(msg)
		}
		if (rawPin.size < minLen) {
			val msg = String.format("Entered PIN is too short, enter at least %d characters.", minLen)
			throw UtilException(msg)
			// throw new UtilException("PIN contains invalid symbols.");
		}
		if (maxLen > 0 && rawPin.size > maxLen) {
			val msg = String.format("Entered PIN is too long, enter at most %d characters.", maxLen)
			throw UtilException(msg)
		}

		// get the pin string and validate it is within stored length
		val charset = Charset.forName(encoding)
		val bb = charset.encode(CharBuffer.wrap(rawPin))
		var pinBytes: ByteArray? = ByteArray(bb.remaining())
		bb[pinBytes]
		// blank out buffer array
		if (bb.hasArray()) {
			Arrays.fill(bb.array(), 0.toByte())
		}

		if (storedLen > 0 && pinBytes!!.size > storedLen) {
			Arrays.fill(pinBytes, 0.toByte())
			val msg = String.format("Storage size for PIN exceeded, only %d bytes are allowed.", storedLen)
			throw UtilException(msg)
		}
		// if the pin is too short, append the necessary padding bytes
		if (needsPadding && pinBytes!!.size < storedLen) {
			val missingBytes = storedLen - pinBytes.size
			val filler = ByteArray(missingBytes)
			Arrays.fill(filler, padChar)
			val pinBytesTmp = ByteUtils.concatenate(pinBytes, filler)
			// blank array before it was copied
			Arrays.fill(pinBytes, 0.toByte())
			pinBytes = pinBytesTmp
		}

		return pinBytes
	}

	fun encodeBcdPin(
		pwdType: PasswordTypeType,
		rawPin: CharArray,
		minLen: Int,
		storedLen: Int,
		maxLen: Int,
		needsPadding: Boolean,
		padChar: Byte,
	): ByteArray {
		val o = ByteArrayOutputStream()
		val pinSize = rawPin.size

		if (PasswordTypeType.ISO_9564_1 == pwdType) {
			val head = (0x20 or (0x0F and pinSize)).toByte()
			o.write(head.toInt())
		}

		if (PasswordTypeType.HALF_NIBBLE_BCD == pwdType) {
			for (i in 0..<pinSize) {
				val nextChar = rawPin[i]
				val digit = (0xF0 or getByte(nextChar).toInt()).toByte()
				o.write(digit.toInt())
			}
		} else if (PasswordTypeType.BCD == pwdType || PasswordTypeType.ISO_9564_1 == pwdType) {
			var i = 0
			while (i < pinSize) {
				val b1 = (getByte(rawPin[i]).toInt() shl 4).toByte()
				var b2 = (padChar.toInt() and 0x0F).toByte() // lower nibble set to pad byte
				// one char left, replace pad nibble with it
				if (i + 1 < pinSize) {
					b2 = (getByte(rawPin[i + 1]).toInt() and 0x0F).toByte()
				}
				val b = (b1.toInt() or b2.toInt()).toByte()
				o.write(b.toInt())
				i += 2
			}
		}

		// add padding bytes if needed
		if (needsPadding && o.size() < storedLen) {
			val missingBytes = storedLen - o.size()
			val filler = ByteArray(missingBytes)
			Arrays.fill(filler, padChar)
			o.write(filler)
		}

		return o.toByteArray()
	}

	private fun getByte(c: Char): Byte {
		if (c >= '0' && c <= '9') {
			return (c.code - '0'.code).toByte()
		} else {
			val ex = UtilException("Entered PIN contains invalid characters.")
			LOG.error(ex) { ex.message }
			throw ex
		}
	}

	private fun getPadChar(
		attributes: PasswordAttributesType,
		needsPadding: Boolean,
	): Byte {
		if (PasswordTypeType.ISO_9564_1 == attributes.pwdType) {
			return 0xFF.toByte()
		} else {
			val padChars = attributes.padChar
			return if (padChars != null && padChars.size == 1) {
				padChars[0]
			} else if (needsPadding) {
				val ex = UtilException("Unsupported combination of PIN parameters concerning padding.")
				throw ex
			} else {
				// just return a value, it is not gonna be used in this case
				0
			}
		}
	}

	private fun needsPadding(attributes: PasswordAttributesType): Boolean {
		val pwdType = attributes.pwdType
		if (PasswordTypeType.ISO_9564_1 == pwdType) {
			return true
		} else {
			val needsPadding = attributes.pwdFlags.contains("needs-padding")
			return needsPadding
		}
	}
}
