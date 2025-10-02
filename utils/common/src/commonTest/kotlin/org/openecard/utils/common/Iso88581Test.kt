package org.openecard.utils.common

import org.openecard.utils.common.Iso88591.decodeIso88591
import org.openecard.utils.common.Iso88591.encodeIso88591
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Iso88581Test {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun encodeIsoStrings() {
		val ref1 = "Hello World!"
		assertContentEquals(ref1.encodeToByteArray(), ref1.encodeIso88591())
		assertEquals(ref1, ref1.encodeIso88591().decodeIso88591())

		val ref2 = "Fö"
		assertContentEquals(hex("46F6").toByteArray(), ref2.encodeIso88591())
		assertEquals(ref2, ref2.encodeIso88591().decodeIso88591())
	}
}
