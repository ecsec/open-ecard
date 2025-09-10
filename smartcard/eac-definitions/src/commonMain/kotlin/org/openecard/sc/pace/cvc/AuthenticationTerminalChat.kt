package org.openecard.sc.pace.cvc

import org.openecard.sc.pace.cvc.Chat.Companion.roleType
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.utils.common.BitSet
import kotlin.enums.enumEntries

internal fun roleForAuthorizationType(
	terminalType: ObjectIdentifier,
	accessRights: BitSet,
) = roleType(terminalType, accessRights, 39 downTo 38)

class AuthenticationTerminalChat(
	terminalType: ObjectIdentifier,
	private val accessRights: BitSet,
) : Chat<AuthenticationTerminalChat>(terminalType, accessRights) {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val role: Role by lazy { roleForAuthorizationType(terminalType, accessRights) }

	val writeAccess: RightsBitSet<WriteAccess> by lazy {
		RightsBitSet(accessRights, enumEntries())
	}
	val readAccess: RightsBitSet<ReadAccess> by lazy {
		RightsBitSet(accessRights, enumEntries())
	}
	val specialFunctions: RightsBitSet<SpecialFunction> by lazy {
		RightsBitSet(accessRights, enumEntries())
	}

	override fun copy(): AuthenticationTerminalChat = AuthenticationTerminalChat(terminalType, accessRights.copy())
}
