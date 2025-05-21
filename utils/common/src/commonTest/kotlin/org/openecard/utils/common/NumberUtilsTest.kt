package org.openecard.utils.common

import kotlin.test.Test
import kotlin.test.assertContentEquals

class NumberUtilsTest {
	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	@Test
	fun testLongToSparseUByteArray() {
		assertContentEquals("00".hexToUByteArray(), 0x00uL.toSparseUByteArray())

		assertContentEquals("FF".hexToUByteArray(), 0xFFuL.toSparseUByteArray())

		assertContentEquals("AAFF".hexToUByteArray(), 0xAAFFuL.toSparseUByteArray())
		assertContentEquals("FFAA".hexToUByteArray(), 0xAAFFuL.toSparseUByteArray(bigEndian = false))

		assertContentEquals("017F".hexToUByteArray(), 0xFFuL.toSparseUByteArray(numBits = 7))
		assertContentEquals("0101".hexToUByteArray(), 0x03uL.toSparseUByteArray(numBits = 1))
		assertContentEquals("7F01".hexToUByteArray(), 0xFFuL.toSparseUByteArray(numBits = 7, bigEndian = false))
	}
}
