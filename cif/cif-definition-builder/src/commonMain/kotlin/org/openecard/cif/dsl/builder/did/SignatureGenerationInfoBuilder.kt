package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.SignatureGenerationInfo
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.did.SignatureGenerationInfoScope
import org.openecard.cif.dsl.builder.Builder

class SignatureGenerationInfoBuilder :
	SignatureGenerationInfoScope,
	Builder<SignatureGenerationInfo> {
	var signatureGenerationInfo: SignatureGenerationInfo? = null

	override fun template(content: @CifMarker (SignatureGenerationInfoScope.TemplateInfoScope.() -> Unit)) {
		val builder = TemplateInfoBuilder()
		content(builder)
		signatureGenerationInfo = builder.build()
	}

	override fun standard(content: @CifMarker (SignatureGenerationInfoScope.StandardInfoScope.() -> Unit)) {
		val builder = StandardInfoBuilder()
		content(builder)
		signatureGenerationInfo = builder.build()
	}

	override fun build(): SignatureGenerationInfo = requireNotNull(signatureGenerationInfo)
}
