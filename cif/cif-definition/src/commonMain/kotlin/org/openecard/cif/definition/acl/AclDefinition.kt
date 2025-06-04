package org.openecard.cif.definition.acl

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.CardProtocol

typealias CifAclOr = BoolTreeOr<BoolTreeLeaf>

@Serializable
data class AclDefinition(
	/**
	 * Contains definitions of the ACLs which are applicable to different protocols the card may use.
	 * Only one of the provided ACLs should be used.
	 * At runtime, the most specific should be selected.
	 */
	val acls: Map<CardProtocol, CifAclOr>,
)

val NeverTree = CifAclOr(listOf(BoolTreeAnd(listOf(BoolTreeLeaf.False))))
val AlwaysTree = CifAclOr(listOf(BoolTreeAnd(listOf(BoolTreeLeaf.True))))
