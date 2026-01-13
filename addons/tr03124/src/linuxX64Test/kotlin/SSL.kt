import kotlinx.cinterop.ExperimentalForeignApi
import org.openecard.utils.openssl.TlsHandler
import kotlin.test.Test
import kotlin.test.assertNotNull

class SSL {
	@OptIn(ExperimentalForeignApi::class)
	@Test
	fun t() {
		TlsHandler().use {
			assertNotNull(it.ctx).also {
				println("ctx: $it")
			}
		}
	}
}
