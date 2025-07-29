package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.definition.did.DidScope
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.acl.missingAuthentications

sealed interface SmartcardDid<D : DidDefinition> {
	val did: D
	val application: SmartcardApplication
	val name: String
		get() = did.name
	val isLocal: Boolean
		get() = did.scope == DidScope.LOCAL

	fun setDidFulfilled() {
		application.device.setDidFulfilled(this)
	}

	fun setDidUnfulfilled() {
		application.device.setDidUnfulfilled(this)
	}

	fun toStateReference(): DidStateReference

	sealed class BaseSmartcardDid<D : DidDefinition>(
		override val did: D,
		override val application: SmartcardApplication,
	) : SmartcardDid<D> {
		protected fun missingAuthentications(acl: CifAclOr): MissingAuthentications =
			acl.missingAuthentications(application.device)

		override fun toStateReference(): DidStateReference =
			DidStateReference(
				name,
				application.device.authenticatedDids.any {
					it.name == this.name
				},
				null,
			)

		override fun equals(other: Any?): Boolean =
			when (other) {
				is BaseSmartcardDid<*> -> other.name == this.name
				else -> false
			}

		override fun hashCode(): Int = name.hashCode()
	}
}
