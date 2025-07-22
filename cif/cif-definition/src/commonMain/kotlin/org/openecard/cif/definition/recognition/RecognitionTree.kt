package org.openecard.cif.definition.recognition

typealias RecognitionTree = List<ApduCallDefinition>

fun RecognitionTree.removeUnsupported(supportedCardTypes: Set<String>): RecognitionTree {
	val cleanedTree = mutableListOf<ApduCallDefinition>()

	for (call in this) {
		val prunedCall = pruneCall(call, supportedCardTypes)
		if (prunedCall != null) {
			cleanedTree.add(prunedCall)
		}
	}
	return cleanedTree
}

private fun pruneCall(
	call: ApduCallDefinition,
	supportedCardTypes: Set<String>,
): ApduCallDefinition? {
	val validResponses = mutableListOf<ResponseApduDefinition>()

	for (response in call.responses) {
		val newConclusion =
			when (val conclusion = response.conclusion) {
				is ConclusionDefinition.RecognizedCardType -> {
					if (conclusion.name in supportedCardTypes) {
						conclusion
					} else {
						null
					}
				}

				is ConclusionDefinition.Call -> {
					val prunedCall = pruneCall(conclusion.call, supportedCardTypes)
					prunedCall?.let { ConclusionDefinition.Call(it) }
				}
			}

		if (newConclusion != null) {
			validResponses.add(response.copy(conclusion = newConclusion))
		}
	}

	return if (validResponses.isNotEmpty()) {
		call.copy(responses = validResponses.toSet())
	} else {
		null
	}
}
