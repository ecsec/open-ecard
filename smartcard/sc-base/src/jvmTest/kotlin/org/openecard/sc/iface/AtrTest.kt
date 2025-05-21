package org.openecard.sc.iface

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class AtrTest {
	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	@Test
	fun `read ATRs`() {
		"3B 00 3B 28 00 34 41 45 41 30 32 30 30".replace(" ", "").hexToUByteArray().toAtr().let { atr ->
			assertEquals(0x3Bu, atr.ts)
		}
		"3B 04 00 04 00 00".replace(" ", "").hexToUByteArray().toAtr().let { atr ->
			assertEquals(0x3Bu, atr.ts)
		}
		"3B 1F 11 00 67 42 41 46 49 53 45 53 52 66 FF 81 90 00".replace(" ", "").hexToUByteArray().toAtr().let { atr ->
			assertEquals(0x3Bu, atr.ts)
			assertEquals(2, atr.historicalBytes?.dataObjects?.size)
		}
		"3B 85 80 01 30 01 01 30 10 14".replace(" ", "").hexToUByteArray().toAtr().let { atr ->
			assertEquals(0x3Bu, atr.ts)
			assertEquals(0, atr.historicalBytes?.dataObjects?.size)
		}
		"3B 8A 80 01 80 31 B8 73 84 01 E0 82 90 00 06".replace(" ", "").hexToUByteArray().toAtr().let { atr ->
			assertEquals(0x3Bu, atr.ts)
			assertEquals(2, atr.historicalBytes?.dataObjects?.size)
			val hb = assertNotNull(atr.historicalBytes)
			assertNotNull(hb.cardServiceData)
			assertNotNull(hb.cardCapabilities)
		}
		"3B 04 60 89".replace(" ", "").hexToUByteArray().toAtr().let { atr ->
			assertEquals(0x3Bu, atr.ts)
		}
		"3B 05 00 03 36 66 AE".replace(" ", "").hexToUByteArray().toAtr().let { atr ->
			assertEquals(0x3Bu, atr.ts)
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `parse pcsclite ATRs`() {
		val samples = SampleAtr.loadSamples()
		samples.forEach {
			try {
				val atr = it.atr.v.toAtr()
				assertContentEquals(it.atr.v, atr.bytes)
			} catch (ex: Exception) {
				fail("Error processing atr: ${it.description}", ex)
			}
		}
	}
}
