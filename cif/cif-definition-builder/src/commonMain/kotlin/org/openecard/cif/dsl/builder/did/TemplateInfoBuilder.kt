package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.cardcall.TemplateApduCallDefinition
import org.openecard.cif.definition.did.SignatureGenerationInfo
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.cardcall.TemplateApduCallScope
import org.openecard.cif.dsl.api.did.SignatureGenerationInfoScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.cardcall.TemplateApduCallBuilder

class TemplateInfoBuilder :
	SignatureGenerationInfoScope.TemplateInfoScope,
	Builder<SignatureGenerationInfo.TemplateInfo> {
	val info: MutableList<TemplateApduCallDefinition> = mutableListOf()

	override fun add(content: @CifMarker (TemplateApduCallScope.() -> Unit)) {
		val builder = TemplateApduCallBuilder()
		content(builder)
		info.add(builder.build())
	}

	override fun build(): SignatureGenerationInfo.TemplateInfo =
		SignatureGenerationInfo.TemplateInfo(
			info,
		)
}
