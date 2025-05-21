package org.openecard.sal.sc

import org.openecard.sal.iface.Application
import org.openecard.sal.iface.DeviceConnection
import org.openecard.sal.iface.dids.AuthenticationDid
import org.openecard.sc.iface.CardDisposition

class SmartcardDeviceConnection(
	override val connectionId: String,
	override val session: SmartcardSalSession,
	val cardType: String,
) : DeviceConnection {
	override val applications: Application get() {
		TODO()
	}
	override val authenticatedDids: List<AuthenticationDid> get() {
		TODO()
	}

	override fun close(disposition: CardDisposition) {
		TODO("Not yet implemented")
	}
}
