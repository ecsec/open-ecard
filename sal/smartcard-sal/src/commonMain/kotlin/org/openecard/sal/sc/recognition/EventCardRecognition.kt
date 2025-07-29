package org.openecard.sal.sc.recognition

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.Terminals

class EventCardRecognition(
	val terminals: Terminals,
	val direct: DirectCardRecognition,
) : CardRecognition {
	// TODO: use cardInfo
	private val recognizedCards: MutableMap<String, Unit> = mutableMapOf()

	fun CoroutineScope.startRecognition(): Job =
		launch {
			TODO("implement")
		}

	override fun recognizeCard(channel: CardChannel): String? {
		recognizedCards[channel.card.terminalConnection.terminal.name]
		TODO("Not yet implemented")
	}
}
