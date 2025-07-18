package org.openecard.cif.dsl.api.capabilities

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface CardCapabilitiesScope : CifScope {
	fun selectionMethods(content: @CifMarker SelectionMethodsScope.() -> Unit)

	fun dataCoding(content: @CifMarker DataCodingScope.() -> Unit)

	fun commandCoding(content: @CifMarker CommandCodingScope.() -> Unit)
}
