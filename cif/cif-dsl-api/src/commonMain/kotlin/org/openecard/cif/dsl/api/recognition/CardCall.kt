package org.openecard.cif.dsl.api.recognition

import org.openecard.cif.definition.recognition.DataMaskDefinition
import org.openecard.cif.definition.recognition.MatchRule
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface ApduCardCallScope {
	@OptIn(ExperimentalUnsignedTypes::class)
	var command: UByteArray

	/**
	 * Define an additional response. Can be called multiple times.
	 */
	fun response(content: @CifMarker ApduResponseScope.() -> Unit)
}

interface ApduResponseScope : CifScope {
	/**
	 * Required
	 */
	var trailer: UShort

	/**
	 * Optional
	 */
	fun body(
		tag: UByte,
		content: @CifMarker ResponseDataMaskScope.() -> Unit,
	)

	fun body(content: @CifMarker MatchingDataScope.() -> Unit)

	fun recognizedCardType(name: String)

	fun call(content: @CifMarker ApduCardCallScope.() -> Unit)
}

interface ResponseDataMaskScope : CifScope {
	fun matchBytes(content: @CifMarker MatchingDataScope.() -> Unit): DataMaskDefinition

	fun matchData(
		tag: UByte,
		content: @CifMarker ResponseDataMaskScope.() -> DataMaskDefinition,
	): DataMaskDefinition.DataObject
}

interface MatchingDataScope : CifScope {
	@OptIn(ExperimentalUnsignedTypes::class)
	var value: UByteArray
	var offset: UInt
	var length: UInt?

	@OptIn(ExperimentalUnsignedTypes::class)
	var mask: UByteArray?
	var rule: MatchRule?
}
