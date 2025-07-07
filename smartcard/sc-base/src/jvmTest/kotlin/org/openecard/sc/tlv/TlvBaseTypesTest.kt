package org.openecard.sc.tlv

import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TlvBaseTypesTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test ULong 0`() {
		val v1 = 0uL
		val v1Tlv = v1.toTlv()
		assertContentEquals(hex("020100"), v1Tlv.toBer())
		assertEquals(v1, v1Tlv.toULong())
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test ULong max`() {
		val v1 = ULong.MAX_VALUE
		val v1Tlv = v1.toTlv()
		assertContentEquals(hex("0208FFFFFFFFFFFFFFFF"), v1Tlv.toBer())
		assertEquals(v1, v1Tlv.toULong())
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test UInt max`() {
		val v1 = UInt.MAX_VALUE
		val v1Tlv = v1.toTlv()
		assertContentEquals(hex("0204FFFFFFFF"), v1Tlv.toBer())
		assertEquals(v1, v1Tlv.toUInt())
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test ULong`() {
		val v1 = 1234uL
		val v1Tlv = v1.toTlv()
		assertContentEquals(hex("020204D2"), v1Tlv.toBer())
		assertEquals(v1, v1Tlv.toULong())
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test UInt`() {
		val v1 = 1234u
		val v1Tlv = v1.toTlv()
		assertContentEquals(hex("020204D2"), v1Tlv.toBer())
		assertEquals(v1, v1Tlv.toUInt())
	}
}
