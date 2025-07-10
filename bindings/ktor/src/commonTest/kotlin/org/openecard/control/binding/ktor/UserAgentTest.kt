package org.openecard.control.binding.ktor

import org.openecard.control.binding.ktor.Versioned.Companion.toTr03124Version
import kotlin.test.Test
import kotlin.test.assertEquals

class UserAgentTest {
	@Test
	fun `generic UserAgent generation`() {
		assertEquals(
			"App/1.0 ()",
			UserAgent("App", "1.0").toHeaderValue(),
		)
		assertEquals(
			"App/1.0 (fooß)",
			UserAgent("App", "1.0", "fooß").toHeaderValue(),
		)
	}

	@Test
	fun `TR03124 UserAgent generation`() {
		assertEquals(
			"App/1.0 (TR-03124-1/1.3)",
			UserAgent.tr01324UserAgent("App", "1.0", listOf("1.3".toTr03124Version())).toHeaderValue(),
		)
	}
}
