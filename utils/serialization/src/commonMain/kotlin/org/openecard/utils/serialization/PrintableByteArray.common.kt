package org.openecard.utils.serialization

import kotlin.jvm.JvmInline

@JvmInline
value class PrintableByteArray(
	val v: ByteArray,
) {
	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String = v.toHexString()
}

fun ByteArray.toPrintable() = PrintableByteArray(this)

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
value class PrintableUByteArray
	constructor(
		val v: UByteArray,
	) {
		override fun toString(): String = v.toHexString()
	}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toPrintable() = PrintableUByteArray(this)
