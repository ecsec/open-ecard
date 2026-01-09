package org.openecard.utils.openssl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import org.openecard.openssl.BIO_C_DO_STATE_MACHINE
import org.openecard.openssl.BIO_ctrl
import org.openecard.openssl.BIO_new_connect
import org.openecard.openssl.OPENSSL_init_ssl
import org.openecard.openssl.SSL_CIPHER_get_name
import org.openecard.openssl.SSL_CTX
import org.openecard.openssl.SSL_CTX_free
import org.openecard.openssl.SSL_CTX_new
import org.openecard.openssl.SSL_ERROR_SYSCALL
import org.openecard.openssl.SSL_ERROR_WANT_READ
import org.openecard.openssl.SSL_ERROR_WANT_WRITE
import org.openecard.openssl.SSL_ERROR_ZERO_RETURN
import org.openecard.openssl.SSL_connect
import org.openecard.openssl.SSL_free
import org.openecard.openssl.SSL_get_current_cipher
import org.openecard.openssl.SSL_get_error
import org.openecard.openssl.SSL_new
import org.openecard.openssl.SSL_read
import org.openecard.openssl.SSL_set_bio
import org.openecard.openssl.SSL_shutdown
import org.openecard.openssl.SSL_write
import org.openecard.openssl.TLS_client_method
import org.openecard.openssl.TLS_method
import kotlin.AutoCloseable
import kotlin.ByteArray
import kotlin.Int
import kotlin.OptIn
import kotlin.String
import kotlin.let

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalForeignApi::class)
class TlsHandler : AutoCloseable {
	@OptIn(ExperimentalForeignApi::class)
	override fun close() {
		SSL_CTX_free(ctx)
	}

	val ctx: CPointer<SSL_CTX>
		get() {
			return SSL_CTX_new(TLS_method()).assertNotNull()
		}

	@OptIn(UnsafeNumber::class)
	fun connect(
		host: String,
		port: Int,
		path: String,
	): String {
		// Initialize OpenSSL (3.x)
		OPENSSL_init_ssl(0u, null).assertSuccess()

		val ctx = SSL_CTX_new(TLS_client_method()).assertNotNull()
		val ssl = SSL_new(ctx).assertNotNull()

		// ---- Create BIO that opens the TCP connection internally ----
		val bio =
			BIO_new_connect("$host:$port").assertNotNull()

		BIO_ctrl(bio, BIO_C_DO_STATE_MACHINE, 0, null).assertSuccess("bio-c")

		SSL_set_bio(ssl, bio, bio)

		SSL_connect(ssl).assertSuccess("sslconn")

		logger.debug { "Cipher: ${SSL_get_current_cipher(ssl)?.let { SSL_CIPHER_get_name(it) }?.toKString()}" }

		// Send HTTP request
		val req = "GET $path HTTP/1.1\r\nHost: $host\r\nConnection: close\r\n\r\n"
		SSL_write(ssl, req.cstr, req.length).assertSuccess()

		val res = StringBuilder()

		// Read response
		memScoped {
			val buf = ByteArray(4096)
			buf.usePinned {
				val cbuf = it.addressOf(0)
				while (true) {
					val n = SSL_read(ssl, cbuf, buf.size)
					if (n > 0) {
						res.append(buf.decodeToString(0, n))
					}

					val err = SSL_get_error(ssl, n)

					when (err) {
						SSL_ERROR_WANT_READ, SSL_ERROR_WANT_WRITE -> {
							continue
						}

						SSL_ERROR_ZERO_RETURN,
						SSL_ERROR_SYSCALL,
						-> {
							break
						}

						else -> {
							n.assertSuccess()
							break
						}
					}
				}
			}
		}

		SSL_shutdown(ssl)
		SSL_free(ssl)
		SSL_CTX_free(ctx)

		return res.toString()
	}
}
