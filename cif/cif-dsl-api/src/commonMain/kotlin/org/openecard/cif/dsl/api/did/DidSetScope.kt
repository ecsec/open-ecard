package org.openecard.cif.dsl.api.did

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface DidSetScope : CifScope {
	fun pace(content: @CifMarker DidDslScope.Pace.() -> Unit)

	fun pin(content: @CifMarker DidDslScope.Pin.() -> Unit)
}
