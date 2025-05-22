package org.openecard.sal.sc.dids

import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.SignDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardDid

class SmartcardSignDid(
	name: String,
	application: SmartcardApplication,
	isLocal: Boolean,
) : SmartcardDid(name, isLocal, application),
	SignDid {
	override val missingSignAuthentications: MissingAuthentications
		get() = missingAuthentications("sign")

	override fun sign(data: ByteArray): ByteArray {
		TODO("Not yet implemented")
	}
}
