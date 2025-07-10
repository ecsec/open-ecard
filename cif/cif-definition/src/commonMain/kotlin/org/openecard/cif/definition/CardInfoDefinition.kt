package org.openecard.cif.definition

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.meta.CardInfoMetadata

@Serializable
class CardInfoDefinition(
	val metadata: CardInfoMetadata,
	// TODO: CardIdentification
	// TODO: CardCapabilities
	val applications: Set<ApplicationDefinition>,
)
