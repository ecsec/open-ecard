package org.openecard.sal.sc

import org.openecard.cif.definition.CardInfoDefinition
import org.openecard.sal.iface.DeviceConnection
import org.openecard.sal.iface.dids.AuthenticationDid
import org.openecard.sal.iface.hasSolution
import org.openecard.sal.iface.selectForProtocol
import org.openecard.sal.sc.dids.SmartcardDid
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.CardDisposition
import org.openecard.utils.common.returnIf

class SmartcardDeviceConnection(
	override val connectionId: String,
	override val session: SmartcardSalSession,
	val channel: CardChannel,
	val cif: CardInfoDefinition,
) : DeviceConnection {
	val cardType: String = cif.metadata.id

	private var _cardState: CardState = CardState(null, null, setOf())
	val cardState: CardState
		get() = _cardState

	override val authenticatedDids: List<AuthenticationDid> get() {
		return cardState.authenticatedDids.filterIsInstance<AuthenticationDid>()
	}

	override val applications: List<SmartcardApplication> by lazy {
		cif.applications
			.mapNotNull { app ->
				app.selectAcl
					.selectForProtocol(channel.card.protocol)
					.returnIf {
						it.hasSolution()
					}?.let { SmartcardApplication(this, app, it) }
			}
	}

	internal val allAuthDids: List<AuthenticationDid> get() {
		return applications.flatMap { it.dids.filterIsInstance<AuthenticationDid>() }
	}

	internal fun findAuthDid(name: String): AuthenticationDid? = allAuthDids.find { it.name == name }

	override fun close(disposition: CardDisposition) {
		channel.card.terminalConnection.disconnect(disposition)
	}

	internal fun setSelectedApplication(application: SmartcardApplication) {
		// remove all local dids
		val unauthDids = _cardState.authenticatedDids.filter { it.isLocal && it.application != application }
		val remainingDids = _cardState.authenticatedDids - unauthDids

		_cardState = _cardState.copy(app = application, dataSet = null, authenticatedDids = remainingDids)
	}

	internal fun setSelectedDataset(dataset: SmartcardDataset?) {
		_cardState = _cardState.copy(dataSet = dataset)
	}

	internal fun isSelectedDataset(dataset: SmartcardDataset): Boolean = _cardState.dataSet?.name == dataset.name

	internal fun setDidFulfilled(did: SmartcardDid<*>) {
		_cardState = _cardState.copy(authenticatedDids = _cardState.authenticatedDids + did)
	}

	internal fun setDidUnfulfilled(did: SmartcardDid<*>) {
		_cardState = _cardState.copy(authenticatedDids = _cardState.authenticatedDids - did)
	}
}
