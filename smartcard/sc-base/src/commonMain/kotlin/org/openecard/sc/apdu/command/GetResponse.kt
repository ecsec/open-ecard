package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu

class GetResponse(
	le: UShort,
) : IsoCommandApdu {
	override val apdu: CommandApdu by lazy {
		CommandApdu(0x00u, 0xC0u, 0x00u, 0x00u, le = le)
	}
}
