package org.openecard.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = PrintableByteArraySerializer::class)
@JvmInline
value class PrintableByteArray(
	val v: ByteArray,
) {
	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String = v.toHexString()
}

fun ByteArray.toPrintable() = PrintableByteArray(this)

object PrintableByteArraySerializer : KSerializer<PrintableByteArray> {
	override val descriptor = PrimitiveSerialDescriptor("PrintableByteArraySerializer", PrimitiveKind.STRING)

	override fun serialize(
		encoder: Encoder,
		value: PrintableByteArray,
	) {
		encoder.encodeString(value.toString())
	}

	@OptIn(ExperimentalStdlibApi::class)
	override fun deserialize(decoder: Decoder): PrintableByteArray = decoder.decodeString().hexToByteArray().toPrintable()
}

@Serializable(with = PrintableUByteArraySerializer::class)
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

object PrintableUByteArraySerializer : KSerializer<PrintableUByteArray> {
	override val descriptor = PrimitiveSerialDescriptor("PrintableUByteArraySerializer", PrimitiveKind.STRING)

	override fun serialize(
		encoder: Encoder,
		value: PrintableUByteArray,
	) {
		encoder.encodeString(value.toString())
	}

	@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
	override fun deserialize(decoder: Decoder): PrintableUByteArray =
		decoder.decodeString().hexToUByteArray().toPrintable()
}
