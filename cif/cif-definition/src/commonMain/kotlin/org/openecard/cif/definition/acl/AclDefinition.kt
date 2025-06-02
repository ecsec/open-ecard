package org.openecard.cif.definition.acl

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.CardProtocol

@Serializable
data class AclDefinition(
	val acls: Map<CardProtocol, BoolTreeOr<BoolTreeLeaf>>,
)
