package org.openecard.addons.tr03124.transport

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.utils.io.core.toByteArray
import org.openecard.addons.tr03124.Tr03124Config
import org.openecard.addons.tr03124.transport.EidServerPaos.Companion.registerPaosNegotiation
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.utils.common.doIf
import org.openecard.utils.openssl.TlsConfig
import org.openecard.utils.openssl.TlsPsk

private fun tlsConfigBase() =
	TlsConfig(
		ciphersTls12 =
			buildList {
				doIf(Tr03124Config.nonBsiApprovedCiphers) {
					add("ECDHE-ECDSA-CHACHA20-POLY1305")
					add("ECDHE-RSA-CHACHA20-POLY1305")
				}
				add("ECDHE-ECDSA-AES256-GCM-SHA384")
				add("ECDHE-RSA-AES256-GCM-SHA384")
				add("ECDHE-ECDSA-AES128-GCM-SHA256")
				add("ECDHE-RSA-AES128-GCM-SHA256")

				// TLSv1.2 weak
				add("ECDHE-ECDSA-AES256-SHA384")
				add("ECDHE-ECDSA-AES128-SHA256")
			},
		ciphersTls13 =
			buildList {
				add("TLS_AES_128_GCM_SHA256")
				add("TLS_AES_256_GCM_SHA384")
				doIf(Tr03124Config.nonBsiApprovedCiphers) {
					add("TLS_CHACHA20_POLY1305_SHA256")
				}
			},
	)

private fun tlsConfigPsk(psk: TlsPsk) =
	TlsConfig(
		tls13 = Tr03124Config.nonBsiApprovedCiphers,
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
		psk = psk,
	)

class CertTrackingClientBuilder(
	val eserviceCertTracker: EserviceCertTracker,
) : KtorClientBuilder {
	override val tokenClient: HttpClient
		get() =
			HttpClient(SwiftNioEngineFactory()) {
				engine {
					certTracker = eserviceCertTracker
					tlsConfig = tlsConfigBase()
				}
				followRedirects = true
			}
	override val redirectClient: HttpClient
		get() =
			HttpClient(SwiftNioEngineFactory()) {
				engine {
					certTracker = eserviceCertTracker
					tlsConfig = tlsConfigBase()
				}
				followRedirects = false
			}

	override val checkCertClient: CertValidationClient
		get() =
			object : CertValidationClient {
				val client =
					HttpClient(SwiftNioEngineFactory()) {
						engine {
							certTracker = eserviceCertTracker
							tlsConfig = tlsConfigBase()
							performHttp = false
						}
					}

				override suspend fun checkCert(url: String) {
					client.use {
						try {
							it.get(url)
						} catch (e: ClientAbort) {
							// That's what we want, cause we're just interested in peer certificate
							// client throws ClientAbort after handshake
						}
					}
				}
			}

	override fun buildEidServerClient(token: TcToken.TcTokenOk): HttpClient =
		when (token) {
			is TcToken.TcTokenAttached -> {
				throw NotImplementedError()
			}

			is TcToken.TcTokenPsk -> {
				buildPskClient(token.sessionIdentifier, token.psk.v.toList())
			}
		}

	private fun buildPskClient(
		session: String,
		psk: List<UByte>,
	) = HttpClient(SwiftNioEngineFactory()) {
		engine {
			tlsConfig =
				tlsConfigPsk(
					TlsPsk(
						session.toByteArray().toUByteArray().toList(),
						psk,
					),
				)
			certTracker = eserviceCertTracker
		}

		Tr03124Config.paosLog?.let {
			install(Logging, it)
		}
		registerPaosNegotiation()
		followRedirects = false
	}
}
