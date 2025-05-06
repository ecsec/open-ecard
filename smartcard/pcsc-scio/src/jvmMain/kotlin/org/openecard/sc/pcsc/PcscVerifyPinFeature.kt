package org.openecard.sc.pcsc

import org.openecard.sc.iface.PasswordAttributes
import org.openecard.sc.iface.ResponseApdu
import org.openecard.sc.iface.VerifyPinFeature

class PcscVerifyPinFeature(
	terminalConnection: PcscTerminalConnection,
	verifyPinCode: Int,
) : VerifyPinFeature {
	override fun verifyPin(
		passwordAttributes: PasswordAttributes,
		template: ByteArray,
	): ResponseApdu {
		TODO("Not yet implemented")
	}
}
