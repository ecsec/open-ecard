package org.openecard.cif.definition.capabilities

import kotlinx.serialization.Serializable

@Serializable
data class DataCodingDefinitions(
	val tlvEfs: Boolean,
	val writeOneTime: Boolean,
	val writeProprietary: Boolean,
	val writeOr: Boolean,
	val writeAnd: Boolean,
	val ffValidAsTlvFirstByte: Boolean,
	val dataUnitsQuartets: Int,
)
