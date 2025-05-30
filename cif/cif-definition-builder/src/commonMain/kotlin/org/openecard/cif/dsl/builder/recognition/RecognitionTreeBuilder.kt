package org.openecard.cif.dsl.builder.recognition

import org.openecard.cif.definition.recognition.ApduCallDefinition
import org.openecard.cif.definition.recognition.ConclusionDefinition
import org.openecard.cif.definition.recognition.DataMaskDefinition
import org.openecard.cif.definition.recognition.MatchRule
import org.openecard.cif.definition.recognition.RecognitionTree
import org.openecard.cif.definition.recognition.ResponseApduDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.recognition.ApduCardCallScope
import org.openecard.cif.dsl.api.recognition.ApduResponseScope
import org.openecard.cif.dsl.api.recognition.MatchingDataScope
import org.openecard.cif.dsl.api.recognition.RecognitionScope
import org.openecard.cif.dsl.api.recognition.ResponseDataMaskScope
import org.openecard.utils.serialization.toPrintable

interface Builder<T> {
	fun build(): T
}

class RecognitionTreeBuilder(
	private val calls: MutableList<ApduCallDefinition> = mutableListOf(),
) : RecognitionScope,
	Builder<List<ApduCallDefinition>> {
	override fun call(content: @CifMarker (ApduCardCallScope.() -> Unit)) {
		val builder = ApduCardCallScopeBuilder()
		content.invoke(builder)
		calls.add(builder.build())
	}

	override fun build(): RecognitionTree = calls.toList()
}

@OptIn(ExperimentalUnsignedTypes::class)
class ApduCardCallScopeBuilder(
	val responses: MutableSet<ResponseApduDefinition> = mutableSetOf(),
) : ApduCardCallScope,
	Builder<ApduCallDefinition> {
	private var _command: UByteArray? = null
	override var command: UByteArray
		get() = _command!!
		set(value) {
			_command = value
		}

	override fun response(content: @CifMarker (ApduResponseScope.() -> Unit)) {
		val builder = ApduResponseBuilder()
		content.invoke(builder)
		responses.add(builder.build())
	}

	override fun build(): ApduCallDefinition =
		ApduCallDefinition(
			_command!!.toPrintable(),
			responses,
		)
}

class ApduResponseBuilder :
	ApduResponseScope,
	Builder<ResponseApduDefinition> {
	var body: DataMaskDefinition? = null
	var conclusion: ConclusionDefinition? = null

	override var trailer: UShort = 0x9000u

	override fun body(
		tag: UByte,
		content: @CifMarker (ResponseDataMaskScope.() -> Unit),
	) {
		val builder = ResponseDataMaskBuilder(tag)
		content.invoke(builder)
		body = builder.build()
	}

	override fun body(content: @CifMarker (MatchingDataScope.() -> Unit)) {
		val builder = MatchingDataBuilder()
		content.invoke(builder)
		body = builder.build()
	}

	override fun recognizedCardType(name: String) {
		conclusion = ConclusionDefinition.RecognizedCardType(name)
	}

	override fun call(content: @CifMarker (ApduCardCallScope.() -> Unit)) {
		val builder = ApduCardCallScopeBuilder()
		content.invoke(builder)
		conclusion = ConclusionDefinition.Call(builder.build())
	}

	override fun build(): ResponseApduDefinition =
		ResponseApduDefinition(
			body = body,
			trailer = trailer,
			conclusion = conclusion!!,
		)
}

class ResponseDataMaskBuilder(
	var tag: UByte,
) : ResponseDataMaskScope,
	Builder<DataMaskDefinition.DataObject> {
	var match: DataMaskDefinition? = null
		set(value) {
			if (built != null) {
				throw IllegalStateException("Cannot update the builder after it builds!")
			}
			field = value
		}
	var built: DataMaskDefinition.DataObject? = null

	override fun matchBytes(content: @CifMarker (MatchingDataScope.() -> Unit)): DataMaskDefinition {
		val builder = MatchingDataBuilder()
		content.invoke(builder)
		match = builder.build()
		return build()
	}

	override fun matchData(
		tag: UByte,
		content: @CifMarker (ResponseDataMaskScope.() -> DataMaskDefinition),
	): DataMaskDefinition.DataObject {
		val builder = ResponseDataMaskBuilder(tag)
		content.invoke(builder)
		match = builder.build()
		return build()
	}

	override fun build(): DataMaskDefinition.DataObject {
		if (built == null) {
			built =
				DataMaskDefinition.DataObject(
					tag = tag,
					match = match!!,
				)
		}
		return built!!
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
class MatchingDataBuilder :
	MatchingDataScope,
	Builder<DataMaskDefinition.MatchingData> {
	private var _value: UByteArray? = null
	override var value: UByteArray
		get() = _value!!
		set(value) {
			_value = value
		}
	override var offset: UInt = 0u
	override var length: UInt? = null
	override var mask: UByteArray? = null
	override var rule: MatchRule? = null

	override fun build(): DataMaskDefinition.MatchingData =
		DataMaskDefinition.MatchingData(
			matchingValue = _value!!.toPrintable(),
			offset = offset,
			length = length,
			mask = mask?.toPrintable(),
			rule = rule ?: MatchRule.Equals,
		)
}
