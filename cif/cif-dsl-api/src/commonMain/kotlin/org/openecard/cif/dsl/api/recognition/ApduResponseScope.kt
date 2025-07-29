package org.openecard.cif.dsl.api.recognition

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface ApduResponseScope : CifScope {
	/**
	 * Required
	 */
	var trailer: UShort

	/**
	 * Optional
	 */
	fun body(
		tag: ULong,
		content: @CifMarker ResponseDataMaskScope.() -> Unit,
	)

	fun body(content: @CifMarker MatchingDataScope.() -> Unit)

	fun recognizedCardType(name: String)

	fun call(content: @CifMarker ApduCardCallScope.() -> Unit)
}
