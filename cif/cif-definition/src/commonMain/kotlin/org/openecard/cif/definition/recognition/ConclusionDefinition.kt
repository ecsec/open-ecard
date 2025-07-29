package org.openecard.cif.definition.recognition

sealed interface ConclusionDefinition {
	data class RecognizedCardType(
		val name: String,
	) : ConclusionDefinition

	data class Call(
		val call: ApduCallDefinition,
	) : ConclusionDefinition
}
