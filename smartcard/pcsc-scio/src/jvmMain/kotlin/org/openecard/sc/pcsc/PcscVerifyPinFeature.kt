package org.openecard.sc.pcsc

import org.openecard.sc.iface.PasswordAttributes
import org.openecard.sc.iface.ResponseApdu
import org.openecard.sc.iface.VerifyPinFeature

class PcscVerifyPinFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val verifyPinDirectCode: Int,
) : VerifyPinFeature {
	override fun verifyPin(
		passwordAttributes: PasswordAttributes,
		template: ByteArray,
	): ResponseApdu {
		TODO("Not yet implemented")
	}
}
