import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import kotlin.collections.toUByteArray

@OptIn(ExperimentalForeignApi::class)
fun NSData.toUByteArray(): UByteArray? =
	if (length > 0.toUInt()) {
		bytes?.readBytes(length.toInt())?.toUByteArray()
	} else {
		null
	}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray? = if (length > 0.toUInt()) bytes?.readBytes(length.toInt()) else null

@OptIn(ExperimentalForeignApi::class)
fun UByteArray.toNSData() =
	usePinned {
		NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
	}
