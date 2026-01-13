package org.example.project

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.openecard.addons.tr03124.transport.CertTrackingClientBuilder
import org.openecard.addons.tr03124.transport.EserviceCertTracker
import org.openecard.addons.tr03124.transport.SwiftNioEngineFactory
import org.openecard.utils.openssl.TlsConfig
import org.openecard.utils.openssl.TlsPsk
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

actual suspend fun doSth() {
	val host = "oetts.fritz.box"
	val port = 44330
	val portPsk = 44331

	val pskData =
		TlsPsk(
			"ECSEC".toByteArray().toUByteArray().toList(),
			listOf(0xec.toUByte(), 0x5e.toUByte(), 0xcf.toUByte(), 0xff.toUByte()),
		)

	performTest("https://$host:$portPsk/index.html", pskData)
}

private suspend fun performTest(
	url: String,
	psk: TlsPsk? = null,
) {
	val eserviceCertTracker = EserviceCertTracker()

	val response =
		HttpClient(SwiftNioEngineFactory()) {
			engine {
				certTracker = eserviceCertTracker
				tlsConfig =
					TlsConfig(
						ciphersTls12 =
							listOf(
								"RSA-PSK-AES256-CBC-SHA",
								"RSA-PSK-AES256-CBC-SHA384",
								"RSA-PSK-AES128-CBC-SHA256",
								"RSA-PSK-AES256-GCM-SHA384",
								"RSA-PSK-AES128-GCM-SHA256",
							),
						ciphersTls13 =
							listOf(
								"TLS_AES_128_GCM_SHA256",
								"TLS_AES_256_GCM_SHA384",
								"TLS_CHACHA20_POLY1305_SHA256",
							),
						tls13 = true,
						psk = psk,
					)
			}
		}.use {
			withTimeout(30.seconds) {
				it.get(url)
			}
		}
	println("RESULT OF TEST $response")
	assertEquals(
		HttpStatusCode.OK,
		response.status,
	)

	assertEquals(
		"Hello",
		response.bodyAsText().trim(),
	)

	assertTrue("${response.headers}") {
		!response.headers.isEmpty()
	}
}
