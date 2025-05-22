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
		assertEquals(StatusWord.COUNTER_ENCODED, status.type)
		assertTrue { status.type.isWarning }
		assertFalse { status.type.isNormal }
		assertFalse { status.type.isCheckingError }
		assertFalse { status.type.isExecutionError }
		assertEquals(StatusWord.NVMEM_CHANGED_WARN, status.type.parentCode)
		assertNull(status.type.parentCode?.parentCode)
	}
}
