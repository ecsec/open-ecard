package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.PacePinId
import org.openecard.cif.dsl.api.CifScope

interface PaceDidParametersScope : CifScope {
	var passwordRef: PacePinId
	var minLength: Int
	var maxLength: Int?
}
