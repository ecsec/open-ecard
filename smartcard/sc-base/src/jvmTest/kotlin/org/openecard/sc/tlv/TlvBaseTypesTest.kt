package org.openecard.sc.tlv

import dev.whyoleg.cryptography.serialization.asn1.Der
import kotlinx.serialization.decodeFromByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class TlvBaseTypesTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test ULong`() {
		val v1 = 1234uL
		val v1Tlv = v1.toTlv()
		assertEquals(v1, v1Tlv.toULong())
		assertEquals(v1, Der.decodeFromByteArray(v1Tlv.toBer().toByteArray()))
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test UInt`() {
		val v1 = 1234u
		val v1Tlv = v1.toTlv()
		assertEquals(v1, v1Tlv.toUInt())
		assertEquals(v1, Der.decodeFromByteArray(v1Tlv.toBer().toByteArray()))
	}
}
