package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.SignDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.mapSmartcardError
import org.openecard.utils.common.throwIf

class SmartcardSignDid(
	application: SmartcardApplication,
	didDef: GenericCryptoDidDefinition.SignatureDidDefinition,
	val signAcl: CifAclOr,
) : SmartcardDid.BaseSmartcardDid<GenericCryptoDidDefinition.SignatureDidDefinition>(didDef, application),
	SignDid {
	override val missingSignAuthentications: MissingAuthentications
		get() = missingAuthentications(signAcl)

	override fun sign(data: ByteArray): ByteArray =
		mapSmartcardError {
			throwIf(!missingSignAuthentications.isSolved) { MissingAuthentication("Sign ACL is not satisfied") }

			TODO("Not yet implemented")
		}
}
