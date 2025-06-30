package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition

sealed interface GenericCryptoDidDefinition<T : GenericCryptoDidParameters> : DidDefinition {
	val parameters: T

	class SignatureDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val signAcl: AclDefinition,
		override val parameters: SignatureDidParameters,
	) : GenericCryptoDidDefinition<SignatureDidParameters>

	class EncryptionDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val encipherAcl: AclDefinition,
		override val parameters: EncryptionDidParameters,
	) : GenericCryptoDidDefinition<EncryptionDidParameters>

	class DecryptionDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val decipherAcl: AclDefinition,
		override val parameters: EncryptionDidParameters,
	) : GenericCryptoDidDefinition<EncryptionDidParameters>
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
