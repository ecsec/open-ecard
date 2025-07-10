package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.EncryptDid
import org.openecard.sal.sc.SmartcardApplication

class SmartcardEncryptDid(
	application: SmartcardApplication,
	didDef: GenericCryptoDidDefinition.EncryptionDidDefinition,
	val encipherAcl: CifAclOr,
) : SmartcardDid.BaseSmartcardDid<GenericCryptoDidDefinition.EncryptionDidDefinition>(didDef, application),
	EncryptDid {
	override val missingEncryptAuthentications: MissingAuthentications
		get() = missingAuthentications(encipherAcl)

	override fun encrypt(data: ByteArray): ByteArray {
		TODO("Not yet implemented")
	}
}
