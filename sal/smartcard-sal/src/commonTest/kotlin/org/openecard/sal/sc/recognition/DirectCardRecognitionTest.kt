package org.openecard.sal.sc.recognition

import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import org.openecard.cif.definition.recognition.RecognitionTree
import org.openecard.cif.dsl.builder.recognition.RecognitionTreeBuilder
import org.openecard.sc.apdu.toResponseApdu
import org.openecard.sc.iface.CardChannel
import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DirectCardRecognitionTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `detect old est eid`() {
		val channel: CardChannel =
			mock {
				every { transmit(any()) } sequentiallyReturns (
					listOf(
						hex("9000"),
					).map { it.toResponseApdu() }
				)
			}

		val sut = DirectCardRecognition(tree)
		assertEquals("http://cif.id.ee/eid", sut.recognizeCard(channel))
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `detect est eid 3-5-8`() {
		val channel: CardChannel =
			mock {
				every { transmit(any()) } sequentiallyReturns (
					listOf(
						hex("6A82"),
						hex("9000"),
						hex("0305089000"),
					).map { it.toResponseApdu() }
				)
			}

		val sut = DirectCardRecognition(tree)
		assertEquals("http://cif.id.ee/eidV3.5.8+", sut.recognizeCard(channel))
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `detect est eid 3-5-0-1`() {
		val channel: CardChannel =
			mock {
				every { transmit(any()) } sequentiallyReturns (
					listOf(
						hex("6A82"),
						hex("9000"),
						hex("0305079000"),
					).map { it.toResponseApdu() }
				)
			}

		val sut = DirectCardRecognition(tree)
		assertEquals("http://cif.id.ee/eidV3.5", sut.recognizeCard(channel))
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `detect est eid 3-5-0-2`() {
		val channel: CardChannel =
			mock {
				every { transmit(any()) } sequentiallyReturns (
					listOf(
						hex("6A82"),
						hex("9000"),
						hex("03059000"),
					).map { it.toResponseApdu() }
				)
			}

		val sut = DirectCardRecognition(tree)
		assertEquals("http://cif.id.ee/eidV3.5.0-2", sut.recognizeCard(channel))
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `detect hpcqsig`() {
		val channel: CardChannel =
			mock {
				every { transmit(any()) } sequentiallyReturns (
					listOf(
						hex("6A82"),
						hex("9000"),
						hex("61084F06D276000040026282"),
					).map { it.toResponseApdu() }
				)
			}

		val sut = DirectCardRecognition(tree)
		assertEquals("http://www.dgn.de/cif/HPCqSIG", sut.recognizeCard(channel))
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `detect no card`() {
		val channel: CardChannel =
			mock {
				every { transmit(any()) } sequentiallyReturns (
					listOf(
						hex("6A82"),
						hex("9000"),
						hex("9000"),
					).map { it.toResponseApdu() }
				)
			}

		val sut = DirectCardRecognition(tree)
		assertNull(sut.recognizeCard(channel))
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
private val tree: RecognitionTree by lazy {
	val b = RecognitionTreeBuilder()
	b.run {
		call {
			command = hex("00A4040C0FF04573744549442076657220312E30")
			response {
				// TODO: check if this match makes sense
				body {
					length = 0x00u
				}
				recognizedCardType("http://cif.id.ee/eid")
			}
		}

		call {
			command = hex("00A4040C0FD23300000045737445494420763335")
			response {
				// TODO: check if this match makes sense
				body {
					length = 0x00u
				}
				call {
					command = hex("00CA010003")
					response {
						// Version is 03.05.00-07 (The only version having the scheme from 3.5.8+ cards)
						body {
							value = hex("030500")
							mask = hex("FFFFF8")
						}
						recognizedCardType("http://cif.id.ee/eidV3.5")
					}
					response {
						body {
							value = hex("030500")
							mask = hex("FFFF00")
						}
						recognizedCardType("http://cif.id.ee/eidV3.5.8+")
					}
					response {
						body {
							value = hex("0305")
						}
						recognizedCardType("http://cif.id.ee/eidV3.5.0-2")
					}
					response {
						trailer = 0x6282u
						body(0x61u) {
							matchBytes {
								length = 0x08u
								value =
									hex(
										"4F06D27600004002",
									)
							}
						}
						recognizedCardType("http://www.dgn.de/cif/HPCqSIG")
					}
				}
			}
		}
	}

	b.build()
}
