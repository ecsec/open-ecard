package org.openecard.sal.sc

typealias SalEventHandler = (SalEvent) -> Unit

class SalEvent(
	val initiatorType: String,
	val initiatorName: String,
	val operation: String,
)
