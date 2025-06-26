package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope
import org.openecard.cif.dsl.api.acl.AclScope

interface DidDslScope : CifScope {
	var name: String
	var scope: DidScope

	interface Decryption : DidDslScope

	interface Encryption : DidDslScope

	interface Signature : DidDslScope

	interface Pace : DidDslScope {
		fun authAcl(content: @CifMarker (AclScope.() -> Unit))

		fun modifyAcl(content: @CifMarker (AclScope.() -> Unit))

		fun parameters(content: @CifMarker (PaceDidParametersScope.() -> Unit))
	}

	interface Pin : DidDslScope
}
