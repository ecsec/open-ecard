package org.openecard.addons.tr03124.transport

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.Logging
import kotlinx.io.IOException
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.TlsVersion
import okio.ByteString.Companion.toByteString
import org.bchateau.pskfactories.BcPskSSLSocketFactory
import org.bchateau.pskfactories.BcPskTlsParams
import org.bouncycastle.tls.BasicTlsPSKIdentity
import org.bouncycastle.tls.CipherSuite
import org.bouncycastle.tls.ProtocolVersion
import org.openecard.addons.tr03124.Tr03124Config
import org.openecard.addons.tr03124.transport.EidServerPaos.Companion.registerPaosNegotiation
import org.openecard.addons.tr03124.xml.TcToken
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private val log = KotlinLogging.logger { }

class CertTrackingClientBuilder(
	certTracker: EserviceCertTracker,
) : KtorClientBuilder {
	private val tm = SslSettings.getTrustAllCertsManager()
	private val sslCtx = SslSettings.getSslContext(tm)
	private val sslSessions = mutableSetOf<SSLSession>()

	@OptIn(ExperimentalUnsignedTypes::class)
	private val httpClientBase =
		OkHttpClient
			.Builder()
			.sslSocketFactory(sslCtx.socketFactory, tm)
			.addNetworkInterceptor { chain ->
				// record TLS cert
				chain.connection()!!.let { con ->
					when (val sock = con.socket()) {
						is SSLSocket -> {
							sock.session.peerCertificates.firstOrNull()?.let { cert ->
								val hash = cert.contentSha256()
								if (cert is X509Certificate) {
									log.debug { "Recording certificate <${cert.subjectX500Principal}>" }
								}
								certTracker.addCertHash(hash.toUByteArray())
							}
						}
						else -> {
							throw IllegalStateException("Non TLS socket used in eID Process")
						}
					}
				}

				val req = chain.request()
				val resp = chain.proceed(req)
				resp
			}.followRedirects(false)
			.build()

	@OptIn(ExperimentalUnsignedTypes::class)
	override val tokenClient: HttpClient by lazy {
		HttpClient(OkHttp) {
			engine {
				preconfigured =
					httpClientBase
						.newBuilder()
						.addNetworkInterceptor { chain ->
							// record session for use in attached eID-Server case
							// connection is required in network interceptor
							chain.connection()!!.let { con ->
								when (val sock = con.socket()) {
									is SSLSocket -> {
										val sess = sock.session
										if (!sslSessions.any { it.id.contentEquals(sess.id) }) {
											sslSessions.add(sess)
										}
									}

									else -> {
										throw IllegalStateException("Non TLS socket used in eID Process")
									}
								}
							}

							val req = chain.request()
							val resp = chain.proceed(req)
							resp
						}.build()
			}

			Tr03124Config.httpLog?.let {
				install(Logging, it)
			}

			followRedirects = true
		}
	}

	override val redirectClient: HttpClient by lazy {
		HttpClient(OkHttp) {
			engine {
				preconfigured =
					httpClientBase
						.newBuilder()
						.build()
			}

			Tr03124Config.httpLog?.let {
				install(Logging, it)
			}

			followRedirects = false
		}
	}

	class ClientAbort : Exception()

	override val checkCertClient: CertValidationClient by lazy {
		val okHttpClient =
			httpClientBase
				.newBuilder()
				.addNetworkInterceptor { chain ->
					// add network interceptor stopping before the HTTP request is made
					throw ClientAbort()
				}.build()

		object : CertValidationClient {
			@Throws(IOException::class)
			override suspend fun checkCert(url: String) {
				try {
					val req = Request.Builder().url(url).build()
					okHttpClient.newCall(req).execute()
					throw IllegalStateException("HTTP request has been executed, but was not intended to be")
				} catch (ex: ClientAbort) {
					return
				}
			}
		}
	}

	override fun buildEidServerClient(token: TcToken): HttpClient {
		val params = token.securityParameters
		return when (token.securityProtocol) {
			null if params == null -> {
				log.info { "Building attached eID-Server PAOS client" }
				buildAttachedClient()
			}
			TcToken.SecurityProtocolType.TLS_PSK if params != null -> {
				log.info { "Building PSK PAOS client" }
				buildPskClient(token.sessionIdentifier, params)
			}
			else -> throw IllegalArgumentException("TCToken contains invalid combination of eID-Server coordinates")
		}
	}

	private fun buildAttachedClient(): HttpClient =
		HttpClient(OkHttp) {
			engine {
				preconfigured =
					httpClientBase
						.newBuilder()
						.addNetworkInterceptor { chain ->
							// check that session is not new
							// connection is required in network interceptor
							chain.connection()!!.let { con ->
								when (val sock = con.socket()) {
									is SSLSocket -> {
										val sess = sock.session
										if (!sslSessions.any { it.id.contentEquals(sess.id) }) {
											throw UntrustedCertificateError("Attached eID Server created a new SSL session, which is not permitted")
										}
									}
									else -> {
										throw IllegalStateException("Non TLS socket used in eID Process")
									}
								}
							}

							val req = chain.request()
							val resp = chain.proceed(req)
							resp
						}.build()
			}

			Tr03124Config.paosLog?.let {
				install(Logging, it)
			}

			registerPaosNegotiation()
			followRedirects = false
		}

	private fun buildPskClient(
		session: String,
		psk: TcToken.PskParams,
	): HttpClient =
		HttpClient(OkHttp) {
			engine {
				preconfigured =
					httpClientBase
						.newBuilder()
						.connectionSpecs(
							listOf(
								ConnectionSpec
									.Builder(
										ConnectionSpec.MODERN_TLS,
									).tlsVersions(TlsVersion.TLS_1_2)
									.cipherSuites(
										"TLS_RSA_PSK_WITH_AES_256_CBC_SHA",
										"TLS_RSA_PSK_WITH_AES_256_CBC_SHA384",
										"TLS_RSA_PSK_WITH_AES_128_CBC_SHA256",
										"TLS_RSA_PSK_WITH_AES_256_GCM_SHA384",
										"TLS_RSA_PSK_WITH_AES_128_GCM_SHA256",
									).build(),
							),
						).sslSocketFactory(SslSettings.getPskSocketFactory(session, psk), tm)
						.build()
			}

			Tr03124Config.paosLog?.let {
				install(Logging, it)
			}

			registerPaosNegotiation()
			followRedirects = false
		}
}

fun Certificate.contentSha256(): ByteArray = encoded.toByteString().sha256().toByteArray()

object SslSettings {
	fun getSslContext(tm: TrustManager): SSLContext {
		val sslContext = SSLContext.getInstance("TLS")
		val tms = listOf(tm)
		sslContext.init(null, tms.toTypedArray(), null)
		return sslContext
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun getPskSocketFactory(
		session: String,
		psk: TcToken.PskParams,
	): SSLSocketFactory =
		BcPskSSLSocketFactory(
			BcPskTlsParams(
				supportedProtocolVersions = arrayOf(ProtocolVersion.TLSv12),
				supportedCipherSuiteCodes =
					intArrayOf(
						CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA,
						CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384,
						CipherSuite.TLS_RSA_PSK_WITH_AES_128_CBC_SHA256,
						CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384,
						CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256,
					),
			),
			BasicTlsPSKIdentity(session, psk.psk.v.toByteArray()),
		)

	@OptIn(ExperimentalUnsignedTypes::class)
	fun getTrustAllCertsManager(): X509TrustManager =
		@Suppress("CustomX509TrustManager")
		object : X509TrustManager {
			override fun checkClientTrusted(
				chain: Array<out X509Certificate>,
				authType: String?,
			): Unit = throw UnsupportedOperationException("Client certificates are not permitted in TR-03124")

			@Suppress("TrustAllX509TrustManager")
			override fun checkServerTrusted(
				chain: Array<out X509Certificate>,
				authType: String,
			) {
			}

			override fun getAcceptedIssuers(): Array<out X509Certificate> = arrayOf()
		}
}
