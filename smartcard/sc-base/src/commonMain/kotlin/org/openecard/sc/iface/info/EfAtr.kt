package org.openecard.sc.iface.info

import org.openecard.sc.iface.HistoricalBytes
import org.openecard.sc.iface.toHistoricalBytes
import org.openecard.sc.tlv.Tlv

class EfAtr(
	private val dos: List<Tlv>,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	val historicalBytes: HistoricalBytes? by lazy {
		dos.find { it.tag.tagNumWithClass == 0x5F52uL }?.contentAsBytesBer?.toHistoricalBytes()
	}
}
