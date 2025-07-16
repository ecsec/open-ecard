package org.openecard.sal.sc

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.cif.definition.did.PaceDidDefinition
import org.openecard.cif.definition.did.PinDidDefinition
import org.openecard.sal.iface.Application
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.Did
import org.openecard.sal.iface.dids.SecureChannelDid
import org.openecard.sal.iface.hasSolution
import org.openecard.sal.iface.selectForProtocol
import org.openecard.sal.sc.acl.missingAuthentications
import org.openecard.sal.sc.dids.SmartcardEncryptDid
import org.openecard.sal.sc.dids.SmartcardPaceDid
import org.openecard.sal.sc.dids.SmartcardPinDid
import org.openecard.sal.sc.dids.SmartcardSignDid
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.utils.common.hex

class SmartcardApplication(
	override val device: SmartcardDeviceConnection,
	val appDef: ApplicationDefinition,
	val selectAcl: CifAclOr,
) : Application {
	override val name: String = appDef.name

	internal val channel: CardChannel = device.channel

	override val datasets: List<SmartcardDataset> by lazy {
		appDef.dataSets.mapNotNull { ds ->
			val readAcl = ds.readAcl.selectForProtocol(device.channel.card.protocol)
			val writeAcl = ds.writeAcl.selectForProtocol(device.channel.card.protocol)
			if (readAcl.hasSolution() || writeAcl.hasSolution()) {
				SmartcardDataset(ds.name, this, ds, readAcl, writeAcl)
			} else {
				null
			}
		}
	}

	override val dids: List<Did> by lazy {
		appDef.dids.mapNotNull { did ->
			when (did) {
				is PinDidDefinition -> {
					val authAcl = did.authAcl.selectForProtocol(device.channel.card.protocol)
					val modifyAcl = did.modifyAcl.selectForProtocol(device.channel.card.protocol)
					if (authAcl.hasSolution() || modifyAcl.hasSolution()) {
						SmartcardPinDid(this, did, authAcl, modifyAcl)
					} else {
						null
					}
				}

				is PaceDidDefinition -> {
					val factory = device.session.sal.paceFactory
					val authAcl = did.authAcl.selectForProtocol(device.channel.card.protocol)
					val modifyAcl = did.modifyAcl.selectForProtocol(device.channel.card.protocol)
					if (factory != null && (authAcl.hasSolution() || modifyAcl.hasSolution())) {
						SmartcardPaceDid(this, did, authAcl, modifyAcl, factory)
					} else {
						null
					}
				}
				is GenericCryptoDidDefinition<*> -> {
					when (did) {
						is GenericCryptoDidDefinition.EncryptionDidDefinition -> {
							val encryptAcl = did.encipherAcl.selectForProtocol(device.channel.card.protocol)
							val decryptAcl = did.decipherAcl.selectForProtocol(device.channel.card.protocol)
							if (encryptAcl.hasSolution() || decryptAcl.hasSolution()) {
								SmartcardEncryptDid(this, did, encryptAcl, decryptAcl)
							} else {
								null
							}
						}
						is GenericCryptoDidDefinition.SignatureDidDefinition -> {
							val signAcl = did.signAcl.selectForProtocol(device.channel.card.protocol)
							if (signAcl.hasSolution()) {
								SmartcardSignDid(this, did, signAcl)
							} else {
								null
							}
						}
					}
				}
			}
		}
	}

	override val missingSelectAuthentications: MissingAuthentications
		get() = selectAcl.missingAuthentications(device)

	override val isConnected: Boolean
		get() = device.cardState.app == this

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun connect() {
		// remove secure channel before switching the application
		val unauthDids = device.cardState.authenticatedDids.filter { it.isLocal && it.application != this }
		unauthDids.filterIsInstance<SecureChannelDid>().forEach {
			// TODO: not sure if we should do some error handling here
			it.closeChannel()
		}

		val selectApdu =
			if (appDef.aid.v.contentEquals(hex("3F00"))) {
				Select.selectMf()
			} else {
				Select.selectDfName(appDef.aid.v)
			}
		// TODO: selection by path see ISO 7816-4 Sec. 8.2 and 8.3

		selectApdu.transmit(channel)

		// update state
		device.setSelectedApplication(this)
	}
}
