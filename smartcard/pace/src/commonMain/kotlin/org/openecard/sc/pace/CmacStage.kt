package org.openecard.sc.pace

import dev.whyoleg.cryptography.algorithms.AES
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.sm.CommandStage
import org.openecard.sc.apdu.sm.ResponseStage
import org.openecard.sc.apdu.sm.SmBasicTags
import org.openecard.sc.apdu.sm.onlyAuthTags
import org.openecard.sc.apdu.sm.pad
import org.openecard.sc.iface.CryptographicChecksumMissing
import org.openecard.sc.iface.CryptographicChecksumWrong
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.common.mergeToArray
import org.openecard.utils.serialization.toPrintable

class CmacStage(
	macKeyBytes: ByteArray,
	val blockSize: Int = 16,
) : CommandStage,
	ResponseStage {
	override val isOptional = false

	private var ssc = 0L
	val macKey by lazy {
		crypto.get(AES.CMAC).keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, macKeyBytes)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processCommand(
		smApdu: CommandApdu,
		dos: List<Tlv>,
	): List<Tlv> {
		// increment counter for next iteration
		ssc++

		val dataItems =
			buildList<UByteArray> {
				if (smApdu.classByteInterIndustry?.sm == SecureMessagingIndication.SM_W_HEADER) {
					add(smApdu.header)
				}

				// add all tags
				dos.onlyAuthTags().forEach { add(it.contentAsBytesBer) }
			}

		val paddedDataItems = dataItems.map { it.pad(blockSize) }
		val mac = calculateMac(paddedDataItems)
		val macDo = TlvPrimitive(SmBasicTags.mac.tag, mac.toPrintable())

		return dos + macDo
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processResponse(dos: List<Tlv>): List<Tlv> {
		// increment counter for next iteration
		ssc++

		val sentMac = dos.find { it.tag == SmBasicTags.mac.tag }?.contentAsBytesBer

		if (sentMac == null) {
			if (isOptional) {
				// continue without error
				return dos
			} else {
				throw CryptographicChecksumMissing("MAC missing in DOs")
			}
		} else {
			// check mac
			val authData = dos.onlyAuthTags().map { it.contentAsBytesBer }.mergeToArray()
			val paddedAuthData = authData.pad(blockSize)
			val calcMac = calculateMac(listOf(paddedAuthData))
			if (!calcMac.contentEquals(sentMac)) {
				throw CryptographicChecksumWrong("Calculated MAC does not match received MAC")
			} else {
				return dos
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun calculateMac(paddedDataItems: List<UByteArray>): UByteArray {
		val sscInit = ssc.toSequenceCounter(blockSize)
		val data = (listOf(sscInit) + paddedDataItems).mergeToArray()

		val gen = macKey.signatureGenerator()
		val mac = gen.generateSignatureBlocking(data.toByteArray())
		return mac.toUByteArray()
	}
}
