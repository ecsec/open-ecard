package org.openecard.sal.sc

import org.openecard.sal.iface.MissingAuthentications

abstract class SmartcardDid(
	val name: String,
	val isLocal: Boolean,
	val application: SmartcardApplication,
) {
	protected fun missingAuthentications(actionType: Any): MissingAuthentications {
		TODO("Not yet implemented")
	}

	protected fun setDidFulfilled() {
		application.device.setDidFulfilled(this)
	}

	protected fun sendEvent() {
		TODO("Not yet implemented")
	}
}
