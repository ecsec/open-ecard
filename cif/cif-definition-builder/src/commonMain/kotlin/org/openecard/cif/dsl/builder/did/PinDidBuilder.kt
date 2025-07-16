package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.NeverAcl
import org.openecard.cif.definition.did.PinDidDefinition
import org.openecard.cif.definition.did.PinDidParameters
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.DidDslScope
import org.openecard.cif.dsl.api.did.PinDidParametersScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.acl.AclBuilder

class PinDidBuilder :
	DidBuilder<PinDidDefinition>(),
	DidDslScope.Pin,
	Builder<PinDidDefinition> {
	var authAcl: AclDefinition = NeverAcl
	var modifyAcl: AclDefinition = NeverAcl
	var resetAcl: AclDefinition = NeverAcl
	var parameters: PinDidParameters? = null

	override fun authAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		this.authAcl = builder.build()
	}

	override fun modifyAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		this.modifyAcl = builder.build()
	}

	override fun resetAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		this.resetAcl = builder.build()
	}

	override fun parameters(content: @CifMarker (PinDidParametersScope.() -> Unit)) {
		val builder = PinDidParametersBuilder()
		content(builder)
		this.parameters = builder.build()
	}

	override fun build(): PinDidDefinition =
		PinDidDefinition(
			name = name,
			scope = scope,
			authAcl = authAcl,
			modifyAcl = modifyAcl,
			resetAcl = resetAcl,
			parameters = requireNotNull(parameters),
		)
}
