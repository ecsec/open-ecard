package org.openecard.utils.openssl

import cnames.structs.bignum_st
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.openecard.openssl.BIGNUM
import org.openecard.openssl.BN_bin2bn
import org.openecard.openssl.BN_bn2bin
import org.openecard.openssl.BN_num_bits

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
