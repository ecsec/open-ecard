package org.openecard.sc.pace

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.sm.CommandStage
import org.openecard.sc.apdu.sm.ResponseStage
import org.openecard.sc.apdu.sm.SmBasicTags
import org.openecard.sc.apdu.sm.pad
import org.openecard.sc.apdu.sm.segmentAuthTags
import org.openecard.sc.iface.CryptographicChecksumMissing
import org.openecard.sc.iface.CryptographicChecksumWrong
import org.openecard.sc.pace.crypto.cmacKey
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
		cmacKey(macKeyBytes)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processCommand(
		smApdu: CommandApdu,
		dos: List<Tlv>,
	): List<Tlv> {
		// increment counter for next iteration
		ssc++

		// padding must be applied for a chain of sequential protected tags
		val paddedDataItems =
			buildList<UByteArray> {
				if (smApdu.classByteInterIndustry?.sm == SecureMessagingIndication.SM_W_HEADER) {
					add(smApdu.header)
				}

				// add all tags
				dos.segmentAuthTags().forEach { seq ->
					val dataObjs = seq.map { it.toBer() }
					add(dataObjs.mergeToArray())
				}
			}

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
			val authData =
				dos.segmentAuthTags().map { seq ->
					val dataObjs = seq.map { it.toBer() }
					dataObjs.mergeToArray()
				}
			val calcMac = calculateMac(authData)
			if (!calcMac.contentEquals(sentMac)) {
				throw CryptographicChecksumWrong("Calculated MAC does not match received MAC")
			} else {
				return dos
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun calculateMac(dataItems: List<UByteArray>): UByteArray {
		val sscInit = ssc.toSequenceCounter(blockSize)

		val gen = macKey.signer()
		gen.update(sscInit.toByteArray())
		val paddedDataItems = dataItems.map { it.pad(blockSize) }
		paddedDataItems.forEach { gen.update(it.toByteArray()) }

		val mac = gen.sign()
		return mac.toUByteArray().sliceArray(0 until 8)
	}
}
