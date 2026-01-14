import TestVars.EgkG21.standardizedParameters
import kotlinx.cinterop.ExperimentalForeignApi
import org.openecard.sc.pace.crypto.OSslEcCurve
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ErrorHandlingTest {
	@BeforeTest()
	fun log() = configureLog()

	@OptIn(ExperimentalForeignApi::class)
	@Test
	fun testThrowingOfOpensslError() {
		OSslEcCurve(standardizedParameters).use { curve ->
			val err =
				assertFails {
					(curve.g * ubyteArrayOf(0x00.toUByte())).toOpensslECPoint()
				}
			assertContains(err.message.toString(), "Openssl error")
		}
	}
}
