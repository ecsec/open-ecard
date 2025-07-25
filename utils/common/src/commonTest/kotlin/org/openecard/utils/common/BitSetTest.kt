package org.openecard.utils.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BitSetTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `one byte set and retrieve`() {
		val bs = bitSetOf(0x00u)
		for (i in 0 until 8) {
			assertEquals(false, bs[i])
		}
		bs[0] = true
		bs[1] = false
		bs[5] = true
		for (i in 0 until 8) {
			if (i in listOf(0, 5)) {
				assertEquals(true, bs[i])
			} else {
				assertEquals(false, bs[i])
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test slice`() {
		val start = bitSetOf(0x12u, 0x34u, 0x56u, 0x78u)
		assertContentEquals(hex("07"), start.slice(31 downTo 28).toUByteArray())
		assertContentEquals(hex("01"), start.slice(IntProgression.fromClosedRange(31, 28, -2)).toUByteArray())
	}
}
