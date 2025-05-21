package org.openecard.sal.sc

import org.openecard.sal.iface.Sal
import org.openecard.sc.iface.Terminals
import org.openecard.utils.common.generateSessionId
import kotlin.random.Random

class SmartcardSal(
	internal val terminals: Terminals,
	internal val random: Random = Random.Default,
) : Sal {
	override fun startSession(sessionId: String?): SmartcardSalSession {
		val session =
			sessionId ?: random.generateSessionId()
		return SmartcardSalSession(this, session)
	}
}
