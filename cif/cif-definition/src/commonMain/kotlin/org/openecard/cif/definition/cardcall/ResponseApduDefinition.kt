package org.openecard.cif.definition.cardcall

data class ResponseApduDefinition(
	val body: DataMaskDefinition?,
	val trailer: UShort,
	val conclusion: ConclusionDefinition?,
)
