package org.openecard.cif.dsl.builder.acl

import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.dsl.api.acl.AclBoolTreeBuilder
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.acl.AclTreeMarker
import org.openecard.cif.dsl.builder.Builder

class AclBuilder(
	val definitions: MutableMap<CardProtocol, CifAclOr> = mutableMapOf(),
) : AclScope,
	Builder<AclDefinition> {
	override fun acl(
		protocol: CardProtocol,
		content: @AclTreeMarker (AclBoolTreeBuilder.() -> CifAclOr),
	) {
		definitions[protocol] = content(AclBoolTreeBuilder)
	}

	override fun build(): AclDefinition = AclDefinition(acls = definitions)
}
