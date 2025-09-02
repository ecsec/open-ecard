package org.openecard.addons.tr03124.transport

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import okhttp3.OkHttpClient
import okio.ByteString.Companion.toByteString
import org.bchateau.pskfactories.BcPskSSLSocketFactory
import org.bchateau.pskfactories.BcPskTlsParams
import org.bouncycastle.tls.BasicTlsPSKIdentity
import org.bouncycastle.tls.TlsPSKIdentity
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.addons.tr03124.xml.TcToken.Companion.toTcToken
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
	private val tm = SslSettings.getTrustManager(certTracker)
	private val sslCtx = SslSettings.getSslContext(tm)
	private val sslSessions = mutableSetOf<SSLSession>()

	@OptIn(ExperimentalUnsignedTypes::class)
	private val httpClientBase =
		OkHttpClient
			.Builder()
			.sslSocketFactory(sslCtx.socketFactory, tm)
			.followRedirects(false)
			.build()

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
			followRedirects = false
		}
	}

	override fun buildEidServerClient(token: TcToken): HttpClient {
		val params = token.securityParameters
		return if (token.securityProtocol == null && params == null) {
			buildAttachedClient()
		} else if (token.securityProtocol == TcToken.SecurityProtocolType.TLS_PSK && params != null) {
			buildPskClient(token.sessionIdentifier, params)
		} else {
			throw IllegalArgumentException("TCToken contains invalid combination of eID-Server coordinates")
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
						.sslSocketFactory(SslSettings.getPskSocketFactory(session, psk), tm)
						.build()
			}
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
			BcPskTlsParams(),
			BasicTlsPSKIdentity(session, psk.psk.v.toByteArray()),
		)

	@OptIn(ExperimentalUnsignedTypes::class)
	fun getTrustManager(certTracker: EserviceCertTracker): X509TrustManager =
		@Suppress("CustomX509TrustManager")
		object : X509TrustManager {
			override fun checkClientTrusted(
				chain: Array<out X509Certificate>,
				authType: String?,
			): Unit = throw UnsupportedOperationException("Client certificates are not permitted in TR-03124")

			override fun checkServerTrusted(
				chain: Array<out X509Certificate>,
				authType: String,
			) {
				chain.firstOrNull()?.let {
					val hash = it.contentSha256()
					log.debug { "Recording certificate <${it.subjectX500Principal}>" }
					certTracker.addCertHash(hash.toUByteArray())
				}
			}

			override fun getAcceptedIssuers(): Array<out X509Certificate> = arrayOf()
		}
}
