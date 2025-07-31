package org.openecard.cif.definition.dataset

import kotlinx.serialization.Serializable

@Serializable
enum class DatasetType {
	TRANSPARENT,
	RECORD,
	RING,
	DATA_OBJECT,
}
