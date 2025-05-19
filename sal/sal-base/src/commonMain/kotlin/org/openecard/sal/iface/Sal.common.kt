package org.openecard.sal.iface

interface Sal {
	fun startSession(sessionId: String? = null): SalSession
}
