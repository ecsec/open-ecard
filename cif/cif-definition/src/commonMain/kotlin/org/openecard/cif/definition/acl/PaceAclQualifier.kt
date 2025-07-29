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
	override fun matches(other: AclQualifier): Boolean =
		when (other) {
			is PaceAclQualifier -> this.chat == other.chat
			else -> false
		}
}
