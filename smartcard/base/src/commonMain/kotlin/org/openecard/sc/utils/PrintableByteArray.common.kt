package org.openecard.sc.utils

import kotlin.jvm.JvmInline

@JvmInline
value class PrintableByteArray(
	val v: ByteArray,
) {
	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String = v.toHexString()
}

fun ByteArray.toPrintable() = PrintableByteArray(this)
