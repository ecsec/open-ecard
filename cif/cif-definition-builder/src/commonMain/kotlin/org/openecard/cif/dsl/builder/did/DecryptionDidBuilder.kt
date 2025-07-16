package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.NeverAcl
import org.openecard.cif.definition.did.EncryptionDidParameters
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.DecryptionDidScope
import org.openecard.cif.dsl.api.did.EncryptionDidParametersScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.acl.AclBuilder
import kotlin.coroutines.EmptyCoroutineContext.get

class DecryptionDidBuilder :
	DidBuilder<GenericCryptoDidDefinition.DecryptionDidDefinition>(),
	DecryptionDidScope,
	Builder<GenericCryptoDidDefinition.DecryptionDidDefinition> {
	var decipherAcl: AclDefinition = NeverAcl
	var parameters: EncryptionDidParameters? = null

	override fun decipherAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		decipherAcl = builder.build()
	}

	override fun parameters(content: @CifMarker (EncryptionDidParametersScope.() -> Unit)) {
		val builder = EncryptionDidParametersBuilder()
		content(builder)
		parameters = builder.build()
	}

	override fun build(): GenericCryptoDidDefinition.DecryptionDidDefinition =
		GenericCryptoDidDefinition.DecryptionDidDefinition(
			name = name,
			scope = scope,
			decipherAcl = decipherAcl,
			parameters = requireNotNull(parameters),
		)
}
