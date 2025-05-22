package org.openecard.cif.dsl.api

import org.openecard.cif.definition.cardcall.DataMaskDefinition
import org.openecard.cif.definition.cardcall.MatchRule

interface ApduCardCallScope {
	@OptIn(ExperimentalUnsignedTypes::class)
	var command: UByteArray

	/**
	 * Define an additional response. Can be called multiple times.
	 */
	fun response(content: @CifMarker ApduResponseScope.() -> Unit)
}

object Api {
	interface ApiCall

	interface ApiResponse
}

interface ApiCardCallScope {
	fun call(content: @CifMarker () -> Api.ApiCall)

	fun responses(content: @CifMarker ApiResponsesScope.() -> Unit)
}

interface ApiCallScope

interface ApiResponseScope

interface ApiResponsesScope {
	fun response(content: @CifMarker CifScope.() -> Api.ApiResponse)
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

	/**
	 * Optional
	 */
	fun conclusion(content: @CifMarker ConclusionScope.() -> Unit)
}

interface ResponseDataMaskScope : CifScope {
	fun matchBytes(content: @CifMarker MatchingDataScope.() -> Unit): DataMaskDefinition

	fun matchData(
		tag: UByte,
		content: @CifMarker ResponseDataMaskScope.() -> DataMaskDefinition,
	): DataMaskDefinition.DataObject
}

interface ApduResponsesScope : CifScope {
	fun response(content: @CifMarker ApduResponseScope.() -> Unit)
}

interface TrailerScope : CifScope

interface MatchingDataScope : CifScope {
	@OptIn(ExperimentalUnsignedTypes::class)
	var value: UByteArray
	var offset: UByte?
	var length: UByte?

	@OptIn(ExperimentalUnsignedTypes::class)
	var mask: UByteArray?
	var rule: MatchRule?
}

interface ConclusionScope : CifScope {
	fun inState(name: String)

	fun recognizedCardType(name: String)

	fun call(content: @CifMarker ApduCardCallScope.() -> Unit)
}
