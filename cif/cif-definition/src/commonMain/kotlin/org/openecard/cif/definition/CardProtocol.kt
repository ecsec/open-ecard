package org.openecard.cif.definition

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(CardProtocolAsStringSerializer::class)
sealed interface CardProtocol {
	@Serializable
	enum class Technical : CardProtocol {
		T0,
		T1,
		T15,
		TCL,
	}

	@Serializable
	enum class Grouped : CardProtocol {
		CONTACT,
		CONTACTLESS,
	}

	@Serializable
	object Any : CardProtocol {
		override fun toString(): String = "Any"
	}
}

class CardProtocolComparator : Comparator<CardProtocol> {
	override fun compare(
		a: CardProtocol,
		b: CardProtocol,
	): Int = a.ordinal().compareTo(b.ordinal())
}

fun CardProtocol.ordinal(): Int =
	when (this) {
		CardProtocol.Any -> 7
		CardProtocol.Grouped.CONTACT -> 5
		CardProtocol.Grouped.CONTACTLESS -> 6
		CardProtocol.Technical.T0 -> 1
		CardProtocol.Technical.T1 -> 2
		CardProtocol.Technical.T15 -> 3
		CardProtocol.Technical.TCL -> 4
	}

object CardProtocolAsStringSerializer : KSerializer<CardProtocol> {
	// Serial names of descriptors should be unique, this is why we advise including app package in the name.
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor(Any::class.qualifiedName!!, PrimitiveKind.STRING)

	private const val ANY_CONSTANT = "CardProtocol::Any"

	override fun serialize(
		encoder: Encoder,
		value: CardProtocol,
	) {
		val string =
			when (value) {
				CardProtocol.Any -> encoder.encodeString(ANY_CONSTANT)
				is CardProtocol.Grouped -> encoder.encodeString(value.name)
				is CardProtocol.Technical -> encoder.encodeString(value.name)
			}
	}

	override fun deserialize(decoder: Decoder): CardProtocol {
		val string = decoder.decodeString()
		CardProtocol.Grouped.entries.find { it.name == string }?.let {
			return it
		}
		CardProtocol.Technical.entries.find { it.name == string }?.let {
			return it
		}
		// not matched yet
		require(ANY_CONSTANT == string) { "Value does not match the required constant" }
		return CardProtocol.Any
	}
}
