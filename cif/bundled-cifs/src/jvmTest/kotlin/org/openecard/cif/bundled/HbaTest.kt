package org.openecard.cif.bundled

import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals

class HbaTest {
	@Disabled
	@Test
	fun `test eGK CIF`() {
		val cif = HbaCif

		assertEquals(7, cif.applications.size)

		// TODO: add more assertions
	}
}
