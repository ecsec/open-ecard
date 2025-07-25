package org.openecard.cif.bundled

import kotlinx.serialization.json.Json
import org.openecard.cif.definition.CifVerifier
import kotlin.test.Test
import kotlin.test.assertEquals

class EgkTest {
	@Test
	fun `test eGK CIF`() {
		val cif = EgkCif
		CifVerifier(cif).verify()

		assertEquals(9, cif.applications.size)

		// TODO: add more assertions

		val cifStr = Json.encodeToString(cif)
	}
}
