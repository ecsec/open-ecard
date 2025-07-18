package org.openecard.cif.dsl.api.capabilities

import org.openecard.cif.dsl.api.CifScope

interface DataCodingScope : CifScope {
	var tlvEfs: Boolean
	var writeOneTime: Boolean
	var writeProprietary: Boolean
	var writeOr: Boolean
	var writeAnd: Boolean
	var ffValidAsTlvFirstByte: Boolean
	var dataUnitsQuartets: Int
}
