package org.openecard.utils.openssl

import cnames.structs.bignum_st
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BIGNUM
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_CTX
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_CTX_free
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_bin2bn
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_bn2bin
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_free
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.BN_num_bits
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT
import dev.whyoleg.cryptography.providers.openssl3.internal.cinterop.EC_POINT_free
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalForeignApi::class)
fun CPointer<bignum_st>?.toUByteArray(): UByteArray {
	val byteArray = UByteArray((BN_num_bits(this) + 7) / 8)
	byteArray.usePinned {
		BN_bn2bin(this, it.addressOf(0)).assertSuccess()
	}
	return byteArray
}

@OptIn(ExperimentalForeignApi::class)
fun UByteArray.toBigNum(): CPointer<BIGNUM> =
	this.usePinned { pinnedValue ->
		BN_bin2bn(
			s = pinnedValue.addressOf(0),
			len = this.size,
			ret = null,
		).assertNotNull()
	}

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

	companion object {
		inline fun <T> memoryManaged(block: MemoryManager.() -> T): T = MemoryManager().use { it.block() }
	}
}
