package org.openecard.cif.dsl.api.recognition

import org.openecard.cif.dsl.api.CifMarker

interface ApduCardCallScope {
	@OptIn(ExperimentalUnsignedTypes::class)
	var command: UByteArray

	/**
	 * Define an additional response. Can be called multiple times.
	 */
	fun response(content: @CifMarker ApduResponseScope.() -> Unit)
}
