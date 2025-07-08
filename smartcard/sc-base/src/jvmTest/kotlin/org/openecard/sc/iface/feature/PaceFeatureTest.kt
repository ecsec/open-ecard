package org.openecard.sc.iface.feature

import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaceFeatureTest {
	@Test
	fun `test status codes mapping`() {
		assertNull(0x0u.toPaceError())
		assertEquals(PaceResultCode.INCONSISTENT_LENGTH, 0xD0000001u.toPaceError()?.error)
		assertNotNull(0xF0066888u.toPaceError()).let {
			assertEquals(PaceResultCode.GA4_ERROR, it.error)
			assertEquals(0x6888u, it.swCode?.sw)
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test capabilities parsing`() {
		val response = hex("00000000010070")
		val capabilities = GetReaderCapabilitiesResponse.fromPaceResponse(response)
		assertTrue {
			setOf(PaceCapability.QES, PaceCapability.GENERIC_PACE, PaceCapability.GERMAN_EID) ==
				capabilities.capabilities
		}
	}
}
