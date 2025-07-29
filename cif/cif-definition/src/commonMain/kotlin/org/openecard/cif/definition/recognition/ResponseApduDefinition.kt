package org.openecard.cif.definition.recognition

data class ResponseApduDefinition(
	val body: DataMaskDefinition?,
	val trailer: UShort,
	val conclusion: ConclusionDefinition,
)
