package org.openecard.common.apdu.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassByteTest {
	@Test
	fun `test parsing`() {
		ClassByte.parse(0x80.toByte()).let {
			assertTrue { it is ProprietaryClassByte }
		}
		ClassByte.parseInterIndustry(0x00.toByte()).let {
			assertEquals(0, it.channelNumber)
			assertEquals(SecureMessagingIndication.NO_SM, it.sm)
			assertEquals(false, it.commandChaining)
			assertEquals(0, it.byte)
		}
	}
}
