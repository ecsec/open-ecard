package org.openecard.sc.pcsc

import org.openecard.sc.iface.ResponseApdu
import org.openecard.sc.iface.feature.PinVerify
import org.openecard.sc.iface.feature.VerifyPinFeature
import org.openecard.sc.iface.toResponseApdu

class PcscVerifyPinFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val verifyPinDirectCode: Int,
) : VerifyPinFeature {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun verifyPin(request: PinVerify): ResponseApdu =
		mapScioError {
			val commandData = request.bytes
			val response = terminalConnection.controlCommand(verifyPinDirectCode, commandData.toByteArray())
			response.toResponseApdu()
		}
}
