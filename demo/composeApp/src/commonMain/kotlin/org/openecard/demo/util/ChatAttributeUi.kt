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
fun AuthenticationTerminalChat.toUiItem(): List<ChatAttributeUi> {
	val items = mutableListOf<ChatAttributeUi>()

	readAccess.toMap().forEach { (dg, required) ->
		items +=
			ChatAttributeUi(
				id = dg.name,
				label = dg.toLabel(),
				required = required,
				selected = required,
			)
	}
	specialFunctions.toMap().forEach { (sf, selected) ->
		items +=
			ChatAttributeUi(
				id = sf.name,
				label = sf.toLabel(),
				required = selected,
				selected = selected,
			)
	}

	return items
}

// convert user selection to chat
fun buildChatFromSelection(
	base: AuthenticationTerminalChat,
	selectedIds: List<String>,
): AuthenticationTerminalChat {
	val chat = base.copy()

	// set base attributes false
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
