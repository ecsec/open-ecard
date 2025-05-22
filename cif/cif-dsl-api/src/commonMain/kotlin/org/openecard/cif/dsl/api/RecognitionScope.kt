package org.openecard.cif.dsl.api

interface RecognitionScope : CifScope {
	fun call(content: @CifMarker ApduCardCallScope.() -> Unit)
}
