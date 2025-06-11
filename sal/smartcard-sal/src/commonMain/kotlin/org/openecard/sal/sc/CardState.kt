package org.openecard.sal.sc

data class CardState(
	val app: SmartcardApplication?,
	val dataSet: SmartcardDataset?,
	val authenticatedDids: Set<SmartcardDid<*>>,
)
