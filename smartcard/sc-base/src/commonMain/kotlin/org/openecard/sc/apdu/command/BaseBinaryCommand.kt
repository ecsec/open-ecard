package org.openecard.sc.apdu.command

import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.common.removeLeadingZeros
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.serialization.toPrintable

abstract class BaseBinaryCommand(
	val insBase: UByte,
	val offset: ULong = 0u,
	val efIdentifier: UShort = 0u,
	val proprietaryDataObject: Boolean = false,
) : IsoCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	internal fun buildParameters(): BinaryParameters {
		val noEf = efIdentifier.toUInt() == 0x0u
		val isShortEf = efIdentifier < 0x1Fu && !noEf
		val isByteOffset = offset <= 255u
		val isShortOffset = offset <= 32767u

		if (isShortEf && isByteOffset) {
			val ins = insBase
			val p1 = 0x80u or efIdentifier.toUInt()
			val p2 = offset.toUByte()
			return BinaryParameters(ins, p1.toUByte(), p2)
		} else if (noEf && isShortOffset) {
			val ins = insBase
			val p12 = offset.toUShort().toUByteArray()
			return BinaryParameters(ins, p12[0], p12[1])
		} else {
			val ins = insBase or 1u
			val p12 = efIdentifier.toUByteArray()
			// strip leading 0 bytes from offset value
			val offsetVal = offset.toUByteArray().removeLeadingZeros().toPrintable()
			val offsetTlv = TlvPrimitive(Tag.forTagNumWithClass(0x54u), offsetVal)
			return BinaryParameters(ins, p12[0], p12[1], offsetTlv)
		}
	}

	internal class BinaryParameters(
		val ins: UByte,
		val p1: UByte,
		val p2: UByte,
		val offsetTlv: Tlv? = null,
	)
}
