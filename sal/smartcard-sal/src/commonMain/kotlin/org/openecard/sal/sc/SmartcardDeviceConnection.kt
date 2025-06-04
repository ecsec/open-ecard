package org.openecard.sal.sc

import org.openecard.cif.definition.CardInfoDefinition
import org.openecard.sal.iface.DeviceConnection
import org.openecard.sal.iface.dids.AuthenticationDid
import org.openecard.sal.sc.acl.hasSolution
import org.openecard.sal.sc.acl.selectForProtocol
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardDisposition
import org.openecard.utils.common.returnIf

class SmartcardDeviceConnection(
	override val connectionId: String,
	override val session: SmartcardSalSession,
	val card: Card,
	val cif: CardInfoDefinition,
) : DeviceConnection {
	val cardType: String = cif.metadata.id

	private val _authenticatedDids: MutableList<SmartcardDid<*>> = mutableListOf()

	override val authenticatedDids: List<AuthenticationDid> get() {
		return _authenticatedDids.filterIsInstance<AuthenticationDid>()
	}

	override val initialApplication: SmartcardApplication
		get() = applications.first()

	override val applications: List<SmartcardApplication> by lazy {
		cif.applications
			.mapNotNull { app ->
				app.selectAcl
					.selectForProtocol(card.protocol)
					.returnIf {
						it.hasSolution()
					}?.let { SmartcardApplication(this, app, it) }
			}
	}

	override fun close(disposition: CardDisposition) {
		TODO("Not yet implemented")
	}

	internal fun setDidFulfilled(did: SmartcardDid<*>) {
		TODO("Not yet implemented")
	}
}
