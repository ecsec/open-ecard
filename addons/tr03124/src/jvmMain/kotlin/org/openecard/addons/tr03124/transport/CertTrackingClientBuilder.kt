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
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
import org.bouncycastle.tls.BasicTlsPSKIdentity
import org.openecard.addons.tr03124.Tr03124Config
import org.openecard.addons.tr03124.transport.EidServerPaos.Companion.registerPaosNegotiation
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.utils.common.cast
import org.openecard.utils.common.doIf
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509ExtendedTrustManager

private val log = KotlinLogging.logger { }

class CertTrackingClientBuilder(
	certTracker: EserviceCertTracker,
) : KtorClientBuilder {
	private val tm = SslSettings.getTrustAllCertsManager(certTracker)
	private val sslCtx = SslSettings.getSslContext(tm)
	private val sslSessions = mutableSetOf<SSLSession>()

	@OptIn(ExperimentalUnsignedTypes::class)
	private val httpClientBase =
		OkHttpClient
			.Builder()
			.apply {
				// define TLS cipher suites and allowed protocols, overridden in PSK client
				connectionSpecs(
					listOf(
						ConnectionSpec
							.Builder(
								ConnectionSpec.RESTRICTED_TLS,
							).cipherSuites(
								*buildList {
									// TLSv1.3
									doIf(Tr03124Config.nonBsiApprovedCiphers) {
										add(okhttp3.CipherSuite.TLS_CHACHA20_POLY1305_SHA256)
									}
									add(okhttp3.CipherSuite.TLS_AES_256_GCM_SHA384)
									add(okhttp3.CipherSuite.TLS_AES_128_GCM_SHA256)
									// TLSv1.2
									doIf(Tr03124Config.nonBsiApprovedCiphers) {
										add(okhttp3.CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256)
										add(okhttp3.CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256)
									}
									add(okhttp3.CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384)
									add(okhttp3.CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384)
									add(okhttp3.CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256)
									add(okhttp3.CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256)
									// TLSv1.2 weak
									add(okhttp3.CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384)
									add(okhttp3.CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256)
								}.toTypedArray(),
							).build(),
					),
				)
				sslSocketFactory(sslCtx.socketFactory, tm)
				addNetworkInterceptor { chain ->
					chain.connection()!!.let { con ->
						when (val sock = con.socket()) {
							is SSLSocket -> {
								sock.session.peerCertificates.firstOrNull()?.let { cert ->
									val cert =
										cert.cast<X509Certificate>()
											?: throw UntrustedCertificateError("Received certificate is not an X509 certificate")
									// record TLS cert
									val hash = cert.contentSha256()
									log.debug { "Recording certificate <${cert.subjectX500Principal}>" }
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
				}
				followRedirects(false)
				// don't allow protocol switching
				followSslRedirects(false)
			}.build()

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

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun buildEidServerClient(token: TcToken.TcTokenOk): HttpClient =
		when (token) {
			is TcToken.TcTokenAttached -> {
				log.info { "Building attached eID-Server PAOS client" }
				buildAttachedClient()
			}
			is TcToken.TcTokenPsk -> {
				log.info { "Building PSK PAOS client" }
				buildPskClient(token.sessionIdentifier, token.psk.v.toByteArray())
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
		psk: ByteArray,
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
										ConnectionSpec.RESTRICTED_TLS,
									).apply {
										val with13 = Tr03124Config.nonBsiApprovedCiphers
										if (!with13) {
											tlsVersions(TlsVersion.TLS_1_2)
										}
										cipherSuites(
											*buildList {
												// TLS 1.3
												doIf(with13) {
													add("TLS_AES_128_GCM_SHA256")
													add("TLS_AES_256_GCM_SHA384")
													add("TLS_CHACHA20_POLY1305_SHA256")
												}
												// Modern ECDHE ciphers -- disabled because OkHttp can't handle empty cert chains
												// doIf(Tr03124Config.nonBsiApprovedCiphers) {
												// 	add("TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256")
												// 	add("TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256")
												// 	add("TLS_ECDHE_PSK_WITH_AES_256_GCM_SHA384")
												// }
												// BSI approved TLS1.2
												add("TLS_RSA_PSK_WITH_AES_256_CBC_SHA")
												add("TLS_RSA_PSK_WITH_AES_256_CBC_SHA384")
												add("TLS_RSA_PSK_WITH_AES_128_CBC_SHA256")
												add("TLS_RSA_PSK_WITH_AES_256_GCM_SHA384")
												add("TLS_RSA_PSK_WITH_AES_128_GCM_SHA256")
											}.toTypedArray(),
										)
									}.build(),
							),
						).sslSocketFactory(SslSettings.getPskSocketFactory(tm, session, psk), tm)
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
	init {
		System.setProperty("jsse.enableFFDHE", "false")
		val modernGroups = doIf(Tr03124Config.nonBsiApprovedCiphers) { ",x25519,x448" } ?: ""
		System.setProperty("jdk.tls.namedGroups", "secp256r1,secp384r1,secp521r1$modernGroups")
		val modernSigAlgs = doIf(Tr03124Config.nonBsiApprovedCiphers) { ",ed25519,ed448" } ?: ""
		System.setProperty(
			"jdk.tls.client.SignatureSchemes",
			"ecdsa_secp256r1_sha256,ecdsa_secp384r1_sha384,ecdsa_secp521r1_sha512," +
				"rsa_pkcs1_sha256,rsa_pkcs1_sha384,rsa_pkcs1_sha512," +
				"rsa_pss_pss_sha256,rsa_pss_pss_sha384,rsa_pss_pss_sha512," +
				"rsa_pss_rsae_sha256,rsa_pss_rsae_sha384,rsa_pss_rsae_sha512" +
				modernSigAlgs,
		)
		Security.addProvider(BouncyCastleProvider())
		Security.addProvider(BouncyCastleJsseProvider())
	}

	internal fun getSslContext(tm: TrustManager): SSLContext {
		val sslContext = SSLContext.getInstance("TLS", "BCJSSE")
		val tms = listOf(tm)
		sslContext.init(null, tms.toTypedArray(), null)
		return sslContext
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	internal fun getPskSocketFactory(
		tm: X509ExtendedTrustManager,
		session: String,
		psk: ByteArray,
	): SSLSocketFactory =
		BcPskSSLSocketFactory(
			tm = tm,
			BcPskTlsParams(),
// 				supportedProtocolVersions = arrayOf(ProtocolVersion.TLSv12),
// 				supportedCipherSuiteCodes =
// 					intArrayOf(
// 						CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA,
// 						CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384,
// 						CipherSuite.TLS_RSA_PSK_WITH_AES_128_CBC_SHA256,
// 						CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384,
// 						CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256,
// 					),
// 			),
			BasicTlsPSKIdentity(session, psk),
		)

	@OptIn(ExperimentalUnsignedTypes::class)
	internal fun getTrustAllCertsManager(certTracker: EserviceCertTracker): X509ExtendedTrustManager =
		Tr03124TrustManager(certTracker)
}
