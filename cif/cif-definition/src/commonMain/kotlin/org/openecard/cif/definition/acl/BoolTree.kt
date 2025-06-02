package org.openecard.cif.definition.acl

import kotlinx.serialization.Serializable

@Serializable
data class BoolTreeOr<T>(
	val or: List<BoolTreeAnd<T>>,
)

@Serializable
data class BoolTreeAnd<T>(
	val and: List<T>,
)

sealed interface BoolTreeLeaf {
	object False : BoolTreeLeaf

	object True : BoolTreeLeaf
}
