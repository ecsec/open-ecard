package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope
import org.openecard.cif.dsl.api.acl.AclScope

interface DidDslScope : CifScope {
	var name: String
	var scope: DidScope

	interface Pace : DidDslScope {
		fun authAcl(content: @CifMarker (AclScope.() -> Unit))

		fun modifyAcl(content: @CifMarker (AclScope.() -> Unit))

		fun parameters(content: @CifMarker (PaceDidParametersScope.() -> Unit))
	}

	interface Pin : DidDslScope {
		fun authAcl(content: @CifMarker (AclScope.() -> Unit))

		fun modifyAcl(content: @CifMarker (AclScope.() -> Unit))

		fun resetAcl(content: @CifMarker (AclScope.() -> Unit))

		fun parameters(content: @CifMarker (PinDidParametersScope.() -> Unit))
	}

	interface GenericCrypto<T : GenericCryptoDidParametersScope> : DidDslScope {
		fun parameters(content: @CifMarker T.() -> Unit)
	}
}
