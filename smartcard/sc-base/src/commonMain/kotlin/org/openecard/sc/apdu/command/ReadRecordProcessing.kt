package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.checkStatus
import org.openecard.sc.iface.CardChannel

@OptIn(ExperimentalUnsignedTypes::class)
fun ReadRecord.transmit(channel: CardChannel): UByteArray {
	val response = channel.transmit(this.apdu)
	response.checkStatus(StatusWord.OK, StatusWord.EOF_REACHED_BEFORE_READING_NE)
	return response.data
}
