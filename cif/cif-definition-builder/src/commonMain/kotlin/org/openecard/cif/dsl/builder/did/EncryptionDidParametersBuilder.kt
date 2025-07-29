package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.EncryptionDidParameters
import org.openecard.cif.definition.did.KeyRefDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.did.EncryptionDidParametersScope
import org.openecard.cif.dsl.api.did.KeyRefScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.utils.serialization.PrintableUByteArray

class EncryptionDidParametersBuilder :
	EncryptionDidParametersScope,
	Builder<EncryptionDidParameters> {
	private var _encryptionAlgorithm: String? = null
	override var encryptionAlgorithm: String
		get() = requireNotNull(_encryptionAlgorithm)
		set(value) {
			_encryptionAlgorithm = value
		}
	override var cardAlgRef: PrintableUByteArray? = null
	override var hashAlgRef: PrintableUByteArray? = null
	var certificates: List<String> = listOf()
	var key: KeyRefDefinition? = null

	override fun certificates(vararg certificate: String) {
		certificates = certificates.toList()
	}

	override fun key(content: @CifMarker (KeyRefScope.() -> Unit)) {
		val builder = KeyRefBuilder()
		content(builder)
		key = builder.build()
	}

	override fun build(): EncryptionDidParameters =
		EncryptionDidParameters(
			key = requireNotNull(key),
			certificates = certificates,
			encryptionAlgorithm = encryptionAlgorithm,
			hashAlgRef = hashAlgRef,
			cardAlgRef = cardAlgRef,
		)
}
