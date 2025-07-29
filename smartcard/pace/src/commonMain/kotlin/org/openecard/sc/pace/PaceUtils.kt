package org.openecard.sc.pace

import org.openecard.sc.iface.SequenceCounterOverflow
import org.openecard.utils.common.toUByteArray
import kotlin.math.sign

@OptIn(ExperimentalUnsignedTypes::class)
@Throws(SequenceCounterOverflow::class)
fun Long.toSequenceCounter(targetLength: Int = 16): UByteArray {
	require(targetLength > 0) { "Non-positive target length for SSC specified" }
	if (this.sign == -1) {
		throw SequenceCounterOverflow("Send Sequence Counter overflow.")
	}
	val bytes = this.toULong().toUByteArray()
	return fillSequenceCounterToTargetSize(bytes, targetLength, false)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun fillSequenceCounterToTargetSize(
	bytes: UByteArray,
	targetLength: Int,
	bytesIsMinimum: Boolean,
): UByteArray =
	if (bytes.size < targetLength) {
		UByteArray(targetLength - bytes.size) + bytes
	} else if (bytes.size == targetLength) {
		bytes
	} else {
		if (!bytesIsMinimum) {
			val minimalBytes = bytes.dropWhile { it == 0u.toUByte() }.toUByteArray()
			fillSequenceCounterToTargetSize(minimalBytes, targetLength, true)
		} else {
			throw SequenceCounterOverflow("Send Sequence Counter is bigger than serialized value permits.")
		}
	}
