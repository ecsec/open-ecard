package org.openecard.sc.pcsc

import org.openecard.sc.iface.ModifyPinFeature

class PcscModifyPinFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val modifyPinDirectCtrlCode: Int,
) : ModifyPinFeature
