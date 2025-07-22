package org.openecard.cif.bundled

import org.openecard.cif.dsl.builder.recognition.RecognitionTreeBuilder
import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalUnsignedTypes::class)
class ReducedTreeTest {
	@Test
	fun `load complete tree`() {
		assertNotNull(CompleteTree.calls)
	}

	@Test
	fun `reduce tree to supported cards`() {
		assertContentEquals(
			testTree,
			testTree.removeUnsupported(setOf(EgkCifDefinitions.cardType, NpaDefinitions.cardType, "UnsupportedCard")),
		)
		assertContentEquals(
			listOf(),
			testTree.removeUnsupported(setOf("Another Card")),
		)
		assertContentEquals(
			listOf(),
			testTree.removeUnsupported(setOf()),
		)
		assertContentEquals(
			npaEgKTree,
			testTree.removeUnsupported(setOf(EgkCifDefinitions.cardType, NpaDefinitions.cardType, "Another Card")),
		)
		assertContentEquals(
			npaTree,
			testTree.removeUnsupported(setOf(NpaDefinitions.cardType, "Another Card")),
		)
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
private val testTree =
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
				response {
					trailer = 0x6300u
					call {
						command = hex("00B00000FF")
						response {
							recognizedCardType(NpaDefinitions.cardType)
						}
					}
				}
			}
		}.build()

@OptIn(ExperimentalUnsignedTypes::class)
private val npaTree =
	RecognitionTreeBuilder()
		.apply {
			call {
				command = hex("00B20304FF")
				response {
					trailer = 0x6300u
					call {
						command = hex("00B00000FF")
						response {
							recognizedCardType(NpaDefinitions.cardType)
						}
					}
				}
			}
		}.build()

@OptIn(ExperimentalUnsignedTypes::class)
private val npaEgKTree =
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
					trailer = 0x6300u
					call {
						command = hex("00B00000FF")
						response {
							recognizedCardType(NpaDefinitions.cardType)
						}
					}
				}
			}
		}.build()
