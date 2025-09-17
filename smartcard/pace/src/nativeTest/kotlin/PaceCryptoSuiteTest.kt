import TestVars.EgkG21.efCaEgk
import TestVars.EgkG21.encNonce
import TestVars.EgkG21.mapNonce
import TestVars.EgkG21.standardizedParameters
import TestVars.EgkG21.step2Nonce
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.cinterop.ExperimentalForeignApi
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.toEfCardAccess
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse
import org.openecard.sc.pace.crypto.OSslEcCurve
import org.openecard.sc.pace.crypto.OSslPaceCryptoSuite
import org.openecard.sc.pace.crypto.cryptoSuite
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaceCryptoSuiteTest {
	@BeforeTest()
	fun log() = configureLog()

	@OptIn(ExperimentalForeignApi::class)
	@Test
	fun testCurveClean() {
		OSslEcCurve(standardizedParameters).close()
	}

	@Test
	fun testOpenSslCurveBrainPoolCurveImp() =
		OSslEcCurve(standardizedParameters).use { curve ->
			assertEquals(
				"8bd2aeb9cb7e57cb2c4b482ffc81b7afb9de27e1e3bd23c23a4453bd9ace3262",
				curve.g.x.toHexString(),
				"Generator point not of brainpoolcurve",
			)
		}

	@OptIn(ExperimentalForeignApi::class)
	@Test
	fun testSuite() =
		OSslEcCurve(standardizedParameters).use { curve ->
			val suite =
				cryptoSuite(
					paceInfos = efCaEgk.toEfCardAccess().paceInfo.first(),
					"123123",
				)

			val step1 = suite.start()
			val step2 = step1.decryptNonce(encNonce) as OSslPaceCryptoSuite.Step2
			assertContentEquals(step2Nonce, step2.nonce)
			val step3 = step2.mapPublicKeyIcc(mapNonce)
			assertTrue {
				step3.getEncodedPublicKeyPcd().isNotEmpty()
			}
			val step4 =
				step3.decodePublicKeyIcc(
					GeneralAuthenticateResponse.KeyAgreement(
						curve.generateKeyPair(CryptographyRandom.Default).publicKey.encoded,
					),
				)
			assertNotNull(
				step4.getAuthenticationTokenPcd(),
			)
			Unit
		}
}
