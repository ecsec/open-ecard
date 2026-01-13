import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.openecard.utils.openssl.TlsHandler
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class)
class TlsHandlerTest {
	fun testHandler(
		host: String,
		port: Int,
		path: String,
	): String =
		runBlocking {
			withTimeout(5.seconds) {
				TlsHandler().use {
					assertNotNull(it.ctx).also { println("ctx: $it") }
					it.connect(host, port, path)
				}
			}
		}

	val host = "macairm1.fritz.box"

	@Test
	fun test_nc() {
		val res = testHandler(host, 12345, "/index.html")
		assertContains(res, "Hello")
	}

	@Test
	fun test_norm() {
		val res = testHandler(host, 44330, "/index.html")
		assertContains(res, "Hello")
	}

	@Test
	fun test_psk() {
		val res = testHandler(host, 44331, "/index.html")
		assertContains(res, "Hello")
	}

	@Test
	fun test_ec() {
		val res = testHandler("ecsec.de", 443, "/")
		assertContains(res, "Hello")
	}
}
