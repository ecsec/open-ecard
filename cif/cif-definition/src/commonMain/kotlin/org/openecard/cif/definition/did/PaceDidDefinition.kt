package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition

@Serializable
data class PaceDidDefinition(
	override val name: String,
	override val scope: DidScope,
	val authAcl: AclDefinition,
	val modifyAcl: AclDefinition,
	val parameters: PaceDidParameters,
) : DidDefinition

@Serializable
data class PaceDidParameters(
	val passwordRef: PacePinId,
	val minLength: Int,
	val maxLength: Int?,
)

enum class PacePinId {
	MRZ,
	CAN,
	PIN,
	PUK,
}
