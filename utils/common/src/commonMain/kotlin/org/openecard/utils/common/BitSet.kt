package org.openecard.utils.common

import kotlin.math.absoluteValue

class BitSet internal constructor(
	private val bytes: MutableList<UByte>,
	val bitSize: Long,
) : Iterable<Boolean> {
	@Throws(IndexOutOfBoundsException::class)
	operator fun get(index: Int): Boolean = get(index.toLong())

	@Throws(IndexOutOfBoundsException::class)
	operator fun get(index: Long): Boolean {
		val byteIdxLong = index / 8
		require(byteIdxLong <= Int.MAX_VALUE)
		val byteIdx = byteIdxLong.toInt()
		val bitIdx = index.mod(8)

		val b = bytes[byteIdx]
		return ((b.toInt() shr bitIdx) and 0x01) == 1
	}

	@Throws(IndexOutOfBoundsException::class)
	operator fun set(
		index: Int,
		bit: Boolean,
	) = set(index.toLong(), bit)

	@Throws(IndexOutOfBoundsException::class)
	operator fun set(
		index: Long,
		bit: Boolean,
	) {
		val byteIdxLong = index / 8
		require(byteIdxLong <= Int.MAX_VALUE)
		val byteIdx = byteIdxLong.toInt()
		val bitIdx = index.mod(8)

		var b = bytes[byteIdx]
		val isBitSet = ((b.toInt() shr bitIdx) and 0x01) == 1
		if (isBitSet != bit) {
			val mask = (1 shl bitIdx).toUByte()
			b = b xor mask
			bytes[byteIdx] = b
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun slice(range: IntProgression): BitSet {
		val numBits = range.count()
		val numBytes = if ((numBits % 8) == 0) (numBits / 8) else (numBits / 8) + 1
		val result = bitSetOf(*UByteArray(numBytes))

		val posRange =
			if (range.step < 0) {
				range.reversed()
			} else {
				range
			}
		var resultIdx = 0
		posRange.forEach { i ->
			result[resultIdx] = this[i]
			resultIdx++
		}

		return result
	}

	override fun iterator(): Iterator<Boolean> =
		sequence {
			// this is very simple and has a lot of room for improvement
			for (i in 0 until bitSize) {
				yield(get(i))
			}
		}.iterator()

	@OptIn(ExperimentalUnsignedTypes::class)
	fun toUByteArray(): UByteArray = bytes.toUByteArray()

	fun copy(): BitSet = BitSet(bytes.toMutableList(), bitSize)
}

/**
 * Build a bitset of the given array.
 * The array is read in little endian mode, meaning index 0 contains bits 0 to 7.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun bitSetOf(vararg bytes: UByte): BitSet = BitSet(bytes.toMutableList(), bytes.size.toLong() * 8)
