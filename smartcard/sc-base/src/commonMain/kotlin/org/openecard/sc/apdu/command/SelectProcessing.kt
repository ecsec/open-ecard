package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.checkOk
import org.openecard.sc.iface.CardChannel

fun Select.transmit(channel: CardChannel): FileInfo? {
	// val capabilities = channel.capabilities
	val response = channel.transmit(this.apdu)
	response.checkOk()

	// TODO("Implement file info retrieval")
	return null
}

// ISO 7816-4, Sec.5.3.1.1
// If supported, selection by short EF identifier shall be indicated.
//
// - If the first software function table (see Table 86) is present in the historical bytes (see 8.1.1) or in EF.ATR
//   (see 8.2.1.1), then the indication is valid at card level.
//
// - If a short EF identifier (tag '88', see Table 12) is present in the control parameters (see 5.3.3) of an EF,
//   then the indication is valid at EF level.
