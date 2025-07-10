package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.SignDid
import org.openecard.sal.sc.SmartcardApplication

class SmartcardSignDid(
	application: SmartcardApplication,
	didDef: GenericCryptoDidDefinition.SignatureDidDefinition,
	val signAcl: CifAclOr,
) : SmartcardDid.BaseSmartcardDid<GenericCryptoDidDefinition.SignatureDidDefinition>(didDef, application),
	SignDid {
	override val missingSignAuthentications: MissingAuthentications
		get() = missingAuthentications(signAcl)

	override fun sign(data: ByteArray): ByteArray {
		TODO("Not yet implemented")
	}
}
