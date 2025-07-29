package org.openecard.cif.dsl.api.did

import org.openecard.cif.dsl.api.CifScope

interface KeyRefScope : CifScope {
	var keyRef: UByte
	var keySize: Int?
	var nonceSize: Int?
}
