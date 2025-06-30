package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.HashGenerationInfoType
import org.openecard.cif.definition.did.SignatureGenerationInfo
import org.openecard.cif.definition.did.SignatureGenerationInfoType
import org.openecard.cif.dsl.api.did.SignatureGenerationInfoScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.utils.serialization.PrintableUByteArray

class StandardInfoBuilder :
	SignatureGenerationInfoScope.StandardInfoScope,
	Builder<SignatureGenerationInfo.StandardInfo> {
	override var hashGenInfo: HashGenerationInfoType? = null
	override var cardAlgRef: PrintableUByteArray? = null
	override var hashAlgRef: PrintableUByteArray? = null
	var infoTypes: Set<SignatureGenerationInfoType> = setOf()

	override fun info(vararg info: SignatureGenerationInfoType) {
		infoTypes = info.toSet()
	}

	override fun build(): SignatureGenerationInfo.StandardInfo =
		SignatureGenerationInfo.StandardInfo(
			info = infoTypes,
			hashGenInfo = hashGenInfo,
			cardAlgRef = cardAlgRef,
			hashAlgRef = hashAlgRef,
		)
}
