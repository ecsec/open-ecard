package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.NeverAcl
import org.openecard.cif.definition.did.PaceDidDefinition
import org.openecard.cif.definition.did.PaceDidParameters
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.DidDslScope
import org.openecard.cif.dsl.api.did.PaceDidParametersScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.acl.AclBuilder

class PaceDidBuilder :
	DidBuilder<PaceDidDefinition>(),
	DidDslScope.Pace,
	Builder<PaceDidDefinition> {
	var authAcl: AclDefinition = NeverAcl
	var modifyAcl: AclDefinition = NeverAcl
	var parameters: PaceDidParameters? = null

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

	override fun parameters(content: @CifMarker (PaceDidParametersScope.() -> Unit)) {
		val builder = PaceDidParametersBuilder()
		content(builder)
		this.parameters = builder.build()
	}

	override fun build(): PaceDidDefinition =
		PaceDidDefinition(
			name = name,
			scope = scope,
			authAcl = authAcl,
			modifyAcl = modifyAcl,
			parameters = requireNotNull(parameters),
		)
}
