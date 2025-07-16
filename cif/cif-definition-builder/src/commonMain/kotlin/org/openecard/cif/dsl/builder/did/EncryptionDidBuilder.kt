package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.NeverAcl
import org.openecard.cif.definition.did.EncryptionDidParameters
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.EncryptionDidParametersScope
import org.openecard.cif.dsl.api.did.EncryptionDidScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.acl.AclBuilder
import kotlin.coroutines.EmptyCoroutineContext.get

class EncryptionDidBuilder :
	DidBuilder<GenericCryptoDidDefinition.EncryptionDidDefinition>(),
	EncryptionDidScope,
	Builder<GenericCryptoDidDefinition.EncryptionDidDefinition> {
	var encipherAcl: AclDefinition = NeverAcl
	var parameters: EncryptionDidParameters? = null

	override fun encipherAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		encipherAcl = builder.build()
	}

	override fun parameters(content: @CifMarker (EncryptionDidParametersScope.() -> Unit)) {
		val builder = EncryptionDidParametersBuilder()
		content(builder)
		parameters = builder.build()
	}

	override fun build(): GenericCryptoDidDefinition.EncryptionDidDefinition =
		GenericCryptoDidDefinition.EncryptionDidDefinition(
			name = name,
			scope = scope,
			encipherAcl = encipherAcl,
			parameters = requireNotNull(parameters),
		)
}
