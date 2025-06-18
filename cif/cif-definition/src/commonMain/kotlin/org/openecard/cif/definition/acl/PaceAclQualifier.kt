package org.openecard.cif.definition.acl

import kotlinx.serialization.Serializable
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
data class PaceAclQualifier(
	/**
	 * Contains the chat that needs to be authorized.
	 */
	val chat: PrintableUByteArray,
) : AclQualifier {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as PaceAclQualifier

		if (!chat.v.contentEquals(other.chat.v)) return false

		return true
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun hashCode(): Int = chat.v.contentHashCode()
}
