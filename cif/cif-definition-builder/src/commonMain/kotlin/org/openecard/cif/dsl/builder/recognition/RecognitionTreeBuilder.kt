package org.openecard.cif.dsl.builder.recognition

import org.openecard.cif.definition.recognition.ApduCallDefinition
import org.openecard.cif.definition.recognition.RecognitionTree
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.recognition.ApduCardCallScope
import org.openecard.cif.dsl.api.recognition.RecognitionTreeScope
import org.openecard.cif.dsl.builder.Builder

class RecognitionTreeBuilder(
	private val calls: MutableList<ApduCallDefinition> = mutableListOf(),
) : RecognitionTreeScope,
	Builder<List<ApduCallDefinition>> {
	override fun call(content: @CifMarker (ApduCardCallScope.() -> Unit)) {
		val builder = ApduCardCallScopeBuilder()
		content.invoke(builder)
		calls.add(builder.build())
	}

	override fun build(): RecognitionTree = calls.toList()
}
