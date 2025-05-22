package org.openecard.cif.dsl.builder.recognition

import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
fun hex(hex: String): UByteArray = hex.hexToUByteArray()

class RecognitionTreeBuilderTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun simpleExample() {
		val sut = RecognitionTreeBuilder()

		sut.call {
			command = hex("00A4000C023F00")
			response {
				trailer = 0x9000u
				conclusion {
					call {
						command = hex("00B20104FF")
						response {
							body {
								offset = 0x17u
								length = 0x01u
								value = hex("06")
							}
							trailer = 0x9000u
							conclusion {
								call {
									command = hex("00A4000C023F00")
									response {
										trailer = 0x9000u
										conclusion {
											call {
												command = hex("00A4010C02AB00")
											}
										}
									}
								}
							}
						}
					}
				}
			}
			response {
				trailer = 0x9000u
				body(0x61u) {
					matchBytes {
						length = 0x08u
						value = hex("4F06D27600004002")
					}
				}
				conclusion {
					recognizedCardType("http://www.dgn.de/cif/HPCqSIG")
				}
			}
		}

		val actual = sut.build()

		assertEquals(actual.size, 1)
	}
}
