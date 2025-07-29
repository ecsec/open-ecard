package org.openecard.cif.dsl.builder.cardcall

import org.openecard.cif.definition.cardcall.ApduTemplateValue
import org.openecard.cif.definition.cardcall.TemplateApduCallDefinition
import org.openecard.cif.dsl.api.cardcall.TemplateApduCallScope
import org.openecard.cif.dsl.builder.Builder

class TemplateApduCallBuilder :
	TemplateApduCallScope,
	Builder<TemplateApduCallDefinition> {
	private var _header: ApduTemplateValue? = null
	override var header: String
		get() = requireNotNull(_header).template
		set(value) {
			_header = ApduTemplateValue(value)
		}
	private var _data: ApduTemplateValue? = null
	override var data: String?
		get() = _data?.template
		set(value) {
			_data = value?.let { ApduTemplateValue(it) }
		}
	override var expectedLength: ULong? = null

	override fun build(): TemplateApduCallDefinition =
		TemplateApduCallDefinition(
			header = requireNotNull(_header),
			data = requireNotNull(_data),
			expectedLength = expectedLength,
		)
}
