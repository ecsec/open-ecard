package org.openecard.cif.dsl.builder.recognition

import org.openecard.cif.definition.recognition.ApduCallDefinition
import org.openecard.cif.definition.recognition.ResponseApduDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.recognition.ApduCardCallScope
import org.openecard.cif.dsl.api.recognition.ApduResponseScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.utils.serialization.toPrintable

@OptIn(ExperimentalUnsignedTypes::class)
class ApduCardCallScopeBuilder(
	val responses: MutableSet<ResponseApduDefinition> = mutableSetOf(),
) : ApduCardCallScope,
	Builder<ApduCallDefinition> {
	private var _command: UByteArray? = null
	override var command: UByteArray
		get() = requireNotNull(_command)
		set(value) {
			_command = value
		}

	override fun response(content: @CifMarker (ApduResponseScope.() -> Unit)) {
		val builder = ApduResponseBuilder()
		content.invoke(builder)
		responses.add(builder.build())
	}

	override fun build(): ApduCallDefinition =
		ApduCallDefinition(
			requireNotNull(_command?.toPrintable()),
			responses,
		)
}
