package org.openecard.cif.dsl.builder.recognition

import org.openecard.cif.definition.recognition.DataMaskDefinition
import org.openecard.cif.definition.recognition.MatchRule
import org.openecard.cif.dsl.api.recognition.MatchingDataScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.utils.serialization.toPrintable

@OptIn(ExperimentalUnsignedTypes::class)
class MatchingDataBuilder :
	MatchingDataScope,
	Builder<DataMaskDefinition.MatchingData> {
	private var _value: UByteArray? = null
	override var value: UByteArray
		get() = _value!!
		set(value) {
			_value = value
		}
	override var offset: UInt = 0u
	override var length: UInt? = null
	override var mask: UByteArray? = null
	override var rule: MatchRule? = null

	override fun build(): DataMaskDefinition.MatchingData {
		val (v, r) =
			if (length != null && _value == null && rule == null) {
				// we only want to match the length, not the content
				ubyteArrayOf() to MatchRule.Contains
			} else {
				value to rule
			}

		return DataMaskDefinition.MatchingData(
			matchingValue = v.toPrintable(),
			offset = offset,
			length = length,
			mask =
				mask?.let {
					check(it.size == value.size) { "Mask has a different size than the comparison value" }
					it.toPrintable()
				},
			rule = r ?: MatchRule.Equals,
		)
	}
}
