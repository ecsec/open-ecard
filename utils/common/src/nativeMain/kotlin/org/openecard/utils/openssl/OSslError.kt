package org.openecard.utils.openssl

import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.ERR_error_string
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.ERR_get_error
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

private val logger = KotlinLogging.logger { }

class OpenSSLError(
	message: String,
) : Exception(message)

fun Int.assertSuccess() {
	if (this <= 0) throw openSSLError()
}

@OptIn(ExperimentalForeignApi::class)
fun <T : CPointed> CPointer<T>?.assertNotNull(): CPointer<T> =
	when (this) {
		null -> throw openSSLError()
		else -> this
	}

fun openSSLError(): OpenSSLError =
	try {
		OpenSSLError(readOpenSSLError()).also {
			logger.error(it) { "Error in OpenSsl" }
		}
	} catch (e: Exception) {
		logger.error(e) { "Could not read OpenSSL error" }
		OpenSSLError("Unknown OpenSSL error")
	}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
fun readOpenSSLError(): String {
	val codes = mutableListOf<UInt>().also { getCodes(it) }.toList()

	return codes.joinToString("\n", prefix = "Openssl error:") { code ->
		memScoped {
			val buffer = allocArray<ByteVar>(256)
			ERR_error_string(code.convert(), buffer)
			"ERR_CODE: $code -> ${buffer.toKString()}"
		}
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
