package org.openecard.utils.apple

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import kotlin.collections.toUByteArray

@OptIn(ExperimentalForeignApi::class)
fun NSData.toUByteArray(): UByteArray =
	when (val b = bytes) {
		null -> ubyteArrayOf()
		else -> b.readBytes(length.convert()).toUByteArray()
	}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray = bytes?.readBytes(length.convert()) ?: byteArrayOf()

@OptIn(ExperimentalForeignApi::class)
fun UByteArray.toNSData() =
	usePinned {
		NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
	}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData() =
	usePinned {
		NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
	}
