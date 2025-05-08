package org.openecard.sc.iface

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ApduTest {
	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	@Test
	fun `test short length apdu write`() {
		val noDataNoLe = CommandApdu(0x00.toUByte(), 0x01.toUByte(), 0x02.toUByte(), 0x03.toUByte(), le = null)
		assertContentEquals("00010203".hexToUByteArray(), noDataNoLe.toBytes)

		val noDataLe0 = CommandApdu(0x00.toUByte(), 0x01.toUByte(), 0x02.toUByte(), 0x03.toUByte(), le = 0u)
		assertContentEquals("0001020300".hexToUByteArray(), noDataLe0.toBytes)

		val noDataLeFF = CommandApdu(0x00.toUByte(), 0x01.toUByte(), 0x02.toUByte(), 0x03.toUByte(), le = 0xFFu)
		assertContentEquals("00010203FF".hexToUByteArray(), noDataLeFF.toBytes)

		val data1NoLe =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				data = ByteArray(1).toUByteArray(),
				le = null,
			)
		assertContentEquals("000102030100".hexToUByteArray(), data1NoLe.toBytes)

		val dataFFNoLe =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				data = ByteArray(0xFF).toUByteArray(),
				le = null,
			)
		assertEquals(5 + 0xFF, dataFFNoLe.toBytes.size)
		assertContentEquals("00010203FF".hexToUByteArray(), dataFFNoLe.toBytes.sliceArray(0 until 5))
		assertContentEquals(ByteArray(0xFF).toUByteArray(), dataFFNoLe.toBytes.sliceArray(5 until 5 + 0xFF))

		val dataFFNoLeForce =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				data = ByteArray(0xFF).toUByteArray(),
				le = null,
				forceExtendedLength = true,
			)
		assertEquals(5 + 0xFF, dataFFNoLeForce.toBytes.size)
		assertContentEquals("00010203FF".hexToUByteArray(), dataFFNoLeForce.toBytes.sliceArray(0 until 5))
		assertContentEquals(ByteArray(0xFF).toUByteArray(), dataFFNoLeForce.toBytes.sliceArray(5 until 5 + 0xFF))

		val data1Le0 =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				data = ByteArray(1).toUByteArray(),
				le = 0u,
			)
		assertEquals(6 + 1, data1Le0.toBytes.size)
		assertContentEquals("0001020301".hexToUByteArray(), data1Le0.toBytes.sliceArray(0 until 5))
		assertContentEquals(ByteArray(1).toUByteArray(), data1Le0.toBytes.sliceArray(5 until 5 + 1))
		assertContentEquals("00".hexToUByteArray(), data1Le0.toBytes.sliceArray(5 + 1 until 6 + 1))
	}

	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	@Test
	fun `test extended length apdu write`() {
		val noDataLe0 =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				le = 0u,
				forceExtendedLength = true,
			)
		assertContentEquals("00010203000000".hexToUByteArray(), noDataLe0.toBytes)

		val noDataLeFF =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				le = 0xFFu,
				forceExtendedLength = true,
			)
		assertContentEquals("000102030000FF".hexToUByteArray(), noDataLeFF.toBytes)

		val noDataLe0100 =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				le = 0x0100u,
			)
		assertContentEquals("00010203000100".hexToUByteArray(), noDataLe0100.toBytes)

		val noDataLeFFFF =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				le = 0xFFFFu,
			)
		assertContentEquals("0001020300FFFF".hexToUByteArray(), noDataLeFFFF.toBytes)

		val data100NoLe =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				data = ByteArray(0x100).toUByteArray(),
				le = null,
			)
		assertEquals(7 + 0x100, data100NoLe.toBytes.size)
		assertContentEquals("00010203000100".hexToUByteArray(), data100NoLe.toBytes.sliceArray(0 until 7))
		assertContentEquals(ByteArray(0x100).toUByteArray(), data100NoLe.toBytes.sliceArray(7 until 7 + 0x100))

		val dataFFLe0 =
			CommandApdu(
				0x00.toUByte(),
				0x01.toUByte(),
				0x02.toUByte(),
				0x03.toUByte(),
				data = ByteArray(0xFF).toUByteArray(),
				le = 0u,
				forceExtendedLength = true,
			)
		assertEquals(9 + 0xFF, dataFFLe0.toBytes.size)
		assertContentEquals("000102030000FF".hexToUByteArray(), dataFFLe0.toBytes.sliceArray(0 until 7))
		assertContentEquals(ByteArray(0xFF).toUByteArray(), dataFFLe0.toBytes.sliceArray(7 until 7 + 0xFF))
		assertContentEquals("0000".hexToUByteArray(), dataFFLe0.toBytes.sliceArray(7 + 0xFF until 9 + 0xFF))
	}
}
