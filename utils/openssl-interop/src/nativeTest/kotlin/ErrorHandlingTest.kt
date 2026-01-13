import kotlinx.cinterop.ExperimentalForeignApi
import org.openecard.openssl.EC_GROUP_new_by_curve_name
import org.openecard.utils.openssl.assertNotNull
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFails

class ErrorHandlingTest {
	@OptIn(ExperimentalForeignApi::class)
	@Test
	fun testThrowingOfOpensslError() {
		val err =
			assertFails {
				EC_GROUP_new_by_curve_name(
					-1,
				).assertNotNull()
			}
		assertContains(err.message.toString(), "osslErrors")
	}
}
