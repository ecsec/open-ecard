package org.openecard.sc.pcsc

import org.openecard.sc.iface.PaceCapability
import org.openecard.sc.iface.PaceEstablishChannelRequest
import org.openecard.sc.iface.PaceEstablishChannelResponse
import org.openecard.sc.iface.PaceFeature

class PcscPaceFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val executePaceCtrlCode: Int,
) : PaceFeature {
	override val paceCapabilities: Set<PaceCapability> by lazy {
		TODO("Not yet implemented")
	}

	override fun establishChannel(req: PaceEstablishChannelRequest): PaceEstablishChannelResponse {
		TODO("Not yet implemented")
	}

	override fun establishChannel(
		pinId: UByte,
		chat: ByteArray,
		pin: ByteArray,
	): PaceEstablishChannelResponse {
		TODO("Not yet implemented")
	}

	override fun destroyChannel() {
		TODO("Not yet implemented")
	}
}
