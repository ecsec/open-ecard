package org.openecard.cif.bundled

import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals

class EgkTest {
	@Disabled
	@Test
	fun `test eGK CIF`() {
		val cif = EgkCif

		assertEquals(4, cif.applications.size)

		// TODO: add more assertions
	}
}
