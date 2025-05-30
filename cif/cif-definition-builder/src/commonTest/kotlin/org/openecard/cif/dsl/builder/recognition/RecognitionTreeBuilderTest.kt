package org.openecard.cif.dsl.builder.recognition

import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertEquals

class RecognitionTreeBuilderTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun simpleExample() {
		val sut = RecognitionTreeBuilder()

		sut.call {
			command = hex("00A4000C023F00")
			response {
				trailer = 0x9000u
				call {
					command = hex("00B20104FF")
					response {
						body {
							offset = 0x17u
							length = 0x01u
							value = hex("06")
						}
						trailer = 0x9000u
						call {
							command = hex("00A4000C023F00")
							response {
								trailer = 0x9000u
								call {
									command = hex("00A4010C02AB00")
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
				recognizedCardType("http://www.dgn.de/cif/HPCqSIG")
			}
		}

		val actual = sut.build()

		assertEquals(actual.size, 1)
	}
}
