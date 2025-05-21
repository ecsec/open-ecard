package org.openecard.sal.sc

import org.openecard.sal.iface.DeviceConnection
import org.openecard.sal.iface.SalSession

class SmartcardSalSession internal constructor(
	override val sal: SmartcardSal,
	override val sessionId: String,
) : SalSession {
	override fun initializeStack() {
		TODO("Not yet implemented")
	}

	override fun shutdownStack() {
		TODO("Not yet implemented")
	}

	override fun devices(): List<String> {
		TODO("Not yet implemented")
	}

	override fun connect(
		terminal: String,
		isExclusive: Boolean,
	): DeviceConnection {
		TODO("Not yet implemented")
	}
}
