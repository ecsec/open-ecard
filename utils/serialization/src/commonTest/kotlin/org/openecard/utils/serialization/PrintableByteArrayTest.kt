package org.openecard.utils.serialization

import kotlin.test.Test
import kotlin.test.assertEquals

class PrintableByteArrayTest {
	@Test
	fun `compare PrintableByteArray`() {
		assertEquals(byteArrayOf(1).toPrintable(), byteArrayOf(1).toPrintable())
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `compare PrintableUByteArray`() {
		assertEquals(ubyteArrayOf(1u).toPrintable(), ubyteArrayOf(1u).toPrintable())
	}
}
