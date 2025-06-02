package org.openecard.utils.common

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArrayUtilsTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test masked contains`() {
		val data = hex("FF11FF")
		val search = hex("11F0")
		val mask = hex("FFF0")
		assertTrue { data.maskedContains(search, mask, offset = null) }
		assertTrue { data.maskedContains(search, mask, offset = 1) }
		assertFalse { data.maskedContains(search, mask, offset = 0) }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test masked contains without mask`() {
		val data = hex("FF11FF")
		val search = hex("11FF")
		assertTrue { data.maskedContains(search, offset = null) }
		assertTrue { data.maskedContains(search, offset = 1) }
		assertFalse { data.maskedContains(search, offset = 0) }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test masked contains invalid length`() {
		val data = hex("FF11FF")
		val search = hex("11FF")
		val mask = hex("FFF000")
		assertFails { data.maskedContains(hex("00"), offset = 3) }
		assertFails { data.maskedContains(hex("00000000"), offset = null) }
		assertFails { data.maskedContains(hex("00"), hex("FFFF"), offset = null) }
	}
}
