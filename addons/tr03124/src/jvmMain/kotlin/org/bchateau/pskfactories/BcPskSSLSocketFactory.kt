/*
 * Copyright (C) 2024 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bchateau.pskfactories

import org.bouncycastle.tls.AlertDescription
import org.bouncycastle.tls.BasicTlsPSKExternal
import org.bouncycastle.tls.CertificateRequest
import org.bouncycastle.tls.HashAlgorithm
import org.bouncycastle.tls.NameType
import org.bouncycastle.tls.NamedGroup
import org.bouncycastle.tls.PSKTlsClient
import org.bouncycastle.tls.ProtocolVersion
import org.bouncycastle.tls.SecurityParameters
import org.bouncycastle.tls.ServerName
import org.bouncycastle.tls.SignatureAlgorithm
import org.bouncycastle.tls.SignatureAndHashAlgorithm
import org.bouncycastle.tls.TlsAuthentication
import org.bouncycastle.tls.TlsClientProtocol
import org.bouncycastle.tls.TlsCredentials
import org.bouncycastle.tls.TlsFatalAlert
import org.bouncycastle.tls.TlsPSK
import org.bouncycastle.tls.TlsPSKExternal
import org.bouncycastle.tls.TlsPSKIdentity
import org.bouncycastle.tls.TlsServerCertificate
import org.bouncycastle.tls.TlsUtils
import org.bouncycastle.tls.crypto.TlsCrypto
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto
import org.openecard.addons.tr03124.Tr03124Config
import org.openecard.utils.common.throwIfNull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.IDN
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.security.Principal
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Collections
import java.util.Vector
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSessionBindingEvent
import javax.net.ssl.SSLSessionBindingListener
import javax.net.ssl.SSLSessionContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * This SSLSocketFactory provides TLS pre-shared key (PSK) cipher suites via Bouncy Castle.
 *
 * When using an instance of this class with OkHttpClient (and possibly other HTTP clients) you
 * must provide an instance of [X509TrustManager] because a null TrustManager is not
 * allowed, but the security of matching pre-shared keys is still enforced.
 */
open class BcPskSSLSocketFactory(
	private val tm: X509TrustManager,
	private val params: BcPskTlsParams,
	private val pskIdentity: TlsPSKIdentity?,
) : SSLSocketFactory() {
	private val crypto: TlsCrypto = BcTlsCrypto(SecureRandom())

	private class BcPskTlsClientProtocol(
		input: InputStream?,
		output: OutputStream?,
	) : TlsClientProtocol(input, output) {
		@Throws(IOException::class)
		override fun close() {
			// Avoid null pointer when "Unable to find acceptable protocols" occurs
			if (peer == null) {
				cleanupHandshake()
			} else {
				super.close()
			}
		}

		@Throws(IOException::class)
		override fun raiseAlertFatal(
			alertDescription: Short,
			message: String?,
			cause: Throwable,
		) {
			cause.printStackTrace()
		}

		@Throws(IOException::class)
		override fun raiseAlertWarning(
			alertDescription: Short,
			message: String?,
		) {
			if (DEBUG) {
				println(message)
			}
		}

		val cipherSuite: String?
			get() {
				val context = context ?: return null
				return BcPskTlsParams.toCipherSuiteString(context.securityParameters.getCipherSuite())
			}

		val protocol: String?
			get() {
				val context = context ?: return null
				return BcPskTlsParams.toJavaName(context.securityParameters.getNegotiatedVersion())
			}

		val sessionId: ByteArray?
			get() {
				val context = context ?: return null
				return context.session.sessionID
			}

		val applicationProtocol: String?
			get() {
				val context = context ?: return null
				return getApplicationProtocol(context.securityParametersConnection)
			}

		companion object {
			fun getApplicationProtocol(securityParameters: SecurityParameters?): String? {
				if (null == securityParameters || !securityParameters.isApplicationProtocolSet) {
					return null
				}

				val applicationProtocol = securityParameters.getApplicationProtocol() ?: return ""

				return applicationProtocol.utf8Decoding
			}
		}
	}

	override fun getDefaultCipherSuites(): Array<String> = params.getSupportedCipherSuites()

	override fun getSupportedCipherSuites(): Array<String> = params.getSupportedCipherSuites()

	/**
	 * Not supported.
	 */
	@Throws(IOException::class, UnknownHostException::class)
	override fun createSocket(
		host: String?,
		port: Int,
	): Socket = throw UnsupportedOperationException()

	/**
	 * Not supported.
	 */
	@Throws(IOException::class)
	override fun createSocket(
		host: InetAddress?,
		port: Int,
	): Socket = throw UnsupportedOperationException()

	/**
	 * Not supported.
	 */
	@Throws(IOException::class, UnknownHostException::class)
	override fun createSocket(
		host: String?,
		port: Int,
		localHost: InetAddress?,
		localPort: Int,
	): Socket = throw UnsupportedOperationException()

	/**
	 * Not supported.
	 */
	@Throws(IOException::class)
	override fun createSocket(
		address: InetAddress?,
		port: Int,
		localAddress: InetAddress?,
		localPort: Int,
	): Socket = throw UnsupportedOperationException()

	/**
	 * This is the only createSocket method that is implemented, it must be used with autoClose set to true and it
	 * requires that the given socket already be connected.
	 */
	@Throws(IOException::class)
	override fun createSocket(
		socket: Socket,
		host: String,
		port: Int,
		autoClose: Boolean,
	): Socket {
		if (!autoClose) {
			throw UnsupportedOperationException("Only auto-close sockets can be created")
		}

		if (!socket.isConnected) {
			throw UnsupportedOperationException("Socket must be connected prior to be used with this factory")
		}

		val tlsClientProtocol = BcPskTlsClientProtocol(socket.getInputStream(), socket.getOutputStream())

		return object : WrappedSSLSocket(socket) {
			private var enabledCipherSuites: Array<String> = params.getSupportedCipherSuites()
			private var enabledProtocols: Array<String> = params.getSupportedProtocols()

			private var enableSessionCreation = true

			@Throws(IOException::class)
			override fun getInputStream(): InputStream? = tlsClientProtocol.getInputStream()

			@Throws(IOException::class)
			override fun getOutputStream(): OutputStream? = tlsClientProtocol.getOutputStream()

			@Synchronized
			@Throws(IOException::class)
			override fun close() {
				super.close()
				synchronized(tlsClientProtocol) {
					tlsClientProtocol.close()
				}
			}

			override fun getApplicationProtocol(): String? = tlsClientProtocol.applicationProtocol

			override fun getEnableSessionCreation(): Boolean = enableSessionCreation

			override fun getEnabledCipherSuites(): Array<String> = enabledCipherSuites.clone()

			override fun getEnabledProtocols(): Array<String> = enabledProtocols.clone()

			override fun getNeedClientAuth(): Boolean = false

			override fun getSupportedProtocols(): Array<String> = params.getSupportedProtocols()

			override fun getUseClientMode(): Boolean = false

			override fun getWantClientAuth(): Boolean = false

			override fun setEnabledCipherSuites(suites: Array<String>) {
				val supported: MutableSet<String> = HashSet()
				supported.addAll(getSupportedCipherSuites())

				val enabled: MutableList<String> = ArrayList()
				for (s in suites) {
					if (supported.contains(s)) {
						enabled.add(s)
					}
				}
				enabledCipherSuites = enabled.toTypedArray()
			}

			override fun setEnableSessionCreation(flag: Boolean) {
				enableSessionCreation = flag
			}

			override fun setEnabledProtocols(protocols: Array<String>) {
				val supported: MutableSet<String> = HashSet()
				supported.addAll(getSupportedProtocols())

				val enabled: MutableList<String> = ArrayList()
				for (s in protocols) {
					if (supported.contains(s)) {
						enabled.add(s)
					}
				}
				enabledProtocols = enabled.toTypedArray()
			}

			override fun setNeedClientAuth(need: Boolean) {
				// Ignored, PSK ensures mutual auth
			}

			override fun setUseClientMode(mode: Boolean) {
				// Ignored, PSK ensures mutual auth
			}

			override fun setWantClientAuth(want: Boolean) {
				// Ignored, PSK ensures mutual auth
			}

			override fun getSupportedCipherSuites(): Array<String> = params.getSupportedCipherSuites()

			@Throws(IOException::class)
			override fun startHandshake() {
				tlsClientProtocol.connect(
					object : PSKTlsClient(crypto, pskIdentity) {
						override fun getSupportedVersions(): Array<ProtocolVersion> =
							BcPskTlsParams.fromSupportedProtocolVersions(getEnabledProtocols())

						override fun getSupportedCipherSuites(): IntArray =
							BcPskTlsParams.fromSupportedCipherSuiteCodes(getEnabledCipherSuites())

						override fun getSupportedGroups(namedGroupRoles: Vector<*>?): Vector<*> {
							val crypto = getCrypto()
							val supportedGroups: Vector<*> = Vector<Any?>()

							if (Tr03124Config.nonBsiApprovedCiphers) {
								TlsUtils.addIfSupported(
									supportedGroups,
									crypto,
									intArrayOf(NamedGroup.x25519, NamedGroup.x448),
								)
							}

							TlsUtils.addIfSupported(
								supportedGroups,
								crypto,
								intArrayOf(NamedGroup.secp256r1, NamedGroup.secp384r1, NamedGroup.secp521r1),
							)

							return supportedGroups
						}

						override fun getSupportedSignatureAlgorithms(): Vector<*> {
							val crypto = getCrypto()
							val supportedAlgs: Vector<*> = Vector<Any?>()

							if (Tr03124Config.nonBsiApprovedCiphers) {
								TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.ed25519)
								TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.ed448)
							}

							TlsUtils.addIfSupported(
								supportedAlgs,
								crypto,
								SignatureAndHashAlgorithm.getInstance(
									HashAlgorithm.sha256,
									SignatureAlgorithm.ecdsa,
								),
							)
							TlsUtils.addIfSupported(
								supportedAlgs,
								crypto,
								SignatureAndHashAlgorithm.getInstance(
									HashAlgorithm.sha384,
									SignatureAlgorithm.ecdsa,
								),
							)
							TlsUtils.addIfSupported(
								supportedAlgs,
								crypto,
								SignatureAndHashAlgorithm.getInstance(
									HashAlgorithm.sha512,
									SignatureAlgorithm.ecdsa,
								),
							)
							TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.rsa_pss_pss_sha256)
							TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.rsa_pss_pss_sha384)
							TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.rsa_pss_pss_sha512)
							TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.rsa_pss_rsae_sha256)
							TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.rsa_pss_rsae_sha384)
							TlsUtils.addIfSupported(supportedAlgs, crypto, SignatureAndHashAlgorithm.rsa_pss_rsae_sha512)
							TlsUtils.addIfSupported(
								supportedAlgs,
								crypto,
								SignatureAndHashAlgorithm.getInstance(
									HashAlgorithm.sha256,
									SignatureAlgorithm.rsa,
								),
							)
							TlsUtils.addIfSupported(
								supportedAlgs,
								crypto,
								SignatureAndHashAlgorithm.getInstance(
									HashAlgorithm.sha384,
									SignatureAlgorithm.rsa,
								),
							)
							TlsUtils.addIfSupported(
								supportedAlgs,
								crypto,
								SignatureAndHashAlgorithm.getInstance(
									HashAlgorithm.sha512,
									SignatureAlgorithm.rsa,
								),
							)

							return supportedAlgs
						}

						override fun getExternalPSKs(): Vector<*> {
							val externals = Vector<TlsPSKExternal>()
							val secret = crypto.createSecret(pskIdentity.psk)
							externals.add(BasicTlsPSKExternal(pskIdentity.pskIdentity, secret))
							return externals
						}

						override fun notifySelectedPSK(selectedPSK: TlsPSK?) {
							throwIfNull(selectedPSK) { TlsFatalAlert(AlertDescription.handshake_failure) }
						}

						override fun getSNIServerNames(): Vector<ServerName> = Vector(listOf(host.hostNameToServerName()))

						override fun getAuthentication(): TlsAuthentication =
							object : TlsAuthentication {
								override fun notifyServerCertificate(serverCert: TlsServerCertificate?) {
									val certFac = CertificateFactory.getInstance("X.509")
									peerCerts =
										serverCert?.certificate?.certificateList?.map {
											certFac.generateCertificate(it.encoded.inputStream()) as X509Certificate
										}

									// validate cert
									peerCerts?.toTypedArray()?.let {
										tm.checkServerTrusted(it, "UNKNOWN")
									}
								}

								override fun getClientCredentials(certificateRequest: CertificateRequest?): TlsCredentials? = null
							}
					},
				)
			}

			private var peerCerts: List<X509Certificate>? = null

			private fun String.hostNameToServerName(): ServerName {
				var name = this
				name = IDN.toASCII(name)
				return ServerName(NameType.host_name, name.toByteArray(StandardCharsets.US_ASCII))
			}

			override fun getSession(): SSLSession {
				return object : SSLSession {
					private var isValid = true

					/**
					 * Maximum length of allowed plain data fragment
					 * as specified by TLS specification.
					 */
					protected val MAX_DATA_LENGTH: Int = 16384 // 2^14

					/**
					 * Maximum length of allowed compressed data fragment
					 * as specified by TLS specification.
					 */
					protected val MAX_COMPRESSED_DATA_LENGTH: Int =
						MAX_DATA_LENGTH + 1024

					/**
					 * Maximum length of allowed ciphered data fragment
					 * as specified by TLS specification.
					 */
					protected val MAX_CIPHERED_DATA_LENGTH: Int =
						MAX_COMPRESSED_DATA_LENGTH + 1024

					/**
					 * Maximum length of ssl record. It is counted as:
					 * type(1) + version(2) + length(2) + MAX_CIPHERED_DATA_LENGTH
					 */
					protected val MAX_SSL_PACKET_SIZE: Int =
						MAX_CIPHERED_DATA_LENGTH + 5

					private val creationTime = System.currentTimeMillis()
					private val valueMap: MutableMap<String, Any?> =
						Collections.synchronizedMap(HashMap())

					override fun getApplicationBufferSize(): Int = tlsClientProtocol.applicationDataLimit

					override fun getCipherSuite(): String? = tlsClientProtocol.cipherSuite

					override fun getCreationTime(): Long = creationTime

					override fun getId(): ByteArray? = tlsClientProtocol.sessionId

					override fun getLastAccessedTime(): Long = getCreationTime()

					override fun getLocalCertificates(): Array<Certificate>? = null

					override fun getLocalPrincipal(): Principal = throw UnsupportedOperationException()

					override fun getPacketBufferSize(): Int = MAX_SSL_PACKET_SIZE

					@Throws(SSLPeerUnverifiedException::class)
					override fun getPeerCertificates(): Array<Certificate> =
						peerCerts?.toTypedArray()
							?: throw SSLPeerUnverifiedException("No certificate received from server")

					override fun getPeerHost(): String = host

					override fun getPeerPort(): Int = port

					@Throws(SSLPeerUnverifiedException::class)
					override fun getPeerPrincipal(): Principal? = null

					override fun getProtocol(): String? = tlsClientProtocol.protocol

					override fun getSessionContext(): SSLSessionContext = throw UnsupportedOperationException()

					override fun getValue(name: String): Any? = valueMap[name]

					override fun getValueNames(): Array<String> {
						synchronized(valueMap) {
							return valueMap.keys.toTypedArray()
						}
					}

					override fun invalidate() {
						isValid = false
					}

					override fun isValid(): Boolean {
						// TODO: Check session time limit
						return isValid && !socket.isClosed && !tlsClientProtocol.isClosed
					}

					override fun putValue(
						name: String,
						value: Any?,
					) {
						notifyUnbound(name, valueMap.put(name, value))
						notifyBound(name, value)
					}

					override fun removeValue(name: String) {
						notifyUnbound(name, valueMap.remove(name))
					}

					fun notifyBound(
						name: String,
						value: Any?,
					) {
						if (value is SSLSessionBindingListener) {
							value.valueBound(SSLSessionBindingEvent(this, name))
						}
					}

					fun notifyUnbound(
						name: String,
						value: Any?,
					) {
						if (value is SSLSessionBindingListener) {
							value.valueUnbound(SSLSessionBindingEvent(this, name))
						}
					}
				}
			}
		}
	}

	companion object {
		private const val DEBUG = false
	}
}
