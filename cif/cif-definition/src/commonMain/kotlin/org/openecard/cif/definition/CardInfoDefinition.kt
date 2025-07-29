package org.openecard.cif.definition

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.capabilities.CardCapabilitiesDefinition
import org.openecard.cif.definition.meta.CardInfoMetadata

@Serializable
class CardInfoDefinition(
	val metadata: CardInfoMetadata,
	// TODO: CardIdentification
	val capabilities: CardCapabilitiesDefinition?,
	val applications: Set<ApplicationDefinition>,
)
