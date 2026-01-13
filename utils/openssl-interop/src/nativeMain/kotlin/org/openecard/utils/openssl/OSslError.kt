package org.openecard.utils.openssl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import org.openecard.openssl.ERR_error_string
import org.openecard.openssl.ERR_get_error

private val logger = KotlinLogging.logger { }

class OpenSSLError(
	message: String,
) : Exception(message)

fun Long.assertSuccess(msg: String? = null) {
	if (this <= 0) throw openSSLError(msg)
}

fun Int.assertSuccess(msg: String? = null) {
	if (this <= 0) throw openSSLError(msg)
}

@OptIn(ExperimentalForeignApi::class)
fun <T : CPointed> CPointer<T>?.assertNotNull(msg: String? = null): CPointer<T> =
	when (this) {
		null -> throw openSSLError("$msg - ossl return value was null but shouldn't")
		else -> this
	}

fun openSSLError(msg: String? = ""): OpenSSLError =
	try {
		OpenSSLError("Message: $msg - osslErrors: ${readOpenSSLError()}").also {
			logger.error(it) { "Error in OpenSsl" }
		}
	} catch (e: Exception) {
		logger.error(e) { "Could not read OpenSSL error" }
		OpenSSLError("Unknown OpenSSL error")
	}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
fun readOpenSSLError(): String {
	val codes = mutableListOf<UInt>().also { getCodes(it) }.toList()

	return if (codes.isNotEmpty()) {
		codes.joinToString("\n", prefix = "Openssl error :") { code ->
			memScoped {
				val buffer = allocArray<ByteVar>(256)
				ERR_error_string(code.convert(), buffer)
				"ERR_CODE: $code -> ${buffer.toKString()}"
			}
		}
	} else {
		"No openssl Errors found"
	}
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
fun getCodes(lst: MutableList<UInt>) {
	when (val c = ERR_get_error().convert<UInt>()) {
		0u -> {}

		else -> {
			lst.add(c)
			getCodes(lst)
		}
	}
}
