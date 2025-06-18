package org.openecard.sc.pace

import com.ionspin.kotlin.bignum.integer.BigInteger
import org.openecard.sc.iface.SequenceCounterOverflow

@OptIn(ExperimentalUnsignedTypes::class)
@Throws(SequenceCounterOverflow::class)
fun BigInteger.toSequenceCounter(targetLength: Int = 16): UByteArray {
	val bytes = this.toUByteArray()
	return if (bytes.size < targetLength) {
		// prepend zeroes
		UByteArray(targetLength - bytes.size) + bytes
	} else if (bytes.size == targetLength) {
		bytes
	} else {
		throw SequenceCounterOverflow("Send Sequence Counter overflow.")
	}
}
