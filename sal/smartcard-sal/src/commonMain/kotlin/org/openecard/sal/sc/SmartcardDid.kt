package org.openecard.sal.sc

import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.definition.did.DidScope
import org.openecard.sal.iface.MissingAuthentications

abstract class SmartcardDid<D : DidDefinition>(
	val did: D,
	val application: SmartcardApplication,
) {
	val name: String = did.name
	val isLocal: Boolean = did.scope == DidScope.LOCAL

	protected fun missingAuthentications(actionType: Any): MissingAuthentications {
		TODO("Not yet implemented")
	}

	protected fun setDidFulfilled() {
		application.device.setDidFulfilled(this)
	}

	protected fun setDidUnfulfilled() {
		application.device.setDidUnfulfilled(this)
	}

	override fun equals(other: Any?): Boolean =
		when (other) {
			is SmartcardDid<*> -> other.name == this.name
			else -> false
		}

	override fun hashCode(): Int = name.hashCode()
}
