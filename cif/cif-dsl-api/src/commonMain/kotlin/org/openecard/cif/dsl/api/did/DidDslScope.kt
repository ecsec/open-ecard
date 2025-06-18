package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.dsl.api.CifScope

interface DidDslScope : CifScope {
	var name: String
	var scope: DidScope

	interface Decryption : DidDslScope

	interface Encryption : DidDslScope

	interface Signature : DidDslScope

	interface Pace : DidDslScope

	interface Pin : DidDslScope
}
