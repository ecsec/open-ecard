package org.openecard.sal.sc.recognition

import org.openecard.sc.iface.CardChannel

interface CardRecognition {
	// TODO: return CardInfo
	fun recognizeCard(channel: CardChannel): String?
}
