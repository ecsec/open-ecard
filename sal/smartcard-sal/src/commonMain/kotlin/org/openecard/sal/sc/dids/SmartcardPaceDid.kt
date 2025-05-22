package org.openecard.sal.sc.dids

import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinCallback
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardDid
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PacePinId

class SmartcardPaceDid(
	name: String,
	isLocal: Boolean,
	application: SmartcardApplication,
	override val pinType: PacePinId,
) : SmartcardDid(name, isLocal, application),
	PaceDid {
	override val missingAuthAuthentications: MissingAuthentications
		get() = missingAuthentications("auth")

	override fun capturePinInHardware(): Boolean {
		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun establishChannel(
		pinCallback: PinCallback,
		chat: UByteArray?,
	): PaceEstablishChannelResponse {
		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun establishChannel(chat: UByteArray?): PaceEstablishChannelResponse {
		TODO("Not yet implemented")
	}

	override fun closeChannel() {
		TODO("Not yet implemented")
	}
}
