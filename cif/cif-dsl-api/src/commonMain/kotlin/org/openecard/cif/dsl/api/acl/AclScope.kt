package org.openecard.cif.dsl.api.acl

import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.dsl.api.CifScope

interface AclScope : CifScope {
	fun acl(
		protocol: CardProtocol,
		content: @AclTreeMarker AclBoolTreeBuilder.() -> CifAclOr,
	)
}
