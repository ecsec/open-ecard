package org.openecard.cif.bundled

import kotlin.test.Test
import kotlin.test.assertNotNull

class CompleteTreeTest {
	@Test
	fun `load bundled cifs`() {
		assertNotNull(CompleteTree.calls)
	}
}
