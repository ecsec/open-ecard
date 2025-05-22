package org.openecard.sal.sc

import org.openecard.sal.iface.Application
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.Did

class SmartcardApplication(
	override val device: SmartcardDeviceConnection,
	override val name: String,
) : Application {
	override val datasets: List<SmartcardDataset>
		get() = TODO("Not yet implemented")

	override val dids: List<Did>
		get() = TODO("Not yet implemented")

	override val missingSelectAuthentications: MissingAuthentications
		get() = TODO("Not yet implemented")

	override val isConnected: Boolean
		get() = TODO("Not yet implemented")

	override fun connect() {
		TODO("Not yet implemented")
	}
}
