package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.KeyRefDefinition
import org.openecard.cif.definition.did.SignatureDidParameters
import org.openecard.cif.definition.did.SignatureGenerationInfo
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.did.KeyRefScope
import org.openecard.cif.dsl.api.did.SignatureDidParametersScope
import org.openecard.cif.dsl.api.did.SignatureGenerationInfoScope
import org.openecard.cif.dsl.builder.Builder

class SignatureDidParametersBuilder :
	SignatureDidParametersScope,
	Builder<SignatureDidParameters> {
	private var _signatureAlgorithm: String? = null
	override var signatureAlgorithm: String
		get() = requireNotNull(_signatureAlgorithm)
		set(value) {
			_signatureAlgorithm = value
		}
	var certificates: List<String> = listOf()
	var key: KeyRefDefinition? = null
	var sigGen: SignatureGenerationInfo? = null

	override fun sigGen(content: @CifMarker (SignatureGenerationInfoScope.() -> Unit)) {
		val builder = SignatureGenerationInfoBuilder()
		content(builder)
		sigGen = builder.build()
	}

	override fun certificates(vararg certificate: String) {
		certificates = certificate.toList()
	}

	override fun key(content: @CifMarker (KeyRefScope.() -> Unit)) {
		val builder = KeyRefBuilder()
		content(builder)
		key = builder.build()
	}

	override fun build(): SignatureDidParameters =
		SignatureDidParameters(
			key = requireNotNull(key),
			sigGen = requireNotNull(sigGen),
			signatureAlgorithm = signatureAlgorithm,
			certificates = certificates,
		)
}
