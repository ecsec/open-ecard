package org.openecard.cif.definition.capabilities

import kotlinx.serialization.Serializable

@Serializable
data class SelectionMethodsDefinition(
	val selectDfByFullName: Boolean,
	val selectDfByPartialName: Boolean,
	val selectDfByPath: Boolean,
	val selectDfByFileId: Boolean,
	val selectDfImplicit: Boolean,
	val supportsShortEf: Boolean,
	val supportsRecordNumber: Boolean,
	val supportsRecordIdentifier: Boolean,
)
