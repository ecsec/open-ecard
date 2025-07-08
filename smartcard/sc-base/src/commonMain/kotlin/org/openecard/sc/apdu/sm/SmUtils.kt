package org.openecard.sc.apdu.sm

import org.openecard.sc.tlv.Tlv

/**
 * Padding the data.
 *
 * @param blockSize Block size
 * @return Padded data
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.pad(blockSize: Int): UByteArray {
	val data = this
	// as padding is mandatory, the result will contain an extra empty block in case the data is already a multiple of the block size
	val result = UByteArray(data.size + (blockSize - data.size % blockSize))
	data.copyInto(result)
	result[data.size] = PADDING_BYTE

	return result
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.unpad(): UByteArray {
	for (i in size - 1 downTo 0) {
		val b = this[i]
		if (b == 0u.toUByte()) {
			continue
		} else if (b == PADDING_BYTE) {
			return this.sliceArray(0 until i)
		} else {
			throw IllegalArgumentException(
				"Given data does not conform to padding, '$b' found where only 0x80 and 0x00 are expected",
			)
		}
	}

	throw IllegalArgumentException(
		"Given data does not contain padding",
	)
}

// ISO/IEC 7816-4 padding tag
val PADDING_BYTE = 0x80u.toUByte()

fun List<Tlv>.onlyAuthTags(): List<Tlv> =
	filter {
		it.isAuthTag()
	}

fun List<Tlv>.segmentAuthTags(): List<List<Tlv>> {
	val input = this
	return buildList {
		// collect all consecutive auth tags in a sublist
		var buf = mutableListOf<Tlv>()
		input.forEach {
			if (it.isAuthTag()) {
				buf.add(it)
			} else {
				if (buf.isNotEmpty()) {
					// copy elements to the list
					add(buf)
					buf = mutableListOf()
				}
			}
		}

		// there may be more elements after the iteration
		if (buf.isNotEmpty()) {
			add(buf)
		}
	}
}

fun Tlv.isAuthTag(): Boolean {
	val cls = this.tag.tagNumWithClass
	// odd or not in range 80-BF
	return (cls.mod(2u) == 1u) || (cls < 0x80u && cls > 0xBFu)
}
