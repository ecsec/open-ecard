package org.openecard.cif.definition.recognition

typealias RecognitionTree = List<ApduCallDefinition>

/**
 * Removes all unsupported cards from this RecognitionTree.
 */
fun RecognitionTree.removeUnsupported(supportedCardTypes: Set<String>): RecognitionTree =
	this.mapNotNull {
		pruneCall(it, supportedCardTypes)
	}

private fun pruneCall(
	call: ApduCallDefinition,
	supportedCardTypes: Set<String>,
): ApduCallDefinition? {
	val validResponses =
		// check all responses and their subtrees
		call.responses.mapNotNull { response ->
			val newConclusion =
				when (val conclusion = response.conclusion) {
					// keep the conclusion if it refers to a supported card
					is ConclusionDefinition.RecognizedCardType -> {
						conclusion.takeIf { it.name in supportedCardTypes }
					}

					is ConclusionDefinition.Call -> {
						// recurse into subtree
						pruneCall(conclusion.call, supportedCardTypes)?.let {
							ConclusionDefinition.Call(it)
						}
					}
				}

			// replace new conclusion in response or return null to remove this subtree
			newConclusion?.let { response.copy(conclusion = newConclusion) }
		}

	return if (validResponses.isNotEmpty()) {
		call.copy(responses = validResponses.toSet())
	} else {
		null
	}
}
