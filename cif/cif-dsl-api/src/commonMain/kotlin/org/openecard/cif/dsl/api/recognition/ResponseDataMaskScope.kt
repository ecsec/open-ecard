package org.openecard.cif.dsl.api.recognition

import org.openecard.cif.definition.recognition.DataMaskDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface ResponseDataMaskScope : CifScope {
	fun matchBytes(content: @CifMarker MatchingDataScope.() -> Unit): DataMaskDefinition

	fun matchData(
		tag: ULong,
		content: @CifMarker ResponseDataMaskScope.() -> DataMaskDefinition,
	): DataMaskDefinition.DataObject
}
