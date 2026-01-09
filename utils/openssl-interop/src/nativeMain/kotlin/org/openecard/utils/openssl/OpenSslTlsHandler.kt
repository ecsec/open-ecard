package org.openecard.utils.openssl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.set
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import org.openecard.openssl.BIO_new
import org.openecard.openssl.BIO_read
import org.openecard.openssl.BIO_s_mem
import org.openecard.openssl.BIO_write
import org.openecard.openssl.CRYPTO_EX_INDEX_SSL
import org.openecard.openssl.CRYPTO_get_ex_new_index
import org.openecard.openssl.SSL
import org.openecard.openssl.SSL_CIPHER_get_name
import org.openecard.openssl.SSL_CTRL_SET_TLSEXT_HOSTNAME
import org.openecard.openssl.SSL_CTX
import org.openecard.openssl.SSL_CTX_free
import org.openecard.openssl.SSL_CTX_new
import org.openecard.openssl.SSL_CTX_set_cipher_list
import org.openecard.openssl.SSL_CTX_set_ciphersuites
import org.openecard.openssl.SSL_CTX_set_options
import org.openecard.openssl.SSL_CTX_set_psk_client_callback
import org.openecard.openssl.SSL_CTX_set_verify
import org.openecard.openssl.SSL_ERROR_SYSCALL
import org.openecard.openssl.SSL_ERROR_WANT_READ
import org.openecard.openssl.SSL_ERROR_WANT_WRITE
import org.openecard.openssl.SSL_ERROR_ZERO_RETURN
import org.openecard.openssl.SSL_OP_NO_SSLv3
import org.openecard.openssl.SSL_OP_NO_TLSv1
import org.openecard.openssl.SSL_OP_NO_TLSv1_1
import org.openecard.openssl.SSL_OP_NO_TLSv1_3
import org.openecard.openssl.SSL_VERIFY_NONE
import org.openecard.openssl.SSL_ctrl
import org.openecard.openssl.SSL_do_handshake
import org.openecard.openssl.SSL_free
import org.openecard.openssl.SSL_get1_peer_certificate
import org.openecard.openssl.SSL_get_current_cipher
import org.openecard.openssl.SSL_get_error
import org.openecard.openssl.SSL_get_ex_data
import org.openecard.openssl.SSL_get_shutdown
import org.openecard.openssl.SSL_new
import org.openecard.openssl.SSL_read
import org.openecard.openssl.SSL_set_bio
import org.openecard.openssl.SSL_set_connect_state
import org.openecard.openssl.SSL_set_ex_data
import org.openecard.openssl.SSL_shutdown
import org.openecard.openssl.SSL_state_string_long
import org.openecard.openssl.SSL_write
import org.openecard.openssl.TLSEXT_NAMETYPE_host_name
import org.openecard.openssl.TLS_client_method
import org.openecard.openssl.i2d_X509
import org.openecard.utils.openssl.MemoryManager.Companion.memoryManaged
import kotlin.collections.toUByteArray

private val logger = KotlinLogging.logger { }

class TlsPsk(
	val identity: List<UByte>,
	val pskVal: List<UByte>,
)

class TlsConfig(
	// null means all available ciphersuites
	val ciphersTls12: List<String>? = null,
	val tls13: Boolean = true,
	val verifyPeer: Boolean = false,
	// null means all available ciphersuites
	val ciphersTls13: List<String>? = null,
	val psk: TlsPsk? = null,
)

@OptIn(ExperimentalForeignApi::class)
private val pskExDataIndex: Int by lazy {
	CRYPTO_get_ex_new_index(
		CRYPTO_EX_INDEX_SSL,
		0,
		null,
		null,
		null,
		staticCFunction { _, ptr, _, _, _, _ ->
			ptr?.asStableRef<TlsPsk>()?.dispose()
		},
	)
}

@OptIn(ExperimentalForeignApi::class)
fun pskCallback() =
	staticCFunction<
		CPointer<SSL>?,
		CPointer<ByteVar>?,
		CPointer<ByteVar>?,
		UInt,
		CPointer<UByteVar>?,
		UInt,
		UInt,
	> { ssl, hint, ident, maxIdentityLen, psk, maxPskLen ->

		val tlsPsk =
			ssl
				?.let { SSL_get_ex_data(it, pskExDataIndex) }
				?.asStableRef<TlsPsk>()
				?.get()

		when (tlsPsk) {
			null -> {
				logger.error { "PSK value was null" }
				0u
			}

			else -> {
				if (tlsPsk.identity.size + 1 > maxIdentityLen.convert<Int>()) {
					logger.error { "pskRef identity to big" }
					0u
				} else {
					tlsPsk.identity.forEachIndexed { i, b ->
						ident?.set(i, b.toByte())
					}
					ident?.set(tlsPsk.identity.size, 0)

					if (tlsPsk.pskVal.size > maxPskLen.convert<Int>()) {
						logger.error { "pskRef val to big" }
						0u
					} else {
						tlsPsk.pskVal.forEachIndexed { i, b ->
							psk?.set(i, b)
						}
						tlsPsk.pskVal.size.convert<UInt>().also {
							logger.debug { "all good returing $it" }
						}
					}
				}
			}
		}
	}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
class OpenSslTlsHandler(
	ctx: CPointer<SSL_CTX>,
	val host: String,
	psk: TlsPsk?,
	val onPeerCert: ((handler: OpenSslTlsHandler, ByteArray?) -> Unit)?,
) : AutoCloseable {
	companion object {
		fun sslCtx(tlsConfig: TlsConfig): CPointer<SSL_CTX> {
			logger.debug { "Creating SSL-Context" }
			return SSL_CTX_new(
				TLS_client_method(),
			).assertNotNull().also {
				SSL_CTX_set_options(
					it,
					SSL_OP_NO_TLSv1 or
						SSL_OP_NO_TLSv1_1 or
						SSL_OP_NO_SSLv3 or
						if (!tlsConfig.tls13) SSL_OP_NO_TLSv1_3 else 0u,
				)

				if (!tlsConfig.verifyPeer) {
					SSL_CTX_set_verify(it, SSL_VERIFY_NONE, null)
				}

				SSL_CTX_set_psk_client_callback(
					it,
					pskCallback(),
				)

				tlsConfig.ciphersTls12?.let { ciphers ->
					SSL_CTX_set_cipher_list(
						it,
						ciphers.joinToString(":"),
					).assertSuccess()
				}

				tlsConfig.ciphersTls13?.let { ciphers ->
					SSL_CTX_set_ciphersuites(
						it,
						ciphers.joinToString(":"),
					).assertSuccess()
				}
			}
		}

		fun freeCtx(ctx: CPointer<SSL_CTX>) = SSL_CTX_free(ctx)
	}

	override fun close() {
		SSL_free(ssl)
	}

	private val ssl = SSL_new(ctx).assertNotNull()
	private val bioIn = BIO_new(BIO_s_mem()).assertNotNull()
	private val bioOut = BIO_new(BIO_s_mem()).assertNotNull()

	private var handshakeComplete = false
	private val outBoundBuffer: MutableList<ByteArray> = mutableListOf()

	private var closing = false
	private var closingDrained = false

	init {
		SSL_ctrl(
			ssl,
			SSL_CTRL_SET_TLSEXT_HOSTNAME,
			TLSEXT_NAMETYPE_host_name.convert(),
			host.cstr,
		)

		psk?.let {
			logger.debug { "Setting psk values: ${it.identity} - ${it.pskVal.toUByteArray().toHexString()}" }

			SSL_set_ex_data(
				ssl,
				pskExDataIndex,
				StableRef.create(psk).asCPointer(),
			)
		}

		SSL_set_bio(ssl, bioIn, bioOut)
		SSL_set_connect_state(ssl)
	}

	@OptIn(ExperimentalForeignApi::class)
	private fun getPeerCertDer(): ByteArray? =
		memoryManaged {
			val peerCert = SSL_get1_peer_certificate(ssl)
			if (peerCert == null) {
				logger.warn { "Peer certificate could not be read." }
			}
			peerCert?.autoFree().let {
				val len = i2d_X509(it, null)
				if (len <= 0) {
					logger.warn { "Peer certificate has len <=0." }
					null
				} else {
					val buf = UByteArray(len)
					buf.usePinned { pinned ->
						val p = pinned.addressOf(0)
						i2d_X509(it, cValuesOf(p))
					}
					return buf.toByteArray().also { b ->
						logger.debug { "CERTBYTES (might be cut off): ${b.toHexString()}" }
					}
				}
			}
		}

	fun handshake() {
		logger.debug { "SSL-Handshake step: ${SSL_state_string_long(ssl)?.toKString()}" }
		val result = SSL_do_handshake(ssl)
		if (result == 1) {
			logger.debug { "SSL-Handshake complete - state: ${SSL_state_string_long(ssl)?.toKString()}" }

			handshakeComplete = true
			val cipher = SSL_get_current_cipher(ssl)
			logger.debug {
				"Host: $host - agreed cipher: ${ SSL_CIPHER_get_name(cipher)?.toKString() }"
			}

			onPeerCert?.invoke(this, getPeerCertDer())

			flushOutBoundBufferToSSL()
			return
		}
		handleSslErrorReturn(result)
	}

	private fun handleSslErrorReturn(err: Int) =
		when (val e = SSL_get_error(ssl, err)) {
			// ignored since we go on with flushing and next events
			SSL_ERROR_WANT_READ,
			SSL_ERROR_WANT_WRITE,
			-> {}

			else -> {
				e.assertSuccess()
			}
		}

	fun inboundEncrypted(data: ByteArray) {
		logger.debug { "inbound encrypted data. size: ${data.size}" }
		if (closing) return
		data.usePinned { BIO_write(bioIn, it.addressOf(0), data.size) }
		if (!handshakeComplete) {
			handshake()
		}
	}

	private fun flushOutBoundBufferToSSL() {
		logger.debug { "flushing outbound buffer to ssl-write" }
		while (outBoundBuffer.isNotEmpty()) {
			val toWrite = outBoundBuffer.first()
			toWrite.usePinned {
				val written = SSL_write(ssl, it.addressOf(0), toWrite.size)
				if (written > 0) {
					if (written < toWrite.size) {
						logger.debug { "flushing outbound: Attention partial write of current: " }
						outBoundBuffer[0] = toWrite.copyOfRange(written, toWrite.size)
					} else {
						logger.debug { "written a bufferelement: " }
						outBoundBuffer.removeFirst()
					}
				} else {
					// we return to give the swiftside a chance to send or read data
					return
				}
			}
		}
	}

	fun outboundPlain(data: ByteArray) {
		logger.debug { "tls: outbound plain data: ${data.toHexString()}" }
		outBoundBuffer.add(data.copyOf())
		if (!handshakeComplete) {
			handshake()
		} else {
			flushOutBoundBufferToSSL()
		}
	}

	fun closeNotify() {
		logger.debug { "close notify - closing connection" }
		val res = SSL_shutdown(ssl)
		closing = true
		// 0 is valid here so assert only negative
		if (res < 0) {
			res.assertSuccess()
		}
	}

	fun readPlaintext(): ByteArray? {
		logger.debug { "tls: read plaintext" }
		if (SSL_get_shutdown(ssl) > 0) return null
		val out = mutableListOf<Byte>()
		while (true) {
			val buf = ByteArray(4 * 1024)
			val n = buf.usePinned { SSL_read(ssl, it.addressOf(0), buf.size) }
			if (n > 0) {
				out.addAll(buf.take(n))
			} else {
				val err = SSL_get_error(ssl, n)
				logger.debug {
					"ssl read error: (${
						when (err) {
							SSL_ERROR_ZERO_RETURN -> "SSL_ERROR_ZERO_RETURN"
							SSL_ERROR_WANT_READ -> "SSL_ERROR_WANT_READ"
							SSL_ERROR_WANT_WRITE -> "SSL_ERROR_WANT_WRITE"
							SSL_ERROR_SYSCALL -> "SSL_ERROR_SYSCALL"
							else -> "UNKNOWN"
						}
					})"
				}
				if (err == SSL_ERROR_ZERO_RETURN) {
					logger.debug { "ZERO_RETURN - basically FIN so we're closing now" }
					closeNotify()
				}
				break
			}
		}
		return if (out.isEmpty()) {
			null
		} else {
			out.toByteArray().also {
				logger.debug { "tls: OUTPUT: ${it.toKString()}" }
			}
		}
	}

	fun readEncryptedOutput(): ByteArray? {
		logger.debug { "read encrypted output from ssl" }
		if (closingDrained) {
			return null
		}
		val out = mutableListOf<Byte>()
		while (true) {
			val buf = ByteArray(4 * 1024)
			val n = buf.usePinned { BIO_read(bioOut, it.addressOf(0), buf.size) }
			when {
				n > 0 -> {
					out.addAll(buf.take(n).toList())
				}

				else -> {
					logger.debug { "read encrypted error biod_read error: $n" }
					break
				}
			}
		}
		if (closing) {
			closingDrained = true
		}
		return if (out.isEmpty()) null else out.toByteArray()
	}
}
