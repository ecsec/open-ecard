package org.openecard.cif.dsl.api.did

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface GenericCryptoDidParametersScope : CifScope {
	fun certificates(vararg certificate: String)

	fun key(content: @CifMarker KeyRefScope.() -> Unit)
}
