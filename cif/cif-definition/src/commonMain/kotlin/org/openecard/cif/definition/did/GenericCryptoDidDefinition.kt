package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition

@Serializable
sealed interface GenericCryptoDidDefinition<T : GenericCryptoDidParameters> : DidDefinition {
	val parameters: T

	@Serializable
	data class SignatureDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val signAcl: AclDefinition,
		override val parameters: SignatureDidParameters,
	) : GenericCryptoDidDefinition<SignatureDidParameters>

	@Serializable
	data class EncryptionDidDefinition(
		override val name: String,
		override val scope: DidScope,
		val encipherAcl: AclDefinition,
		val decipherAcl: AclDefinition,
		override val parameters: EncryptionDidParameters,
	) : GenericCryptoDidDefinition<EncryptionDidParameters>
}

@Serializable
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
