package org.openecard.cif.bundled

import org.openecard.cif.definition.recognition.ConclusionDefinition
import org.openecard.cif.dsl.builder.recognition.RecognitionTreeBuilder
import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalUnsignedTypes::class)
class ReducedTreeTest {
	@Test
	fun `reduce tree to supported cards`() {
		val supportedCardTypes =
			setOf(
				EgkCifDefinitions.cardType,
				HbaDefinitions.cardType,
				NpaDefinitions.cardType,
			)

		val tree =
			RecognitionTreeBuilder()
				.apply {
					call {
						command = hex("00B00000FF")
						response {
							recognizedCardType(EgkCifDefinitions.cardType)
						}
					}

					call {
						command = hex("00B20304FF")
						response {
							recognizedCardType("UnsupportedCard")
						}
					}

					call {
						command = hex("00B20304FF")
						response {
							call {
								command = hex("00B00000FF")
								response {
									recognizedCardType(NpaDefinitions.cardType)
								}
							}
						}
					}
				}.build()

		val cleanedTree = tree.removeUnsupported(supportedCardTypes)

		assertEquals(2, cleanedTree.size)

		val firstConclusion = cleanedTree[0].responses.first().conclusion
		assertTrue(firstConclusion is ConclusionDefinition.RecognizedCardType)
		assertEquals(EgkCifDefinitions.cardType, firstConclusion.name)

		val recursiveConclusion = cleanedTree[1].responses.first().conclusion
		assertTrue(recursiveConclusion is ConclusionDefinition.Call)

		val innerCall = recursiveConclusion.call
		val innerConclusion = innerCall.responses.first().conclusion
		assertTrue(innerConclusion is ConclusionDefinition.RecognizedCardType)
		assertEquals(NpaDefinitions.cardType, innerConclusion.name)

		val cardsInTree = mutableListOf<String>()

		for (call in cleanedTree) {
			for (response in call.responses) {
				val conclusion = response.conclusion

				if (conclusion is ConclusionDefinition.RecognizedCardType) {
					cardsInTree.add(conclusion.name)
				}

				if (conclusion is ConclusionDefinition.Call) {
					val innerResponses = conclusion.call.responses
					for (innerResponse in innerResponses) {
						val innerConclusion = innerResponse.conclusion
						if (innerConclusion is ConclusionDefinition.RecognizedCardType) {
							cardsInTree.add(innerConclusion.name)
						}
					}
				}
			}
		}
		assertFalse(cardsInTree.contains("UnsupportedCard"))
	}
}
