package org.openecard.cif.bundled

import kotlinx.serialization.json.Json
import org.openecard.cif.definition.CardInfoDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

class CifSerialization {
	@OptIn(ExperimentalTime::class)
	@Test
	fun `CIF serialization roundtrip with eGK`() {
		val cifRef = EgkCif
		val cifStr = Json.encodeToString(cifRef)
		val cif = Json.decodeFromString<CardInfoDefinition>(cifStr)
		assertEquals(cifRef, cif)
	}
}
