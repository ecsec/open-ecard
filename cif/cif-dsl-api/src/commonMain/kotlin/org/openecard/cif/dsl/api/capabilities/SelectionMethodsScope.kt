package org.openecard.cif.dsl.api.capabilities

import org.openecard.cif.dsl.api.CifScope

interface SelectionMethodsScope : CifScope {
	var selectDfByFullName: Boolean
	var selectDfByPartialName: Boolean
	var selectDfByPath: Boolean
	var selectDfByFileId: Boolean
	var selectDfImplicit: Boolean
	var supportsShortEf: Boolean
	var supportsRecordNumber: Boolean
	var supportsRecordIdentifier: Boolean
}
