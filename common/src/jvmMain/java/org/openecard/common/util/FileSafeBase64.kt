/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import org.openecard.bouncycastle.util.Strings
import org.openecard.bouncycastle.util.encoders.Base64Encoder
import org.openecard.bouncycastle.util.encoders.Encoder
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * This class is a Base64 encodeDataDatar/ decoder with a filename safe alphabet.
 * The following characters are different than in the original [Base64
 * specification (RFC 4648)](http://tools.ietf.org/html/rfc4648). This version is described in sec. 5 of the RFC.<br></br>
 * <table>
 * <caption>Character Replacement</caption>
 * <tr><td>+</td><td>-&gt;</td><td>-</td></tr>
 * <tr><td>/</td><td>-&gt;</td><td>_</td></tr>
</table> *
 *
 *
 * This file also implements static methods which are derived from the BouncyCastle Base64 class.
 *
 * @author Tobias Wich
 */
class FileSafeBase64 : Base64Encoder() {
	init {
		encodingTable[62] = '-'.code.toByte()
		encodingTable[63] = '_'.code.toByte()
		// update decoding table
		initialiseDecodingTable()
	}

	companion object {
		private val ENCODER: Encoder = FileSafeBase64()

		/**
		 * Encode the input data producing a base 64 encoded string.
		 *
		 * @param data Data to be encoded as base 64.
		 * @return String containing the base 64 encoded data.
		 */
		fun toBase64String(data: ByteArray): String {
			val encoded = encodeData(data)
			return Strings.fromByteArray(encoded)
		}

		/**
		 * Encode the input data producing a base 64 encoded byte array.
		 *
		 * @param data Data to be encoded as base 64.
		 * @return Byte array containing the base 64 encoded data.
		 */
		fun encodeData(data: ByteArray): ByteArray {
			try {
				val inLength = data.size
				val outLength = (inLength + 2) / 3 * 4
				val bOut = ByteArrayOutputStream(outLength)
				encodeData(data, bOut)
				return bOut.toByteArray()
			} catch (ex: IOException) {
				throw RuntimeException("Failed to write encoded data to memory.", ex)
			}
		}

		/**
		 * Encode the input data to base 64 writing it to the given output stream.
		 *
		 * @param data Data to be encoded as base 64.
		 * @param out Stream to write the data to.
		 * @return The number of bytes produced.
		 * @throws java.io.IOException Thrown when writing to the stream failed.
		 */
		@Throws(IOException::class)
		fun encodeData(
			data: ByteArray,
			out: OutputStream?,
		): Int {
			val inLength = data.size
			return ENCODER.encode(data, 0, inLength, out)
		}

		/**
		 * Decode the base 64 encoded data and write it to a byte array.
		 * Whitespace will be ignored.
		 *
		 * @param data Base 64 encoded data.
		 * @return A byte array representing the decoded data.
		 */
		fun decodeData(data: ByteArray): ByteArray {
			try {
				val outLength = data.size / 4 * 3
				val bOut = ByteArrayOutputStream(outLength)
				decodeData(data, bOut)
				return bOut.toByteArray()
			} catch (ex: IOException) {
				throw RuntimeException("Failed to write decoded data to memory.", ex)
			}
		}

		/**
		 * Decode the base 64 encoded data writing it to the given output stream.
		 * Whitespace characters will be ignored.
		 *
		 * @param data Base 64 encoded data.
		 * @param out Stream to write the data to.
		 * @return The number of bytes produced.
		 * @throws java.io.IOException
		 */
		@Throws(IOException::class)
		fun decodeData(
			data: ByteArray,
			out: OutputStream?,
		): Int = ENCODER.decode(data, 0, data.size, out)

		/**
		 * Decode the base 64 encoded string and write it to a byte array.
		 * Whitespace will be ignored.
		 *
		 * @param data Base 64 encoded data.
		 * @return A byte array representing the decoded data.
		 */
		fun decodeData(data: String): ByteArray {
			try {
				val outLength = data.length / 4 * 3
				val bOut = ByteArrayOutputStream(outLength)
				decodeData(data, bOut)
				return bOut.toByteArray()
			} catch (ex: IOException) {
				throw RuntimeException("Failed to write decoded data to memory.", ex)
			}
		}

		/**
		 * Decode the base 64 encoded string writing it to the given output stream.
		 * Whitespace characters will be ignored.
		 *
		 * @param data Base 64 encoded data.
		 * @param out Stream to write the data to.
		 * @return The number of bytes produced.
		 * @throws java.io.IOException
		 */
		@Throws(IOException::class)
		fun decodeData(
			data: String?,
			out: OutputStream?,
		): Int = ENCODER.decode(data, out)
	}
}
