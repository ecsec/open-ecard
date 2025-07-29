package org.openecard.utils.common

import kotlin.test.Test
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
}
