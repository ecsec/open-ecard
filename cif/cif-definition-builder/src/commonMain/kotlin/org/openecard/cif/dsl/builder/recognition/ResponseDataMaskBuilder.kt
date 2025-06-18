package org.openecard.cif.dsl.builder.recognition

import org.openecard.cif.definition.recognition.DataMaskDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.recognition.MatchingDataScope
import org.openecard.cif.dsl.api.recognition.ResponseDataMaskScope
import org.openecard.cif.dsl.builder.Builder

class ResponseDataMaskBuilder(
	var tag: ULong,
) : ResponseDataMaskScope,
	Builder<DataMaskDefinition.DataObject> {
	var match: DataMaskDefinition? = null
		set(value) {
			if (built != null) {
				throw IllegalStateException("Cannot update the builder after it builds!")
			}
			field = value
		}
	var built: DataMaskDefinition.DataObject? = null

	override fun matchBytes(content: @CifMarker (MatchingDataScope.() -> Unit)): DataMaskDefinition {
		val builder = MatchingDataBuilder()
		content.invoke(builder)
		match = builder.build()
		return build()
	}

	override fun matchData(
		tag: ULong,
		content: @CifMarker (ResponseDataMaskScope.() -> DataMaskDefinition),
	): DataMaskDefinition.DataObject {
		val builder = ResponseDataMaskBuilder(tag)
		content.invoke(builder)
		match = builder.build()
		return build()
	}

	override fun build(): DataMaskDefinition.DataObject {
		if (built == null) {
			built =
				DataMaskDefinition.DataObject(
					tag = tag,
					match = match!!,
				)
		}
		return built!!
	}
}
