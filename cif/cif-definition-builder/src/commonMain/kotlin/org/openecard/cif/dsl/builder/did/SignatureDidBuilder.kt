package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.NeverAcl
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.cif.definition.did.SignatureDidParameters
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.SignatureDidParametersScope
import org.openecard.cif.dsl.api.did.SignatureDidScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.acl.AclBuilder

class SignatureDidBuilder :
	DidBuilder<GenericCryptoDidDefinition.SignatureDidDefinition>(),
	SignatureDidScope,
	Builder<GenericCryptoDidDefinition.SignatureDidDefinition> {
	var signAcl: AclDefinition = NeverAcl
	var parameters: SignatureDidParameters? = null

	override fun signAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		signAcl = builder.build()
	}

	override fun parameters(content: @CifMarker (SignatureDidParametersScope.() -> Unit)) {
		val builder = SignatureDidParametersBuilder()
		content(builder)
		parameters = builder.build()
	}

	override fun build(): GenericCryptoDidDefinition.SignatureDidDefinition =
		GenericCryptoDidDefinition.SignatureDidDefinition(
			name = name,
			scope = scope,
			signAcl = signAcl,
			parameters = requireNotNull(parameters),
		)
}
