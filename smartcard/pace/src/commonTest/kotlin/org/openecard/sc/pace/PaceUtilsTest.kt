package org.openecard.sc.pace

import org.junit.jupiter.api.assertThrows
import org.openecard.sc.iface.SequenceCounterOverflow
import kotlin.test.Test
import kotlin.test.assertContentEquals

class PaceUtilsTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test SSC 16byte`() {
		val len = 16
		assertContentEquals(UByteArray(15) + ubyteArrayOf(0u), 0L.toSequenceCounter(targetLength = len))
		assertContentEquals(UByteArray(15) + ubyteArrayOf(1u), 1L.toSequenceCounter(targetLength = len))
		assertContentEquals(
			UByteArray(8) + 127u + UByteArray(7) { 0xFFu },
			Long.MAX_VALUE.toSequenceCounter(targetLength = len),
		)
		assertThrows<SequenceCounterOverflow> { (Long.MAX_VALUE + 1).toSequenceCounter(targetLength = len) }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test SSC 8byte`() {
		val len = 8
		assertContentEquals(UByteArray(7) + ubyteArrayOf(0u), 0L.toSequenceCounter(targetLength = len))
		assertContentEquals(UByteArray(7) + ubyteArrayOf(1u), 1L.toSequenceCounter(targetLength = len))
		assertContentEquals(
			ubyteArrayOf(127u) + UByteArray(7) { 0xFFu },
			Long.MAX_VALUE.toSequenceCounter(targetLength = len),
		)
		assertThrows<SequenceCounterOverflow> { (Long.MAX_VALUE + 1).toSequenceCounter(targetLength = len) }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test SSC 4byte`() {
		val len = 4
		assertContentEquals(UByteArray(3) + ubyteArrayOf(0u), 0L.toSequenceCounter(targetLength = len))
		assertContentEquals(UByteArray(3) + ubyteArrayOf(1u), 1L.toSequenceCounter(targetLength = len))
		assertContentEquals(
			UByteArray(4) { 0xFFu },
			0xFFFFFFFF.toSequenceCounter(targetLength = len),
		)
		assertThrows<SequenceCounterOverflow> { 0x01FFFFFFFF.toSequenceCounter(targetLength = len) }
		assertThrows<SequenceCounterOverflow> { (Long.MAX_VALUE + 1).toSequenceCounter(targetLength = len) }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test SSC 1byte`() {
		val len = 1
		assertContentEquals(ubyteArrayOf(0u), 0L.toSequenceCounter(targetLength = len))
		assertContentEquals(ubyteArrayOf(1u), 1L.toSequenceCounter(targetLength = len))
		assertContentEquals(
			ubyteArrayOf(0xFFu),
			0xFFL.toSequenceCounter(targetLength = len),
		)
		assertThrows<SequenceCounterOverflow> { 0x01FFL.toSequenceCounter(targetLength = len) }
		assertThrows<SequenceCounterOverflow> { (Long.MAX_VALUE + 1).toSequenceCounter(targetLength = len) }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test SSC invalid byte`() {
		assertThrows<IllegalArgumentException> { 1L.toSequenceCounter(targetLength = 0) }
		assertThrows<IllegalArgumentException> { 1L.toSequenceCounter(targetLength = -1) }
	}
}
