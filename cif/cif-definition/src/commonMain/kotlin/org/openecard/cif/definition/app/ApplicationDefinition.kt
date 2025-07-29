package org.openecard.cif.definition.app

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.dataset.DataSetDefinition
import org.openecard.cif.definition.did.DidDefinition
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
data class ApplicationDefinition(
	/**
	 * Application identifier.
	 * Might be a root based path as well (see ISO 7816-4 for more details).
	 */
	val aid: PrintableUByteArray,
	/**
	 * Human-readable name of the application.
	 */
	val name: String,
	/**
	 * Description and other information of this application.
	 */
	val description: String?,
	val selectAcl: AclDefinition,
	val dids: Set<DidDefinition>,
	val dataSets: Set<DataSetDefinition>,
)
