package org.openecard.sc.pcsc

import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toResponseApdu
import org.openecard.sc.iface.feature.ModifyPinFeature
import org.openecard.sc.iface.feature.PinModify

class PcscModifyPinFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val modifyPinDirectCtrlCode: Int,
) : ModifyPinFeature {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun modifyPin(request: PinModify): ResponseApdu =
		mapScioError {
			val commandData = request.bytes
			val response = terminalConnection.controlCommand(modifyPinDirectCtrlCode, commandData.toByteArray())
			response.toResponseApdu()
		}
}
