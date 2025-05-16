package org.openecard.utils.common

/**
 * Reverses the array when the given condition is true.
 * This is a convenience funtion, so an extra if around the reverse is not needed.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.reversedIf(cond: () -> Boolean): UByteArray =
	if (cond()) {
		this.reversedArray()
	} else {
		this
	}
