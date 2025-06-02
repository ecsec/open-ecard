package org.openecard.cif.definition.acl

import kotlinx.serialization.Serializable

@Serializable
data class DidStateReference(
	val name: String,
	val active: Boolean,
	/**
	 * State qualifier.
	 */
	val stateQualifier: AclQualifier?,
) : BoolTreeLeaf
