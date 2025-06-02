package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable

// TODO: think about how to solve the translation (name) and if it is necessary at all
@Serializable
sealed interface DidDefinition {
	val name: String
	val scope: DidScope
}
