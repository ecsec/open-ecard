package org.openecard.cif.definition.cardcall

import org.openecard.utils.serialization.PrintableUByteArray

sealed class DataMaskDefinition {
	data class MatchingData(
		val matchingValue: PrintableUByteArray,
		val offset: UByte?,
		val length: UByte?,
		val mask: PrintableUByteArray?,
		val rule: MatchRule,
	) : DataMaskDefinition()

	data class DataObject(
		val tag: UByte,
		val match: DataMaskDefinition,
	) : DataMaskDefinition()
}
