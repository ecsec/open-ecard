package org.openecard.addons.tr03124.transport

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addons.tr03124.Tr03124Config
import org.openecard.utils.common.throwIf
import java.net.Socket
import java.security.PublicKey
import java.security.cert.PKIXBuilderParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.EdECPublicKey
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.CertPathTrustManagerParameters
import javax.net.ssl.SSLEngine
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509ExtendedTrustManager

private val log = KotlinLogging.logger { }

@Suppress("CustomX509TrustManager")
class Tr03124TrustManager(
	private val certTracker: EserviceCertTracker,
) : X509ExtendedTrustManager() {
	override fun checkClientTrusted(
		chain: Array<out X509Certificate?>?,
		authType: String?,
		socket: Socket?,
	): Unit = throw UnsupportedOperationException("Client certificates are not permitted in TR-03124")

	override fun checkClientTrusted(
		chain: Array<out X509Certificate?>?,
		authType: String?,
		engine: SSLEngine?,
	): Unit = throw UnsupportedOperationException("Client certificates are not permitted in TR-03124")

	override fun checkClientTrusted(
		chain: Array<out X509Certificate?>?,
		authType: String?,
	): Unit = throw UnsupportedOperationException("Client certificates are not permitted in TR-03124")

	private fun getTrustManager(chain: Array<out X509Certificate>): X509ExtendedTrustManager {
		val ee = chain[0]
		val tmf = TrustManagerFactory.getInstance("PKIX", "BCJSSE")
		val params =
			CertPathTrustManagerParameters(
				PKIXBuilderParameters(
					mutableSetOf(TrustAnchor(ee, null)),
					X509CertSelector().apply {
						certificate = ee
					},
				),
			)
		tmf.init(params)
		return tmf.trustManagers.filterIsInstance<X509ExtendedTrustManager>().first()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun checkEeCert(chain: Array<out X509Certificate>) {
		val ee = chain[0]
		log.debug { "Checking certificate: \n$ee" }

		// check key sizes
		checkKeySize(ee.publicKey)

		// check date
		ee.checkValidity()
	}

	override fun checkServerTrusted(
		chain: Array<out X509Certificate>,
		authType: String,
		socket: Socket,
	) {
		val tm = getTrustManager(chain)
		tm.checkServerTrusted(chain, authType, socket)
		checkEeCert(chain)
	}

	override fun checkServerTrusted(
		chain: Array<out X509Certificate>,
		authType: String,
		engine: SSLEngine,
	) {
		val tm = getTrustManager(chain)
		tm.checkServerTrusted(chain, authType, engine)
		checkEeCert(chain)
	}

	override fun checkServerTrusted(
		chain: Array<out X509Certificate>,
		authType: String,
	): Unit = throw UnsupportedOperationException("Connection information is required for trust validation")

	// TODO: according to okhttp, this is needed for android, need to revise once we test on android

// 	@Suppress("unused")
// 	@Throws(CertificateException::class)
// 	fun checkServerTrusted(
// 		chain: Array<out X509Certificate>,
// 		authType: String,
// 		host: String?,
// 	): MutableList<X509Certificate> =
// 		throw UnsupportedOperationException("Connection information is required for trust validation")

	override fun getAcceptedIssuers(): Array<out X509Certificate> = arrayOf()

	@Throws(InvalidTlsParameter::class)
	fun checkKeySize(pk: PublicKey) {
		if (!Tr03124Config.disableKeySizeCheck) {
			when (pk) {
				is RSAPublicKey -> {
					throwIf(pk.modulus.bitLength() <= 3000) { InvalidTlsParameter("RSA key of the server certificate is too small") }
				}

				is ECPublicKey -> {
					throwIf(
						pk.w.affineX.bitLength() <= 250,
					) { InvalidTlsParameter("ECDSA key of the server certificate is too small") }
				}

				is EdECPublicKey -> {
					throwIf(pk.point.y.bitLength() <= 250) { InvalidTlsParameter("EdDSA key of the server certificate is too small") }
				}

				else -> {
					throw InvalidTlsParameter("Unsupported key type used in certificate")
				}
			}
		}
	}
}
