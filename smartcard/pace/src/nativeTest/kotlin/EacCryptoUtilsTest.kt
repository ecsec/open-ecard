import TestVars.EgkG21.standardizedParameters
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.cinterop.ExperimentalForeignApi
import org.openecard.sc.pace.crypto.OSslEcCurve
import org.openecard.sc.pace.crypto.eacCryptoUtils
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class EacCryptoUtilsTest {
	@BeforeTest()
	fun log() = configureLog()

	@OptIn(ExperimentalForeignApi::class)
	@Test
	fun cryptoUtils() {
		OSslEcCurve(standardizedParameters).use { curve ->
			val keyData = curve.generateKeyPair(CryptographyRandom.Default).publicKey.encoded

			assertTrue {
				eacCryptoUtils()
					.compressKey(
						keyData,
						standardizedParameters,
					).isNotEmpty()
			}
		}
	}
}
