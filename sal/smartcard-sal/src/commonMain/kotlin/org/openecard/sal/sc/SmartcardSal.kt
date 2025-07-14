package org.openecard.sal.sc

import org.openecard.cif.definition.CardInfoDefinition
import org.openecard.sal.iface.Sal
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.Terminals
import org.openecard.sc.iface.feature.PaceFeatureFactory
import org.openecard.utils.common.generateSessionId
import kotlin.random.Random

class SmartcardSal(
	internal val terminals: Terminals,
	internal val cifs: Set<CardInfoDefinition>,
	internal val random: Random = Random.Default,
	val cardRecognition: CardRecognition,
	internal val paceFactory: PaceFeatureFactory? = null,
) : Sal {
	override fun startSession(sessionId: String?): SmartcardSalSession {
		val session = sessionId ?: random.generateSessionId()
		return SmartcardSalSession(this, session)
	}
}
