package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.DecryptDid
import org.openecard.sal.iface.dids.EncryptDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.mapSmartcardError

class SmartcardEncryptDid(
	application: SmartcardApplication,
	didDef: GenericCryptoDidDefinition.EncryptionDidDefinition,
	val encipherAcl: CifAclOr,
	val decipherAcl: CifAclOr,
) : SmartcardDid.BaseSmartcardDid<GenericCryptoDidDefinition.EncryptionDidDefinition>(didDef, application),
	EncryptDid,
	DecryptDid {
	override val missingEncryptAuthentications: MissingAuthentications
		get() = missingAuthentications(encipherAcl)

	override val missingDecryptAuthentications: MissingAuthentications
		get() = missingAuthentications(decipherAcl)

	override fun encrypt(data: ByteArray): ByteArray =
		mapSmartcardError {
			TODO("Not yet implemented")
		}

	override fun decrypt(data: ByteArray): ByteArray =
		mapSmartcardError {
			TODO("Not yet implemented")
		}
}
