package org.openecard.cif.dsl.api.did

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.DidDslScope.GenericCrypto

interface EncryptionDidScope : GenericCrypto<EncryptionDidParametersScope> {
	fun encipherAcl(content: @CifMarker AclScope.() -> Unit)

	fun decipherAcl(content: @CifMarker AclScope.() -> Unit)
}
