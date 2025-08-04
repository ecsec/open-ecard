import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosNfcTest {
	@Test
	fun getFactory() {
		assertEquals("IosNFC", IosTerminalFactory().name)
	}
}
