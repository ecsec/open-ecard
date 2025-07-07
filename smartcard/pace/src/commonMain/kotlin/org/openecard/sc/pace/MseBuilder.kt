package org.openecard.sc.pace

import org.openecard.sc.apdu.command.Mse
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.MseTags
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.sc.tlv.tlvCustom
import org.openecard.sc.tlv.toTlv
import org.openecard.sc.tlv.toTlvBer
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.serialization.toPrintable

@OptIn(ExperimentalUnsignedTypes::class)
fun paceMseSetAt(
	paceInfos: EfCardAccess.PaceInfos,
	pinId: PacePinId,
	chat: UByteArray?,
): Mse {
	val dos =
		buildList {
			add(paceInfos.info.protocol.tlvCustom(MseTags.cryptoMechanismReference))
			add(TlvPrimitive(MseTags.passwordReference, pinId.code.toUByteArray().toPrintable()))
			paceInfos.info.parameterId
				?.toTlv(MseTags.sessionKeyComputationReference)
				?.let { add(it) }
			chat?.toTlvBer()?.tlv?.let { add(it) }
		}

	val flags =
		Mse.p1FlagsAllUnset
			.setComputationDecipherIntAuthKeyAgree(true)
			.setVerifyEncipherExtAuthKeyAgree(true)
	return Mse.mseSet(flags, Mse.Tag.AT, dos)
}
