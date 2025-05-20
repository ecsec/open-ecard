/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
package org.openecard.ifd.scio.reader

import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType
import org.openecard.common.USBLangID
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.IntegerUtils
import org.openecard.common.util.PINUtils
import org.openecard.common.util.UtilException
import org.openecard.ifd.scio.IFDException
import java.io.ByteArrayOutputStream
import java.util.Locale

/**
 *
 * @author Tobias Wich
 */
class PCSCPinVerify(
	attributes: PasswordAttributesType,
	cmdTemplate: ByteArray,
) {
	private val pwdType = attributes.getPwdType()
	private val minLen: Int = attributes.getMinLength().toInt()
	private val storedLen: Int = attributes.getStoredLength().toInt()
	private val maxLen: Int =
		if (attributes.getMaxLength() != null) {
			attributes.getMaxLength().toInt()
		} else {
			if (pwdType == PasswordTypeType.ISO_9564_1) {
				(storedLen * 2) - 2
			} else if (pwdType == PasswordTypeType.BCD) {
				storedLen * 2
			} else {
				this.storedLen
			}
		}

	@Throws(IFDException::class)
	private fun prepareStructure(
		attributes: PasswordAttributesType,
		cmdTemplate: ByteArray,
	) {
		// get apdu and pin template
		val pinTemplate =
			try {
				PINUtils.createPinMask(attributes)
			} catch (e: UtilException) {
				val ex = IFDException(e)
				throw ex
			}

		var template = cmdTemplate
		if (pinTemplate.size > 0) {
			template = ByteUtils.concatenate(cmdTemplate, pinTemplate.size.toByte())!!
			template = ByteUtils.concatenate(template, pinTemplate)!!
		}
		setData(template)

		val nibbleHandling = pwdType == PasswordTypeType.BCD || pwdType == PasswordTypeType.ISO_9564_1
		val isoPin = pwdType == PasswordTypeType.ISO_9564_1
		val pinLenIdx = template.size // pointer to byte containing pin length in iso encoding
		val pinPos = if (isoPin) pinLenIdx + 1 else pinLenIdx

		// prepare bmFormatString
		val bmSysUnits: Byte = 1 // bytes
		val bmPinPos = (if (isoPin) 1 else 0).toByte()
		val bmJustify: Byte = 0 // left
		var bmPinType: Byte = 0 // binary
		if (nibbleHandling) {
			bmPinType = 1
		} else if (pwdType == PasswordTypeType.ASCII_NUMERIC || pwdType == PasswordTypeType.UTF_8) {
			bmPinType = 2
		}
		this.bmFormatString =
			((bmSysUnits.toInt() shl 7) or (bmPinPos.toInt() shl 3) or (bmJustify.toInt() shl 2) or bmPinType.toInt()).toByte()

		// prepare pin block string
		val bmPinManagement = (if (isoPin) 4 else 0).toByte() // number of bits of the length field
		val pinSize = (if (isoPin) storedLen - 1 else storedLen).toByte()
		this.bmPINBlockString = ((bmPinManagement.toInt() shl 4) or pinSize.toInt()).toByte()

		// pin length format
		val bmPinLengthUnit: Byte = 0 // bits
		val bmPinBytePos = (if (isoPin) 4 else 0).toByte()
		bmPINLengthFormat = ((bmPinLengthUnit.toInt() shl 4) or bmPinBytePos.toInt()).toByte()

		this.minPINSize = minLen.toByte()
		this.maxPINSize = maxLen.toByte()
	}

	/** timeout in seconds, 0 means default  */
	var bTimeOut: Byte = 0x3C.toByte()

	/** timeout in seconds after first keystroke  */
	var bTimeOut2: Byte = 0

	/** formatting options, USB_CCID_PIN_FORMAT  */
	var bmFormatString: Byte = 0

	/** bits 7-4 bit size of PIN length in APDU, bits 3-0 PIN block size in bytes after justification and formatting  */
	var bmPINBlockString: Byte = 0

	/** bits 7-5 RFU, bit 4 set if system units are bytes clear if system units are bits, bits 3-0 PIN length position in system units  */
	var bmPINLengthFormat: Byte = 0

	/** XXYY, where XX is minimum PIN size in digits, YY is maximum  */
	private var wPINMaxExtraDigit: Short = 0

	/** Conditions under which PIN entry should be considered complete.
	 *
	 * The value is a bit wise OR operation:
	 *  * 0x1 Max size reached
	 *  * 0x2 Validation key pressed
	 *  * 0x4 Timeout occurred  */
	private val bEntryValidationCondition: Byte = 0x2

	/** Number of messages to display for PIN verification management.
	 *
	 * The value is one of:
	 *  * 0x0 no string
	 *  * 0x1 Message indicated by msg idx
	 *  * 0xFF default CCID message  */
	private val bNumberMessage = 0x01.toByte()

	/** Language for messages  */
	private var wLangId = USBLangID.getCode(Locale.getDefault())

	/** Message index (should be 00).
	 *
	 * The first three messages should be as follows in the reader:
	 *  * 0x0 PIN insertion prompt: "ENTER PIN"
	 *  * 0x1 PIN modification prompt: "ENTER NEW PIN"
	 *  * 0x2 New PIN confirmation prompt: "CONFIRM NEW PIN"  */
	private val bMsgIndex: Byte = 0

	/** T=1 I-block prologue field to use (fill with 00)  */
	private val bTeoPrologue = byteArrayOf(0, 0, 0)

	/** length of Data to be sent to the ICC  */
	private var ulDataLength = 0

	/** Data to send to the ICC  */
	private var abData: ByteArray? = null

	init {
		// initialise content needed for serialisation
		prepareStructure(attributes, cmdTemplate)
	}

	var minPINSize: Byte
		get() = ((wPINMaxExtraDigit.toInt() shr 8) and 0xFF).toByte()
		set(minSize) {
			wPINMaxExtraDigit = ((wPINMaxExtraDigit.toInt() and 0x00FF) or (minSize.toInt() shl 8)).toShort()
		}

	var maxPINSize: Byte
		get() = (wPINMaxExtraDigit.toInt() and 0xFF).toByte()
		set(maxSize) {
			wPINMaxExtraDigit = ((wPINMaxExtraDigit.toInt() and 0xFF00) or maxSize.toInt()).toShort()
		}

	fun setData(data: ByteArray) {
		ulDataLength = data.size
		abData = data
	}

	fun setLang(l: Locale) {
		wLangId = USBLangID.getCode(l)
	}

	fun toBytes(): ByteArray {
		val o = ByteArrayOutputStream(42) // just a random magic number ^^
		// write all numbers to the stream
		o.write(bTimeOut.toInt())
		o.write(bTimeOut2.toInt())
		o.write(bmFormatString.toInt())
		o.write(bmPINBlockString.toInt())
		o.write(bmPINLengthFormat.toInt())
		o.write(this.maxPINSize.toInt())
		o.write(this.minPINSize.toInt())
		o.write(bEntryValidationCondition.toInt())
		o.write(bNumberMessage.toInt())
		// byte lang_low  = (byte) (wLangId & 0xFF);
		// byte lang_high = (byte) ((wLangId >> 8) & 0xFF);
		val langLow = ((wLangId.toInt() shr 8) and 0xFF).toByte()
		val langHigh = (wLangId.toInt() and 0xFF).toByte()
		o.write(langHigh.toInt())
		o.write(langLow.toInt())
		o.write(bMsgIndex.toInt())
		o.write(bTeoPrologue, 0, bTeoPrologue.size)
		val ulDataLengthBytes = IntegerUtils.toByteArray(ulDataLength)
		for (i in ulDataLengthBytes.indices.reversed()) {
			o.write(ulDataLengthBytes[i].toInt())
		}
		// write missing bytes to length field
		for (i in ulDataLengthBytes.size..3) {
			o.write(0)
		}
		if (ulDataLength > 0) {
			o.write(abData!!, 0, abData!!.size)
		}

		val result = o.toByteArray()
		return result
	}
}
