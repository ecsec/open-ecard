package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.PaceDidDefinition
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinCallback
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardDid
import org.openecard.sal.sc.dids.toSalType
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PacePinId

class SmartcardPaceDid(
	application: SmartcardApplication,
	didDef: PaceDidDefinition,
	val authAcl: CifAclOr,
	val modifyAcl: CifAclOr,
) : SmartcardDid<PaceDidDefinition>(didDef, application),
	PaceDid {
	override val pinType: PacePinId = did.parameters.passwordRef.toSalType()

	override val missingAuthAuthentications: MissingAuthentications
		get() = missingAuthentications("auth")

	override fun capturePinInHardware(): Boolean {
		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun establishChannel(
		pinCallback: PinCallback,
		chat: UByteArray?,
	): PaceEstablishChannelResponse {
		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun establishChannel(chat: UByteArray?): PaceEstablishChannelResponse {
		TODO("Not yet implemented")
	}

	override fun closeChannel() {
		TODO("Not yet implemented")
	}
}

internal fun org.openecard.cif.definition.did.PacePinId.toSalType() =
	when (this) {
		org.openecard.cif.definition.did.PacePinId.MRZ -> PacePinId.MRZ
		org.openecard.cif.definition.did.PacePinId.CAN -> PacePinId.CAN
		org.openecard.cif.definition.did.PacePinId.PIN -> PacePinId.PIN
		org.openecard.cif.definition.did.PacePinId.PUK -> PacePinId.PUK
	}
