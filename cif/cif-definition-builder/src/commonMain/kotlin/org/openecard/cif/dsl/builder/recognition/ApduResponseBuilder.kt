package org.openecard.cif.dsl.builder.recognition

import org.openecard.cif.definition.recognition.ConclusionDefinition
import org.openecard.cif.definition.recognition.DataMaskDefinition
import org.openecard.cif.definition.recognition.ResponseApduDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.recognition.ApduCardCallScope
import org.openecard.cif.dsl.api.recognition.ApduResponseScope
import org.openecard.cif.dsl.api.recognition.MatchingDataScope
import org.openecard.cif.dsl.api.recognition.ResponseDataMaskScope
import org.openecard.cif.dsl.builder.Builder

class ApduResponseBuilder :
	ApduResponseScope,
	Builder<ResponseApduDefinition> {
	var body: DataMaskDefinition? = null
	var conclusion: ConclusionDefinition? = null

	override var trailer: UShort = 0x9000u

	override fun body(
		tag: ULong,
		content: @CifMarker (ResponseDataMaskScope.() -> Unit),
	) {
		val builder = ResponseDataMaskBuilder(tag)
		content.invoke(builder)
		body = builder.build()
	}

	override fun body(content: @CifMarker (MatchingDataScope.() -> Unit)) {
		val builder = MatchingDataBuilder()
		content.invoke(builder)
		body = builder.build()
	}

	override fun recognizedCardType(name: String) {
		conclusion = ConclusionDefinition.RecognizedCardType(name)
	}

	override fun call(content: @CifMarker (ApduCardCallScope.() -> Unit)) {
		val builder = ApduCardCallScopeBuilder()
		content.invoke(builder)
		conclusion = ConclusionDefinition.Call(builder.build())
	}

	override fun build(): ResponseApduDefinition =
		ResponseApduDefinition(
			body = body,
			trailer = trailer,
			conclusion = requireNotNull(conclusion),
		)
}
