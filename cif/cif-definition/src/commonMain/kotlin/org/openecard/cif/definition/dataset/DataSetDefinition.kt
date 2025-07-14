package org.openecard.cif.definition.dataset

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.utils.serialization.PrintableUByteArray

// TODO: think about how to solve the translation (name) and if it is necessary at all
@Serializable
data class DataSetDefinition(
	val path: PrintableUByteArray,
	val shortEf: UByte?,
	/**
	 * Dataset type, if known.
	 * If this value is not set, the SAL will detect the correct type by itself.
	 */
	val type: DatasetType?,
	val name: String,
	/**
	 * Description and other information of this application.
	 */
	val description: String?,
	val readAcl: AclDefinition,
	val writeAcl: AclDefinition,
)
