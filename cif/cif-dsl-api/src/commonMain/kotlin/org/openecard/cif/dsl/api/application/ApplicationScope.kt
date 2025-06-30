package org.openecard.cif.dsl.api.application

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope
import org.openecard.cif.dsl.api.CifSetScope
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.dataset.DataSetScope
import org.openecard.cif.dsl.api.did.DidSetScope
import org.openecard.utils.serialization.PrintableUByteArray

interface ApplicationScope : CifScope {
	/**
	 * Application identifier.
	 * Might be a root based path as well (see ISO 7816-4 for more details).
	 */
	var aid: PrintableUByteArray

	/**
	 * Human-readable name of the application.
	 */
	var name: String

	/**
	 * Description and other information of this application.
	 */
	var description: String?

	fun selectAcl(content: @CifMarker AclScope.() -> Unit)

	fun dids(content: @CifMarker DidSetScope.() -> Unit)

	fun dataSets(content: @CifMarker CifSetScope<DataSetScope>.() -> Unit)
}
