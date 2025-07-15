package org.openecard.sc.pace.asn1

import org.openecard.sc.tlv.Tag

object MseTags {
	val cryptoMechanismReference = Tag.forTagNumWithClass(0x80u)
	val passwordReference = Tag.forTagNumWithClass(0x83u)
	val sessionKeyComputationReference = Tag.forTagNumWithClass(0x84u)
	val chat = Tag.forTagNumWithClass(0x7F4Cu)
	val certExtensions = Tag.forTagNumWithClass(0x65u)
}
