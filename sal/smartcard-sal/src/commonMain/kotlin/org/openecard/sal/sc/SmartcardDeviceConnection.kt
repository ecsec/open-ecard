package org.openecard.sal.sc

import org.openecard.sal.iface.DeviceConnection
import org.openecard.sal.iface.dids.AuthenticationDid
import org.openecard.sc.iface.CardDisposition

class SmartcardDeviceConnection(
	override val connectionId: String,
	override val session: SmartcardSalSession,
	val cardType: String,
	// TODO: use real cif type
	val cif: Any,
) : DeviceConnection {
	override val initialApplication: SmartcardApplication
		get() = TODO("Not yet implemented")

	override val applications: List<SmartcardApplication> get() {
		TODO()
	}

	override val authenticatedDids: List<AuthenticationDid> get() {
		TODO()
	}

	override fun close(disposition: CardDisposition) {
		TODO("Not yet implemented")
	}

	internal fun setDidFulfilled(did: SmartcardDid) {
		TODO("Not yet implemented")
	}
}
