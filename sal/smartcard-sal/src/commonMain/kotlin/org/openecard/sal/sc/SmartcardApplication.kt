package org.openecard.sal.sc

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.cif.definition.did.PaceDidDefinition
import org.openecard.cif.definition.did.PinDidDefinition
import org.openecard.sal.iface.Application
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.Did
import org.openecard.sal.sc.acl.hasSolution
import org.openecard.sal.sc.acl.selectForProtocol
import org.openecard.sal.sc.dids.SmartcardPaceDid
import org.openecard.sal.sc.dids.SmartcardPinDid
import org.openecard.sal.sc.dids.SmartcardSignDid

class SmartcardApplication(
	override val device: SmartcardDeviceConnection,
	val appDef: ApplicationDefinition,
	val selectAcl: CifAclOr,
) : Application {
	override val name: String = appDef.name
	override val datasets: List<SmartcardDataset> by lazy {
		appDef.dataSets.mapNotNull { ds ->
			val readAcl = ds.readAcl.selectForProtocol(device.card.protocol)
			val writeAcl = ds.writeAcl.selectForProtocol(device.card.protocol)
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
					val authAcl = did.authAcl.selectForProtocol(device.card.protocol)
					val modifyAcl = did.modifyAcl.selectForProtocol(device.card.protocol)
					if (authAcl.hasSolution() || modifyAcl.hasSolution()) {
						SmartcardPinDid(this, did, authAcl, modifyAcl)
					} else {
						null
					}
				}

				is PaceDidDefinition -> {
					val authAcl = did.authAcl.selectForProtocol(device.card.protocol)
					val modifyAcl = did.modifyAcl.selectForProtocol(device.card.protocol)
					if (authAcl.hasSolution() || modifyAcl.hasSolution()) {
						SmartcardPaceDid(this, did, authAcl, modifyAcl)
					} else {
						null
					}
				}
				is GenericCryptoDidDefinition -> {
					when (did) {
						is GenericCryptoDidDefinition.DecryptionDidDefinition -> TODO("No Implemented yet")
						is GenericCryptoDidDefinition.EncryptionDidDefinition -> TODO("No Implemented yet")
						is GenericCryptoDidDefinition.SignatureDidDefinition -> {
							val signAcl = did.signAcl.selectForProtocol(device.card.protocol)
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
		get() = TODO("Not yet implemented")

	override val isConnected: Boolean
		get() = TODO("Not yet implemented")

	override fun connect() {
		TODO("Not yet implemented")
	}
}
