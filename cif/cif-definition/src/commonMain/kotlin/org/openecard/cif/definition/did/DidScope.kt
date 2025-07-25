package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable

@Serializable
enum class DidScope {
	LOCAL,
	GLOBAL,
}
