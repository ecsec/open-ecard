package org.openecard.addons.tr03124.transport

import SwiftNio.OpenSslTlsChannelHandler
import SwiftNio.SwiftNioHttpPskClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.callContext
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import org.openecard.utils.common.doIf
import org.openecard.utils.openssl.OpenSslTlsHandler
import org.openecard.utils.openssl.TlsConfig
import platform.Foundation.NSData
import platform.Foundation.NSError
import toByteArray
import toNSData

private val logger = KotlinLogging.logger { }

class ClientAbort : CancellationException()

class NioEngineError(
	e: NSError?,
) : Exception(
		message = e?.localizedDescription ?: "Error in nio based engine",
	)

class SwiftNioEngineConfig : HttpClientEngineConfig() {
	var certTracker: EserviceCertTracker? = null
	var performHttp = true
	var tlsConfig: TlsConfig = TlsConfig()
}

class SwiftNioEngineFactory : HttpClientEngineFactory<SwiftNioEngineConfig> {
	override fun create(block: SwiftNioEngineConfig.() -> Unit): HttpClientEngine =
		SwiftNioEngine(
			SwiftNioEngineConfig().apply {
				block()
			},
		)
}

@OptIn(ExperimentalForeignApi::class)
class SwiftNioEngine(
	override val config: SwiftNioEngineConfig,
) : HttpClientEngineBase("SwiftNioEngine") {
	val sslCtx = OpenSslTlsHandler.sslCtx(config.tlsConfig)

	private val closed = atomic(false)

	override fun close() {
		super.close()
		// if you think wth, look in the base class - it seems to be expected to get called more than once here
		if (!closed.compareAndSet(false, true)) return
		OpenSslTlsHandler.freeCtx(sslCtx)
	}

	// create and wire swiftnio tls handler
	internal fun OpenSslTlsHandler.wireNio() =
		OpenSslTlsChannelHandler.createWithHandshake(
			handshake = {
				handshake()
			},
			inboundEncrypted = { d: NSData? ->
				inboundEncrypted((d?.toByteArray() ?: emptyArray<Byte>()) as ByteArray)
			},
			readPlaintext = {
				readPlaintext()?.toNSData()
			},
			outboundPlain = { d: NSData? ->
				outboundPlain((d?.toByteArray() ?: emptyArray<Byte>()) as ByteArray)
			},
			readEncryptedOutput = {
				readEncryptedOutput()?.toNSData()
			},
			closeNotify = {
				closeNotify()
			},
		)

	@InternalAPI
	override suspend fun execute(data: HttpRequestData): HttpResponseData {
		logger.debug { "Executing request $data" }
		val callCtx = callContext()

		val responseDeffered = CompletableDeferred<HttpResponseData>()

		var method: String? = null
		var path: String? = null
		var headers: Map<Any?, *>? = null
		var body: String? = null

		if (config.performHttp) {
			method = data.method.value
			path = data.url.encodedPathAndQuery
			headers = data.headers.stringMap()
			body = data.body.stringBody()
		}

		return OpenSslTlsHandler(
			sslCtx,
			host = data.url.host,
			config.tlsConfig.psk,
			onPeerCert = { certBytes, closeNotify ->
				certBytes?.let {
					logger.debug { "Adding cert hash" }
					config.certTracker?.addCertHash(it.sha256().toUByteArray())
				}
				doIf(!config.performHttp) {
					logger.debug { "Closing since we only fetch certs." }
					closeNotify.invoke()
					responseDeffered.cancel(ClientAbort())
				}
			},
		).use {
			SwiftNioHttpPskClient.performHttpWithHost(
				host = data.url.host,
				port = data.url.port.toLong(),
				method = method,
				path = path,
				headers = headers,
				body = body,
				osslTlsChannelHandler = it.wireNio(),
				completion = { status, headers, body ->
					responseDeffered.complete(
						HttpResponseData(
							statusCode = HttpStatusCode(status.toInt(), "Status"),
							requestTime = GMTDate(),
							headers = headers.toKtorHeaders(),
							version = HttpProtocolVersion.HTTP_1_1,
							body = body?.toByteReadChannel() as Any,
							callContext = callCtx,
						),
					)
				},
				onError = { e ->
					responseDeffered.completeExceptionally(NioEngineError(e))
				},
			)

			responseDeffered.await()
		}
	}
}

private fun NSData?.toByteReadChannel() =
	this?.toByteArray()?.let {
		ByteReadChannel(it, 0, it.size)
	} ?: ByteReadChannel(ByteArray(0))

private fun Map<Any?, *>?.toKtorHeaders(): Headers =
	headersOf(
		*
			this
				?.map {
					it.key.toString() to it.value.toString().split(",")
				}?.toTypedArray() ?: arrayOf(),
	)

private fun Headers.stringMap(): Map<Any?, *> =
	this
		.toMap()
		.mapValues {
			it.value.joinToString(separator = ",")
		}

private fun OutgoingContent.stringBody() =
	when (this) {
		is OutgoingContent.ByteArrayContent -> this.bytes().decodeToString()
		else -> null
	}
