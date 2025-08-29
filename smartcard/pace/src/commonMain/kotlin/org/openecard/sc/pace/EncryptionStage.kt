package org.openecard.sc.pace

import dev.whyoleg.cryptography.DelicateCryptographyApi
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.sm.CommandStage
import org.openecard.sc.apdu.sm.PaddingContentIndicator
import org.openecard.sc.apdu.sm.ResponseStage
import org.openecard.sc.apdu.sm.SmBasicTags
import org.openecard.sc.apdu.sm.pad
import org.openecard.sc.apdu.sm.unpad
import org.openecard.sc.iface.UnsupportedPadding
import org.openecard.sc.iface.UnsupportedSmDo
import org.openecard.sc.pace.crypto.aesCbcKey
import org.openecard.sc.pace.crypto.aesEcbKey
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

	private var ssc = 0L
	val encKey by lazy {
		aesCbcKey(encKeyBytes)
	}

	@OptIn(DelicateCryptographyApi::class)
	val ivKey by lazy {
		aesEcbKey(encKeyBytes)
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
		val encData = encKey.cipher(false).encryptWithIv(iv, paddedData.toByteArray()).toUByteArray()
		val resultData = paddingIndicator.byte.toUByteArray() + encData
		return resultData
	}

	@OptIn(ExperimentalUnsignedTypes::class, DelicateCryptographyApi::class, ExperimentalStdlibApi::class)
	private fun decryptData(encData: UByteArray): UByteArray {
		val iv = getIv()
		val pi = PaddingContentIndicator.fromIndicatorByte(encData[0])
		val encDataNoPi = encData.sliceArray(1 until encData.size).toUByteArray()
		val decData = encKey.cipher(false).decryptWithIv(iv, encDataNoPi.toByteArray()).toUByteArray()
		return when (pi) {
			PaddingContentIndicator.NoIndication, PaddingContentIndicator.CbcPadding -> decData.unpad()
			PaddingContentIndicator.NoPadding -> decData
			else -> throw UnsupportedPadding("Unsupported padding indicator byte (${pi.byte.toHexString()})")
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class, DelicateCryptographyApi::class)
	private fun getIv(): ByteArray {
		val sscData = ssc.toSequenceCounter(blockSize)
		return ivKey.cipher(false).encrypt(sscData.toByteArray())
	}
}
