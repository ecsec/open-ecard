package org.openecard.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PrintableByteArraySerializer::class)
class PrintableByteArray(
	val v: ByteArray,
) {
	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String = v.toHexString()

	override fun equals(other: Any?): Boolean =
		when (other) {
			is PrintableByteArray -> this.v.contentEquals(other.v)
			else -> false
		}

	override fun hashCode(): Int = v.contentHashCode()
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
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
class PrintableUByteArray
	constructor(
		val v: UByteArray,
	) {
		override fun toString(): String = v.toHexString()

		override fun equals(other: Any?): Boolean =
			when (other) {
				is PrintableUByteArray -> this.v.contentEquals(other.v)
				else -> false
			}

		override fun hashCode(): Int = v.contentHashCode()
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
