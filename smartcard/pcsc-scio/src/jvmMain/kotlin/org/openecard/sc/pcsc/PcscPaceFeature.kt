package org.openecard.sc.pcsc

import org.openecard.sc.iface.PaceCapability
import org.openecard.sc.iface.PaceEstablishChannelRequest
import org.openecard.sc.iface.PaceEstablishChannelResponse
import org.openecard.sc.iface.PaceFeature

class PcscPaceFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val executePaceCtrlCode: Int,
) : PaceFeature {
	override fun getPaceCapabilities(): Set<PaceCapability> =
		mapScioError {
			TODO("Not yet implemented")
		}

	override fun establishChannel(req: PaceEstablishChannelRequest): PaceEstablishChannelResponse =
		mapScioError {
			TODO("Not yet implemented")
		}

	override fun establishChannel(
		pinId: UByte,
		chat: ByteArray,
		pin: ByteArray,
	): PaceEstablishChannelResponse =
		mapScioError {
			TODO("Not yet implemented")
		}

	override fun destroyChannel() =
		mapScioError {
			TODO("Not yet implemented")
		}
}
