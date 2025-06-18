package org.openecard.sc.pace

import com.ionspin.kotlin.bignum.integer.toBigInteger
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.sm.CommandStage
import org.openecard.sc.apdu.sm.PaddingContentIndicator
import org.openecard.sc.apdu.sm.ResponseStage
import org.openecard.sc.apdu.sm.SmBasicTags
import org.openecard.sc.apdu.sm.pad
import org.openecard.sc.apdu.sm.unpad
import org.openecard.sc.iface.UnsupportedPadding
import org.openecard.sc.iface.UnsupportedSmDo
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.common.isOdd
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.serialization.toPrintable

class EncryptionStage(
	encKeyBytes: ByteArray,
	val paddingIndicator: PaddingContentIndicator = PaddingContentIndicator.CbcPadding,
	val blockSize: Int = 16,
) : CommandStage,
	ResponseStage {
	override val isOptional = false

	private var ssc = 0.toBigInteger()
	val encKey by lazy {
		crypto.get(AES.CBC).keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, encKeyBytes)
	}

	@OptIn(DelicateCryptographyApi::class)
	val ivKey by lazy {
		crypto.get(AES.ECB).keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, encKeyBytes)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processCommand(
		smApdu: CommandApdu,
		dos: List<Tlv>,
	): List<Tlv> {
		// increment counter for next iteration
		ssc++

		return dos.map {
			if (it.tag in SmBasicTags.plain.tags) {
				// replace these tags with encrypted data
				val data = it.contentAsBytesBer
				val encData = encryptData(data)

				// determine new tag
				val newTag =
					if (it.tag.tagNumWithClass.isOdd) {
						SmBasicTags.paddingCrypto.auth.tag
					} else {
						SmBasicTags.paddingCrypto.unauth.tag
					}

				// return new element into the map
				TlvPrimitive(newTag, encData.toPrintable())
			} else {
				// don't touch this element
				it
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processResponse(dos: List<Tlv>): List<Tlv> {
		// increment counter for next iteration
		ssc++

		return dos.map {
			if (it.tag in SmBasicTags.paddingCrypto.tags) {
				val encData = it.contentAsBytesBer
				val plainData = decryptData(encData)
				TlvPrimitive(SmBasicTags.plain.unauth.tag, plainData.toPrintable())
			} else if (it.tag in SmBasicTags.plainCryptoNoSmDos.tags) {
				throw UnsupportedSmDo("DO with plain crypto without SM DOs encountered ${it.tag}")
			} else if (it.tag in SmBasicTags.plainCryptoWithSmDos.tags) {
				throw UnsupportedSmDo("DO with plain crypto with SM DOs encountered ${it.tag}")
			} else {
				// don't touch non encrypted elements
				it
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class, DelicateCryptographyApi::class, ExperimentalStdlibApi::class)
	private fun encryptData(data: UByteArray): UByteArray {
		val iv = getIv()
		val paddedData =
			when (paddingIndicator) {
				PaddingContentIndicator.NoIndication, PaddingContentIndicator.CbcPadding -> data.pad(blockSize)
				PaddingContentIndicator.NoPadding -> data
				else -> throw UnsupportedPadding("Unsupported padding indicator byte (${paddingIndicator.byte.toHexString()})")
			}
		val tbeData = paddingIndicator.byte.toUByteArray() + paddedData
		return encKey.cipher(false).encryptWithIvBlocking(iv, tbeData.toByteArray()).toUByteArray()
	}

	@OptIn(ExperimentalUnsignedTypes::class, DelicateCryptographyApi::class, ExperimentalStdlibApi::class)
	private fun decryptData(encData: UByteArray): UByteArray {
		val iv = getIv()
		val decData = encKey.cipher(false).decryptWithIvBlocking(iv, encData.toByteArray())
		val pi = PaddingContentIndicator.fromIndicatorByte(decData[0].toUByte())
		val decDataNoPi = decData.sliceArray(1 until decData.size).toUByteArray()
		return when (pi) {
			PaddingContentIndicator.NoIndication, PaddingContentIndicator.CbcPadding -> decDataNoPi.unpad()
			PaddingContentIndicator.NoPadding -> decDataNoPi
			else -> throw UnsupportedPadding("Unsupported padding indicator byte (${pi.byte.toHexString()})")
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class, DelicateCryptographyApi::class)
	private fun getIv(): ByteArray {
		val sscData = ssc.toSequenceCounter(blockSize)
		return ivKey.cipher(false).encryptBlocking(sscData.toByteArray())
	}
}
