/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.ifd.protocol.pace

import org.openecard.bouncycastle.crypto.engines.AESEngine
import org.openecard.bouncycastle.crypto.macs.CMac
import org.openecard.bouncycastle.crypto.params.KeyParameter
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.ClassByte
import org.openecard.common.apdu.common.SecureMessagingIndication
import org.openecard.common.tlv.TLV
import org.openecard.common.util.ByteUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


// ISO/IEC 7816-4 padding tag
private const val PAD = 0x80.toByte()

fun BigInteger.toSSCBytes(): ByteArray {
	val ssc = this.toByteArray()
	if (ssc.size < 16) {
		val result = ByteArray(16)
		return ssc.copyInto(result, result.size - ssc.size)
	} else {
		return ssc
	}
}

enum class ReadState {
	INIT,
	DATA,
	TRAILER,
	MAC,
	;

	fun selectNext(tag: Long): ReadState {
		return when (this) {
			INIT -> {
				when (tag) {
					0x81L -> DATA
					0x87L -> DATA
					0x99L -> TRAILER
					else -> throw IOException("Malformed Secure Messaging APDU")
				}
			}

			DATA -> {
				when (tag) {
					0x99L -> TRAILER
					else -> throw IOException("Malformed Secure Messaging APDU")
				}
			}

			TRAILER -> {
				when (tag) {
					0x8EL -> MAC
					else -> throw IOException("Malformed Secure Messaging APDU")
				}
			}

			MAC -> {
				throw IOException("Malformed Secure Messaging APDU")
			}
		}
	}
}

/**
 * Implements Secure Messaging according to ISO/IEC 7816-4.
 *
 * @author Moritz Horsch
 */
class SecureMessaging(
	// Keys for encryption and message authentication.
	private val keyMAC: ByteArray,
	private val keyENC: ByteArray,
) {
	// Send Sequence Counter. See BSI-TR-03110 section F.3.
	private var secureMessagingSSC = BigInteger.ZERO

	/**
	 * Encrypt the APDU.
	 *
	 * @param apdu APDU
	 * @return Encrypted APDU
	 * @throws Exception
	 */
	@Throws(Exception::class)
	fun encrypt(apdu: ByteArray): ByteArray {
		secureMessagingSSC++
		val commandAPDU = encrypt(apdu, secureMessagingSSC)
		secureMessagingSSC++

		return commandAPDU
	}

	/**
	 * Encrypt the APDU.
	 *
	 * @param apdu APDU
	 * @param secureMessagingSSC Secure Messaging Send Sequence Counter
	 * @return Encrypted APDU
	 * @throws Exception
	 */
	@Throws(Exception::class)
	private fun encrypt(apdu: ByteArray, secureMessagingSSC: BigInteger): ByteArray {
		val baos = ByteArrayOutputStream()
		val cAPDU = CardCommandAPDU(apdu)

		require(!cAPDU.isSecureMessaging) { "Malformed APDU." }

		var data = cAPDU.data
		val header = cAPDU.header
		val lc = cAPDU.lc
		val le = cAPDU.le
		val leEncoded = cAPDU.encodeLeField()

		// Indicate Secure Messaging
		// note: must be done before mac calculation
		header[0] = ClassByte.parse(header[0]).let {
			it.sm = SecureMessagingIndication.SM_W_HEADER
			it.byte
		}

		if (data.isNotEmpty()) {
			// Encrypt data
			val c = getCipher(secureMessagingSSC, Cipher.ENCRYPT_MODE)
			val paddedData = pad(data, 16)
			val dataEncrypted = c.doFinal(paddedData)

			// Add padding indicator 0x01
			val paddedEncryptedData = ByteUtils.concatenate(0x01.toByte(), dataEncrypted)

			val dataObject = TLV()
			dataObject.setTagNumWithClass(0x87.toByte())
			dataObject.value = paddedEncryptedData
			baos.write(dataObject.toBER())
		}

		// Write protected LE
		if (leEncoded.isNotEmpty()) {
			val leObject = TLV()
			leObject.setTagNumWithClass(0x97.toByte())
			leObject.value = leEncoded
			baos.write(leObject.toBER())
		}

		//
		// Calculate MAC
		//
		val cmac = getCMAC(secureMessagingSSC)
		var mac = ByteArray(cmac.macSize)

		synchronized(cmac) {
			val paddedHeader = pad(header, 16)
			cmac.update(paddedHeader, 0, paddedHeader.size)

			if (baos.size() > 0) {
				val paddedData = pad(baos.toByteArray(), 16)
				cmac.update(paddedData, 0, paddedData.size)
			}

			cmac.doFinal(mac, 0)
		}
		mac = ByteUtils.copy(mac, 0, 8)

		//
		// Build APDU
		val macStructure = TLV()
		macStructure.setTagNumWithClass(0x8E.toByte())
		macStructure.value = mac
		val secureData = ByteUtils.concatenate(baos.toByteArray(), macStructure.toBER())

		val secureCommand = CardCommandAPDU(header[0], header[1], header[2], header[3], secureData)
		// set LE explicitly to 0x00 or in case of extended length 0x00 0x00
		// always use extended length if there is an le field, as returned data can be longer due to encryption
		if (secureCommand.lc <= 0xFF && leEncoded.isEmpty()) {
			secureCommand.le = 256
		} else {
			secureCommand.le = 65536
		}

		return secureCommand.toByteArray()
	}

	/**
	 * Decrypt the APDU.
	 *
	 * @param response the response
	 * @return the byte[]
	 * @throws Exception the exception
	 */
	@Throws(Exception::class)
	fun decrypt(response: ByteArray): ByteArray {
		require(response.size >= 2) { "Secure Messaging Response APDU does not have a trailer." }
		val trailer = response.sliceArray(response.size - 2 until response.size)

		return when (ByteUtils.toHexString(trailer)) {
			"6987" -> throw GeneralSecurityException("Secure Messaging of ICC reports missing SM DOs (6987).")
			"6988" -> throw GeneralSecurityException("Secure Messaging of ICC reports invalid SM DOs (6988).")
			else -> {
				val responseNoTrailer = response.sliceArray(0 until response.size - 2)
				decrypt(responseNoTrailer, secureMessagingSSC)
			}
		}
	}

	/**
	 * Decrypt the APDU.
	 *
	 * @param response the response
	 * @param secureMessagingSSC the secure messaging ssc
	 * @return the byte[]
	 * @throws Exception the exception
	 */
	@Throws(Exception::class)
	private fun decrypt(responseNoTrailer: ByteArray, secureMessagingSSC: BigInteger): ByteArray {
		// Status bytes of the response APDU. MUST be 2 bytes.
		val statusBytes = ByteArray(2)
		// plain data 0x81
		var plainDataObject: ByteArray? = null
		// Padding-content indicator followed by cryptogram 0x87.
		var withPadding = false
		var encDataObject: ByteArray? = null
		// Cryptographic checksum 0x8E. MUST be 8 bytes.
		val macObject = ByteArray(8)

		val tlv = TLV.fromBER(responseNoTrailer)

		//
		// Read APDU structure
		// Case 1: DO99|DO8E|SW1SW2
		// Case 2: DO87|DO99|DO8E|SW1SW2
		// Case 3: DO99|DO8E|SW1SW2
		// Case 4: DO87|DO99|DO8E|SW1SW2
		//

		var state = ReadState.INIT
		for (nextTlv in tlv.asList()) {
			state = state.selectNext(nextTlv.tag.tagNumWithClass)
			when (state) {
				ReadState.INIT -> {
					throw IOException("Malformed Secure Messaging APDU")
				}

				ReadState.DATA -> {
					if (nextTlv.tag.tagNumWithClass == 0x81L) {
						plainDataObject = nextTlv.value
					} else if (nextTlv.tag.tagNumWithClass == 0x87L) {
						when (nextTlv.value.first()) {
							0x00.toByte(), 0x01.toByte() -> withPadding = true
							0x02.toByte() -> withPadding = false
							else -> throw UnsupportedOperationException(
								"Unsupported padding indicator byte 0x${
									nextTlv.value.first().toString(16)
								}"
							)
						}
						encDataObject = nextTlv.value.sliceArray(1 until nextTlv.value.size)
					}
				}

				ReadState.TRAILER -> {
					require(nextTlv.value.size == 2) { "Malformed Secure Messaging APDU" }
					nextTlv.value.copyInto(statusBytes)
				}

				ReadState.MAC -> {
					require(nextTlv.value.size == 8) { "Malformed Secure Messaging APDU" }
					nextTlv.value.copyInto(macObject)
				}
			}
		}

		// after reading everything, the state must be MAC
		require(state == ReadState.MAC) { "Malformed Secure Messaging APDU (parser state=$state)" }

		// Calculate MAC for verification
		val cmac = getCMAC(secureMessagingSSC)
		var mac = ByteArray(cmac.macSize)

		synchronized(cmac) {
			val macData = ByteArrayOutputStream()

			tlv.asList().dropLast(1).forEach { macData.write(it.toBER()) }

			val paddedData = pad(macData.toByteArray(), 16)
			cmac.update(paddedData, 0, paddedData.size)

			cmac.doFinal(mac, 0)
			mac = ByteUtils.copy(mac, 0, 8)
		}

		// Verify MAC
		if (!ByteUtils.compare(mac, macObject)) {
			throw GeneralSecurityException("Secure Messaging MAC verification failed")
		}

		val baos = ByteArrayOutputStream(responseNoTrailer.size)
		// Decrypt data
		if (encDataObject != null) {
			val c = getCipher(secureMessagingSSC, Cipher.DECRYPT_MODE)
			val dataDecrypted = c.doFinal(encDataObject)
			if (withPadding) {
				baos.write(unpad(dataDecrypted))
			} else {
				baos.write(dataDecrypted)
			}
		} else if (plainDataObject != null) {
			baos.write(plainDataObject)
		}

		// Add status code
		baos.write(statusBytes)

		return baos.toByteArray()
	}


	//
	// Cipher functions
	//

	/**
	 * Gets the cipher for de/encryption.
	 *
	 * @param smssc the Secure Messaging Send Sequence Counter
	 * @param mode the mode indicating de/encryption
	 * @return the cipher
	 * @throws Exception the exception
	 */
	@Throws(Exception::class)
	private fun getCipher(smssc: BigInteger, mode: Int): Cipher {
		val c = Cipher.getInstance("AES/CBC/NoPadding")
		val key: Key = SecretKeySpec(keyENC, "AES")
		val iv = getCipherIV(smssc.toSSCBytes())
		val algoPara: AlgorithmParameterSpec = IvParameterSpec(iv)

		c.init(mode, key, algoPara)

		return c
	}

	/**
	 * Gets the Initialization Vector (IV) for the cipher.
	 *
	 * @param smssc Secure Messaging Send Sequence Counter
	 * @return Initialization Vector
	 * @throws Exception
	 */
	@Throws(Exception::class)
	private fun getCipherIV(smssc: ByteArray): ByteArray {
		val c = Cipher.getInstance("AES/ECB/NoPadding")
		val key: Key = SecretKeySpec(keyENC, "AES")

		c.init(Cipher.ENCRYPT_MODE, key)

		return c.doFinal(smssc)
	}

	/**
	 * Gets the CMAC.
	 *
	 * @param smssc Secure Messaging Send Sequence Counter
	 * @return CMAC
	 */
	private fun getCMAC(smssc: BigInteger): CMac {
		val cmac = CMac(AESEngine())
		cmac.init(KeyParameter(keyMAC))
		val smsscBytes = smssc.toSSCBytes()
		cmac.update(smsscBytes, 0, smsscBytes.size)

		return cmac
	}


	//
	// ISO/IEC 7816-4 padding functions
	//

	/**
	 * Padding the data.
	 *
	 * @param data Unpadded data
	 * @param blockSize Block size
	 * @return Padded data
	 */
	private fun pad(data: ByteArray, blockSize: Int): ByteArray {
		val result = ByteArray(data.size + (blockSize - data.size % blockSize))
		System.arraycopy(data, 0, result, 0, data.size)
		result[data.size] = PAD

		return result
	}

	/**
	 * Unpadding the data.
	 *
	 * @param data Padded data
	 * @return Unpadded data
	 */
	private fun unpad(data: ByteArray): ByteArray {
		for (i in data.indices.reversed()) {
			if (data[i] == PAD) {
				return ByteUtils.copy(data, 0, i)
			}
		}

		return data
	}

}
