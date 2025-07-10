package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.PinDidDefinition
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PinCallback
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sc.iface.feature.PinStatus

class SmartcardPinDid(
	application: SmartcardApplication,
	didDef: PinDidDefinition,
	val authAcl: CifAclOr,
	val modifyAcl: CifAclOr,
) : SmartcardDid.BaseSmartcardDid<PinDidDefinition>(didDef, application),
	PinDid {
	override val missingAuthAuthentications: MissingAuthentications
		get() = missingAuthentications(authAcl)
	override val missingModifyAuthentications: MissingAuthentications
		get() = missingAuthentications(modifyAcl)

	override fun capturePinInHardware(): Boolean {
		TODO("Not yet implemented")
	}

	override fun pinStatus(): PinStatus {
		TODO("Not yet implemented")
	}

	override suspend fun verify(pinCallback: PinCallback) {
		TODO("Not yet implemented")
	}

	override suspend fun verify() {
		TODO("Not yet implemented")
	}

	override suspend fun modify(
		oldPinCallback: PinCallback,
		newPinCallback: PinCallback,
	) {
		TODO("Not yet implemented")
	}

	override suspend fun modify() {
		TODO("Not yet implemented")
	}
}
