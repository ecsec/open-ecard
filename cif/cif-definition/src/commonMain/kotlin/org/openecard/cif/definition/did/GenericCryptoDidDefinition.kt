package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition

sealed interface GenericCryptoDidDefinition : DidDefinition {
	val parameters: GenericCryptoDidParameters

	class SignatureDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val signAcl: AclDefinition,
		override val parameters: SignatureDidParameters,
	) : GenericCryptoDidDefinition

	class EncryptionDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val encipherAcl: AclDefinition,
		override val parameters: EncryptionDidParameters,
	) : GenericCryptoDidDefinition

	class DecryptionDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val decipherAcl: AclDefinition,
		override val parameters: EncryptionDidParameters,
	) : GenericCryptoDidDefinition
}

sealed interface GenericCryptoDidParameters {
	val key: KeyRefDefinition
	val certificates: List<String>
}

@Serializable
data class KeyRefDefinition(
	val keyRef: UByte,
	val keySize: Int?,
	val nonceSize: Int?,
)
