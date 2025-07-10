package org.openecard.sal.sc

import org.openecard.sal.sc.dids.SmartcardDid

data class CardState(
	val app: SmartcardApplication?,
	val dataSet: SmartcardDataset?,
	val authenticatedDids: Set<SmartcardDid<*>>,
)
