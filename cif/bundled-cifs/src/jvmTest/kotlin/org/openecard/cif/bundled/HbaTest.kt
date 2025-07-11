package org.openecard.cif.bundled

import org.junit.jupiter.api.Disabled
import org.openecard.cif.definition.CifVerifier
import kotlin.test.Test
import kotlin.test.assertEquals

class HbaTest {
	@Disabled
	@Test
	fun `test HBA CIF`() {
		val cif = HbaCif
		CifVerifier(cif).verify()

		assertEquals(7, cif.applications.size)

		// TODO: add more assertions
	}
}
