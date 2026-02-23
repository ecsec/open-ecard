package org.openecard.demo.util

import org.openecard.sc.pace.cvc.AuthenticationTerminalChat
import org.openecard.sc.pace.cvc.ReadAccess
import org.openecard.sc.pace.cvc.SpecialFunction

data class ChatAttributeUi(
	val id: String,
	val label: String,
	val required: Boolean,
	val selected: Boolean,
)

// convert server data to ui items
fun chatToUi(
	requiredChat: AuthenticationTerminalChat,
	optionalChat: AuthenticationTerminalChat,
): List<ChatAttributeUi> {
	val items = mutableListOf<ChatAttributeUi>()

	ReadAccess.entries.forEach { key ->
		val required = requiredChat.readAccess[key]
		val optional = optionalChat.readAccess[key]

		if (required || optional) {
			items +=
				ChatAttributeUi(
					id = key.name,
					label = key.toLabel(),
					required = required,
					selected = true,
				)
		}
	}

	SpecialFunction.entries.forEach { key ->
		val required = requiredChat.specialFunctions[key]
		val optional = optionalChat.specialFunctions[key]

		if (required || optional) {
			items +=
				ChatAttributeUi(
					id = key.name,
					label = key.toLabel(),
					required = required,
					selected = true,
				)
		}
	}

	return items
}

// convert user selection to chat
fun buildChatFromSelection(
	base: AuthenticationTerminalChat,
	selectedIds: List<String>,
): AuthenticationTerminalChat {
	val chat = base.copy()

	// set base attributes to false
	chat.readAccess.clear()
	chat.specialFunctions.clear()

	selectedIds.forEach { id ->
		ReadAccess.entries.firstOrNull { it.name == id }?.let {
			chat.readAccess[it] = true
		}
		SpecialFunction.entries.firstOrNull { it.name == id }?.let {
			chat.specialFunctions[it] = true
		}
	}

	return chat
}
