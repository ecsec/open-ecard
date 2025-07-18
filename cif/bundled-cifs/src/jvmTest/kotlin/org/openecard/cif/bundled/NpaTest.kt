package org.openecard.cif.bundled

import org.openecard.cif.definition.CifVerifier
import kotlin.test.Test
import kotlin.test.assertEquals

class NpaTest {
	@Test
	fun `test NPA CIF`() {
		val cif = NpaCif
		CifVerifier(cif).verify()

		assertEquals(3, cif.applications.size)

		// TODO: add more assertions
	}
}
