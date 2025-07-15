package org.openecard.sc.apdu.command

import org.openecard.sc.tlv.Tag

object VerifyTags {
	/**
	 * Biometric data, ISO/IEC 7816-11, Sec. 6.2
	 */
	object Biomentric {
		val biometricDataPlain = Tag.forTagNumWithClass(0x5F2Eu)

		val biometricDataTemplate = Tag.forTagNumWithClass(0x7F2Eu)

		val biometricDataStandardPrimitive = Tag.forTagNumWithClass(0x81u)
		val biometricDataStandardConstructed = Tag.forTagNumWithClass(0xA1u)

		val biometricDataProprietaryPrimitive = Tag.forTagNumWithClass(0x82u)
		val biometricDataProprietaryConstructed = Tag.forTagNumWithClass(0xA2u)
	}
}
