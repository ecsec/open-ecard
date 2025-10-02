package org.openecard.utils.common

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse

class RandBitsTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testRandomBits() {
		val random = Random.Default

		assertFails {
			random.nextBitField(-16)
		}
		assertFails {
			random.nextBitField(0)
		}
		assertEquals(
			1,
			random.nextBitField(1).size,
		)
		assertEquals(
			1,
			random.nextBitField(7).size,
		)
		assertEquals(
			1,
			random.nextBitField(8).size,
		)
		assertEquals(
			2,
			random.nextBitField(9).size,
		)

		val oneByte = random.nextBitField(3)[0]
		assertEquals(
			oneByte,
			((oneByte.toUInt() shl 8 - 3) shr 8 - 3).toUByte(),
		)

		val a = random.nextBitField(32 * 8)
		val b = random.nextBitField(32 * 8)

		// I guess this assertion isn't completely right but testing random is hard.
		assertFalse {
			a.contentEquals(b)
		}
	}
}
