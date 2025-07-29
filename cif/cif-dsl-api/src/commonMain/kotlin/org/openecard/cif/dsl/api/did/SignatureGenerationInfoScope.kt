package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.HashGenerationInfoType
import org.openecard.cif.definition.did.SignatureGenerationInfoType
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope
import org.openecard.cif.dsl.api.cardcall.TemplateApduCallScope
import org.openecard.utils.serialization.PrintableUByteArray

interface SignatureGenerationInfoScope : CifScope {
	fun template(content: @CifMarker TemplateInfoScope.() -> Unit)

	fun standard(content: @CifMarker StandardInfoScope.() -> Unit)

	interface StandardInfoScope : CifScope {
		var hashGenInfo: HashGenerationInfoType?
		var cardAlgRef: PrintableUByteArray?
		var hashAlgRef: PrintableUByteArray?

		fun info(vararg info: SignatureGenerationInfoType)
	}

	interface TemplateInfoScope : CifScope {
		fun add(content: @CifMarker TemplateApduCallScope.() -> Unit)
	}
}
