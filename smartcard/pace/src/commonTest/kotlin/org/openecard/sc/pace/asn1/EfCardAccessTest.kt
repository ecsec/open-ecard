package org.openecard.sc.pace.asn1

import org.openecard.sc.pace.asn1.EfCardAccess.Companion.toEfCardAccess
import org.openecard.sc.pace.oid.PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128
import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertEquals

class EfCardAccessTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	val efCaEgk = hex("31143012060a04007f0007020204020202010202010d")

	@OptIn(ExperimentalUnsignedTypes::class)
	val efCaNpa =
		hex(
			"3181c1300d060804007f00070202020201023012060a04007f000702020302020201020201483012060a04007f0007020204020202010202010d301c060904007f000702020302300c060704007f0007010202010d020148302a060804007f0007020206161e687474703a2f2f6273692e62756e642e64652f6369662f6e70612e786d6c303e060804007f000702020831323012060a04007f00070202030202020102020149301c060904007f000702020302300c060704007f0007010202010d020149000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
		)

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `deserialize EF_CA data eGK`() {
		val efca = efCaEgk.toEfCardAccess()
		val secInfos = efca.secInfos
		assertEquals(1, secInfos.size)
		val paceInfo = efca.paceInfo.firstOrNull()?.info
		assertEquals(
			id_PACE_ECDH_GM_AES_CBC_CMAC_128,
			paceInfo?.protocol?.value,
		)
		assertEquals(
			2,
			paceInfo?.version,
		)
		assertEquals(
			13u,
			paceInfo?.parameterId,
		)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `deserialize EF_CA data nPa`() {
		val efca = efCaNpa.toEfCardAccess()
		val secInfos = efca.secInfos
		assertEquals(6, secInfos.size)
		val paceInfo = efca.paceInfo.firstOrNull()?.info
		assertEquals(
			id_PACE_ECDH_GM_AES_CBC_CMAC_128,
			paceInfo?.protocol?.value,
		)
		assertEquals(
			2,
			paceInfo?.version,
		)
		assertEquals(
			13u,
			paceInfo?.parameterId,
		)
	}
}
