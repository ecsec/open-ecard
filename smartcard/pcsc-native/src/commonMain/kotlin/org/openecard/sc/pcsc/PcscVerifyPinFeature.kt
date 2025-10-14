package org.openecard.sc.pcsc

import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toResponseApdu
import org.openecard.sc.iface.feature.PinVerify
import org.openecard.sc.iface.feature.VerifyPinFeature

class PcscVerifyPinFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val verifyPinDirectCode: Int,
) : VerifyPinFeature {
	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun verifyPin(request: PinVerify): ResponseApdu =
		mapScioError {
			// TODO: make cancellable
			val commandData = request.bytes
			val response = terminalConnection.controlCommand(verifyPinDirectCode, commandData.toByteArray())
			response.toResponseApdu()
		}
}
