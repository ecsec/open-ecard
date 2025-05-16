package org.openecard.utils.common

class BitSet internal constructor(
	private val bytes: MutableList<UByte>,
	private val bitSize: Long,
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
		return (b.toInt() and bitIdx) == 1
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
		b = b xor bitIdx.toUByte()
		bytes[byteIdx] = b
	}

	override fun iterator(): Iterator<Boolean> =
		sequence {
			// this is very simple and has a lot of room for improvement
			for (i in 0 until bitSize) {
				yield(get(i))
			}
		}.iterator()
}

/**
 * Build a bitset of the given array.
 * The array is read in little endian mode, meaning index 0 contains bits 0 to 7.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun bitSetOf(vararg bytes: UByte): BitSet = BitSet(bytes.toMutableList(), bytes.size.toLong() * 8)
