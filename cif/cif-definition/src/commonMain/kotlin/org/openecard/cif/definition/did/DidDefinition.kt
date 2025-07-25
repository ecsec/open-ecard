package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable

@Serializable
sealed interface DidDefinition {
	val name: String
	val scope: DidScope
}
