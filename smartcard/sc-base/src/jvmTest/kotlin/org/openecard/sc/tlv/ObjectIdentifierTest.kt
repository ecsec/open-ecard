package org.openecard.sc.tlv

import org.junit.jupiter.api.assertThrows
import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ObjectIdentifierTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test invalid oids`() {
		assertThrows<IllegalArgumentException> { "0".toObjectIdentifier().valueBytes }
		assertThrows<IllegalArgumentException> { "0.40".toObjectIdentifier().valueBytes }
		assertThrows<IllegalArgumentException> { "1.40".toObjectIdentifier().valueBytes }
		assertThrows<IllegalArgumentException> { "3.5".toObjectIdentifier().valueBytes }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test example oid`() {
		val oid = "2.999".toObjectIdentifier()
		val value = oid.valueBytes
		assertContentEquals(hex("8837"), value)
		val oid2 = value.toObjectIdentifier()
		assertEquals(oid, oid2)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test single byte roundtrip`() {
		val oid = "0.4.0.127.0.7.2.2.4.2.2".toObjectIdentifier()
		val value = oid.valueBytes
		assertContentEquals(hex("04007f00070202040202"), value)
		val oid2 = value.toObjectIdentifier()
		assertEquals(oid, oid2)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test multibyte roundtrip 2`() {
		val oid = "0.0.128".toObjectIdentifier()
		val value = oid.valueBytes
		assertContentEquals(hex("008100"), value)
		val oid2 = value.toObjectIdentifier()
		assertEquals(oid, oid2)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test multibyte roundtrip 3`() {
		val oid = "0.0.65793".toObjectIdentifier()
		val value = oid.valueBytes
		assertContentEquals(hex("00848201"), value)
		val oid2 = value.toObjectIdentifier()
		assertEquals(oid, oid2)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test TLV roundtrip`() {
		val oid = "0.0.65793".toObjectIdentifier()
		val tlvBytes = oid.tlvStandard.toBer()
		assertContentEquals(hex("060400848201"), tlvBytes)
		val oid2 = tlvBytes.toTlvBer().tlv.toObjectIdentifier()
		assertEquals(oid, oid2)
	}
}
