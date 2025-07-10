package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.DecryptDid
import org.openecard.sal.sc.SmartcardApplication

class SmartcardDecryptDid(
	application: SmartcardApplication,
	didDef: GenericCryptoDidDefinition.DecryptionDidDefinition,
	val decipherAcl: CifAclOr,
) : SmartcardDid.BaseSmartcardDid<GenericCryptoDidDefinition.DecryptionDidDefinition>(didDef, application),
	DecryptDid {
	override val missingDecryptAuthentications: MissingAuthentications
		get() = missingAuthentications(decipherAcl)

	override fun decrypt(data: ByteArray): ByteArray {
		TODO("Not yet implemented")
	}
}
