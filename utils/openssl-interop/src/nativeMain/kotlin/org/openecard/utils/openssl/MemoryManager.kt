package org.openecard.utils.openssl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import org.openecard.openssl.BIGNUM
import org.openecard.openssl.BN_CTX
import org.openecard.openssl.BN_CTX_free
import org.openecard.openssl.BN_free
import org.openecard.openssl.EC_POINT
import org.openecard.openssl.EC_POINT_free
import org.openecard.openssl.X509
import org.openecard.openssl.X509_free

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalForeignApi::class)
class MemoryManager : AutoCloseable {
	private val freeFunctions = mutableListOf<() -> Unit>()

	override fun close() {
		freeFunctions.forEach { f -> f() }
	}

	fun CPointer<BIGNUM>.autoFree() =
		apply {
			logger.debug { "Registering CPointer<BIGNUM> for freeing: $this" }
			freeFunctions.add {
				logger.debug { "Freeing CPointer<BIGNUM>: $this" }
				BN_free(this)
			}
		}

	fun CPointer<BN_CTX>.autoFree() =
		apply {
			logger.debug { "Registering CPointer<BN_CTX> for freeing: $this" }
			freeFunctions.add {
				logger.debug { "Freeing CPointer<BN_CTX>: $this" }
				BN_CTX_free(this)
			}
		}

	fun CPointer<EC_POINT>.autoFree() =
		apply {
			logger.debug { "Registering CPointer<EC_POINT> for freeing: $this" }
			freeFunctions.add {
				logger.debug { "Freeing CPointer<EC_POINT>: $this" }
				EC_POINT_free(this)
			}
		}

	fun CValuesRef<EC_POINT>.autoFree() =
		apply {
			logger.debug { "Registering CValuesRef<EC_POINT> for freeing: $this" }
			freeFunctions.add {
				logger.debug { "Freeing CValuesRef<EC_POINT>: $this" }
				EC_POINT_free(this)
			}
		}

	fun CPointer<X509>.autoFree() =
		apply {
			logger.debug { "Registering CPointer<X509> for freeing: $this" }
			freeFunctions.add {
				logger.debug { "Freeing CPointer<X509>: $this" }
				X509_free(this)
			}
		}

	companion object {
		inline fun <T> memoryManaged(block: MemoryManager.() -> T): T = MemoryManager().use { it.block() }
	}
}
