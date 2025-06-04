package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.SignDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardDid

class SmartcardSignDid(
	application: SmartcardApplication,
	didDef: GenericCryptoDidDefinition.SignatureDidDefinition,
	val signAcl: CifAclOr,
) : SmartcardDid<GenericCryptoDidDefinition.SignatureDidDefinition>(didDef, application),
	SignDid {
	override val missingSignAuthentications: MissingAuthentications
		get() = missingAuthentications("sign")

	override fun sign(data: ByteArray): ByteArray {
		TODO("Not yet implemented")
	}
}
