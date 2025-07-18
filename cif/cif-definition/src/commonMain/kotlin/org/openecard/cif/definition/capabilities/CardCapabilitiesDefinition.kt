package org.openecard.cif.definition.capabilities

import kotlinx.serialization.Serializable

@Serializable
data class CardCapabilitiesDefinition(
	val selectionMethods: SelectionMethodsDefinition,
	val dataCoding: DataCodingDefinitions?,
	val commandCoding: CommandCodingDefinitions?,
)
