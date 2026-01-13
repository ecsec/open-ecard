
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.toKString
import org.openecard.openssl.OPENSSL_VERSION
import org.openecard.openssl.OpenSSL_version
import org.openecard.openssl.SSL_CTX
import org.openecard.openssl.SSL_CTX_new
import org.openecard.openssl.SSLv23_method
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull

@OptIn(ExperimentalForeignApi::class)
class OpensslTest {
	@Test
	fun testVersion() {
		val v = assertNotNull(OpenSSL_version(OPENSSL_VERSION))
		assertContains(v.toKString(), "OpenSSL")
	}

	@Test
	fun testInit() {
		val ctx: CPointer<SSL_CTX>? = SSL_CTX_new(SSLv23_method?.invoke())
		assertNotNull(ctx).also { println("ctx: $ctx") }
	}
}
