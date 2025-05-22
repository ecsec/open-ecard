package org.openecard.cif.definition.cardcall

sealed interface ConclusionDefinition {
	data class NamedState(
		val name: String,
	) : ConclusionDefinition

	data class RecognizedCardType(
		val name: String,
	) : ConclusionDefinition

	data class Call(
		val call: CardCallDefinition,
	) : ConclusionDefinition
}
