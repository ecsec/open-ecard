package org.openecard.cif.dsl.api.did

import org.openecard.cif.dsl.api.CifMarker

interface SignatureDidParametersScope : GenericCryptoDidParametersScope {
	var signatureAlgorithm: String

	fun sigGen(content: @CifMarker SignatureGenerationInfoScope.() -> Unit)
}
