package org.openecard.cif.dsl.api.recognition

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface RecognitionScope : CifScope {
	fun call(content: @CifMarker ApduCardCallScope.() -> Unit)
}
