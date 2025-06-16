package org.openecard.sc.apdu.sm

import org.openecard.sc.apdu.CommandApdu

@OptIn(ExperimentalUnsignedTypes::class)
fun CommandApdu.paddedHeader(blockSize: Int): UByteArray = header.pad(blockSize)

/**
 * Padding the data.
 *
 * @param blockSize Block size
 * @return Padded data
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun UByteArray.pad(blockSize: Int): UByteArray {
	val data = this
	// as padding is mandatory, the result will contain an extra empty block in case the data is already a multiple of the block size
	val result = UByteArray(data.size + (blockSize - data.size % blockSize))
	data.copyInto(result)
	result[data.size] = PADDING_BYTE

	return result
}

// ISO/IEC 7816-4 padding tag
val PADDING_BYTE = 0x80u.toUByte()
