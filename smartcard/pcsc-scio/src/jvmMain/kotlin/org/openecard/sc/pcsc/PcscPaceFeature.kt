package org.openecard.sc.pcsc

import org.openecard.sc.iface.feature.GetReaderCapabilitiesResponse
import org.openecard.sc.iface.feature.PaceCapability
import org.openecard.sc.iface.feature.PaceDestroyChannelRequest
import org.openecard.sc.iface.feature.PaceDestroyChannelResponse
import org.openecard.sc.iface.feature.PaceEstablishChannelRequest
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.feature.PaceGetReaderCapabilitiesRequest

class PcscPaceFeature(
	private val terminalConnection: PcscTerminalConnection,
	private val executePaceCtrlCode: Int,
) : PaceFeature {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun getPaceCapabilities(): Set<PaceCapability> =
		mapScioError {
			val commandData = PaceGetReaderCapabilitiesRequest.bytes
			val response = terminalConnection.controlCommand(executePaceCtrlCode, commandData.toByteArray())
			val resp = GetReaderCapabilitiesResponse.fromPaceResponse(response.toUByteArray())
			resp.capabilities
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun establishChannel(req: PaceEstablishChannelRequest): PaceEstablishChannelResponse =
		mapScioError {
			// TODO: make cancellable
			val commandData = req.bytes
			val response = terminalConnection.controlCommand(executePaceCtrlCode, commandData.toByteArray())
			val resp = PaceEstablishChannelResponse.fromPaceResponse(response.toUByteArray())
			resp
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun destroyChannel() =
		mapScioError {
			val commandData = PaceDestroyChannelRequest.bytes
			val response = terminalConnection.controlCommand(executePaceCtrlCode, commandData.toByteArray())
			PaceDestroyChannelResponse.fromPaceResponse(response.toUByteArray())
			Unit
		}
}
