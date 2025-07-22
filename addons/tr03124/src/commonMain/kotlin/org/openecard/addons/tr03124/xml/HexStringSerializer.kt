package org.openecard.addons.tr03124

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.openecard.utils.common.hex

@OptIn(ExperimentalUnsignedTypes::class)
typealias HexString =
	@Serializable(with = HexStringSerializer::class)
	UByteArray

@OptIn(ExperimentalUnsignedTypes::class)
object HexStringSerializer : KSerializer<UByteArray> {
	// Serial names of descriptors should be unique, this is why we advise including app package in the name.
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("org.openecard.addons.tr03124.HexString", PrimitiveKind.STRING)

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun serialize(
		encoder: Encoder,
		value: UByteArray,
	) {
		val string = value.toHexString()
		encoder.encodeString(string)
	}

	override fun deserialize(decoder: Decoder): UByteArray {
		val string = decoder.decodeString()
		val bytes = hex(string)
		return bytes
	}
}
