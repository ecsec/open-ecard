package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.checkStatus
import org.openecard.sc.iface.CardChannel
import org.openecard.utils.common.mergeToArray

@OptIn(ExperimentalUnsignedTypes::class)
fun ReadRecord.transmit(channel: CardChannel): UByteArray {
	val response = channel.transmit(this.apdu)
	response.checkStatus(StatusWord.OK, StatusWord.EOF_REACHED_BEFORE_READING_NE)
	return response.data
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Sequence<ReadRecord>.transmit(channel: CardChannel): UByteArray {
	val commands = this
	return buildList {
		for (command in commands) {
			try {
				val resp = command.transmit(channel)
				add(resp)
			} catch (ex: ApduProcessingError) {
				if (ex.status.type in listOf(StatusWord.RECORD_NOT_FOUND)) {
					break
				} else {
					throw ex
				}
			}
		}
	}.mergeToArray()
}
