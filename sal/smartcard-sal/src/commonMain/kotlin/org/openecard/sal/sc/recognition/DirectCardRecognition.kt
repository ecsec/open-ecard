package org.openecard.sal.sc.recognition

import org.openecard.cif.definition.recognition.ApduCallDefinition
import org.openecard.cif.definition.recognition.ConclusionDefinition
import org.openecard.cif.definition.recognition.DataMaskDefinition
import org.openecard.cif.definition.recognition.MatchRule
import org.openecard.cif.definition.recognition.RecognitionTree
import org.openecard.cif.definition.recognition.ResponseApduDefinition
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toCommandApdu
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.tlv.toTlvBer
import org.openecard.utils.common.maskedContains

class DirectCardRecognition(
	private val tree: RecognitionTree,
) : CardRecognition {
	override fun recognizeCard(channel: CardChannel): String? = RecognizerContext(channel).processTree(tree)

	private class RecognizerContext(
		val channel: CardChannel,
	)

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun RecognizerContext.processTree(calls: List<ApduCallDefinition>): String? {
		val result =
			calls.firstOrNull() ?.let {
				processApdu(it)
			}
		return result ?: if (calls.isNotEmpty()) {
			processTree(calls.drop(1))
		} else {
			null
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun RecognizerContext.processApdu(call: ApduCallDefinition): String? {
		val response = channel.transmit(call.command.v.toCommandApdu())

		return call.responses.firstNotNullOfOrNull { expectedResponse ->
			if (expectedResponse.trailerMatch(response) && expectedResponse.contentMatch(response)) {
				val conclusion = expectedResponse.conclusion
				when (conclusion) {
					is ConclusionDefinition.Call -> {
						processApdu(conclusion.call)
					}
					is ConclusionDefinition.RecognizedCardType -> {
						conclusion.name
					}
				}
			} else {
				null
			}
		}
	}

	private fun ResponseApduDefinition.trailerMatch(response: ResponseApdu): Boolean = trailer == response.sw

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun ResponseApduDefinition.contentMatch(response: ResponseApdu): Boolean {
		return body?.let { body ->
			val data = response.data
			when (body) {
				is DataMaskDefinition.MatchingData -> {
					body.contentMatch(data)
				}
				is DataMaskDefinition.DataObject -> {
					body.contentMatch(data)
				}
			}
		} ?: true
		// return true when there is nothing to match
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun DataMaskDefinition.MatchingData.contentMatch(data: UByteArray): Boolean {
		try {
			val refVal = this.matchingValue.v
			val data = data.sliceArray(offset.toInt() until offset.toInt() + refVal.size)
			length?.let {
				if (data.size != it.toInt()) {
					return false
				}
			}
			return when (rule) {
				MatchRule.Equals -> data.maskedContains(refVal, mask?.v, offset = 0)
				MatchRule.Contains -> data.maskedContains(refVal, mask?.v, offset = null)
			}
		} catch (ex: IndexOutOfBoundsException) {
			// wrong index values lead to a non match
			// this is shorter than performing every check in the logic code
			return false
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun DataMaskDefinition.DataObject.contentMatch(data: UByteArray): Boolean {
		try {
			val tlv = data.toTlvBer()
			val matchedTlv = tlv.tlv.findNextTags(tag).firstOrNull()
			return if (matchedTlv != null) {
				val next = this.match
				val nextData = matchedTlv.contentAsBytesBer
				when (next) {
					is DataMaskDefinition.MatchingData -> {
						next.contentMatch(nextData)
					}

					is DataMaskDefinition.DataObject -> {
						next.contentMatch(nextData)
					}
				}
			} else {
				// tag not found
				false
			}
		} catch (ex: Exception) {
			// parse exception, the data is not tlv
			return false
		}
	}
}
