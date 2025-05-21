package org.openecard.sal.sc.recognition

import org.openecard.sc.iface.TerminalConnection

interface CardRecognition {
	// TODO: return CardInfo
	fun recognizeCard(connection: TerminalConnection): Unit
}
