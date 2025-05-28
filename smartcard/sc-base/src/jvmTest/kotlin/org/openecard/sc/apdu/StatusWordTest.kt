package org.openecard.sc.apdu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StatusWordTest {
	@Test
	fun `map sw to enum`() {
		val sw: UShort = 0x63C5u
		val status = sw.toStatusWord()
		assertEquals(sw, status.sw)
		assertEquals(0x63u, status.sw1)
		assertEquals(0xC5u, status.sw2)
		assertEquals(StatusWord.COUNTER_ENCODED, status.type)
		assertTrue { status.type.isWarning }
		assertFalse { status.type.isNormal }
		assertFalse { status.type.isCheckingError }
		assertFalse { status.type.isExecutionError }
		assertEquals(StatusWord.NVMEM_CHANGED_WARN, status.type.parentCode)
		assertNull(status.type.parentCode?.parentCode)
		assertEquals(0x5u, status.parameter)
	}

	@Test
	fun `sw no parameter 6CXX`() {
		val sw: UShort = 0x6CC5u
		val status = sw.toStatusWord()
		assertNull(status.parameter)
	}
}
