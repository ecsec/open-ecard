package org.openecard.cif.dsl.api.did

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.DidDslScope.GenericCrypto

interface DecryptionDidScope : GenericCrypto<EncryptionDidParametersScope> {
	fun decipherAcl(content: @CifMarker AclScope.() -> Unit)
}
