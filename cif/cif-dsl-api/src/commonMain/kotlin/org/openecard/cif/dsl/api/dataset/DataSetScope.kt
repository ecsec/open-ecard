package org.openecard.cif.dsl.api.dataset

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.utils.serialization.PrintableUByteArray

interface DataSetScope : CifScope {
	var path: PrintableUByteArray
	var shortEf: UByte?
	var name: String
	var description: String

	fun readAcl(content: @CifMarker AclScope.() -> Unit)

	fun writeAcl(content: @CifMarker AclScope.() -> Unit)
}
