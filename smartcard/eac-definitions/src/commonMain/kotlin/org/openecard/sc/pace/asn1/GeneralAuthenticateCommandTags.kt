package org.openecard.sc.pace.asn1

import org.openecard.sc.tlv.Tag

object GeneralAuthenticateCommandTags {
	val mappingData = Tag.forTagNumWithClass(0x81u)
	val ephemeralPublicKey = Tag.forTagNumWithClass(0x83u)
	val authenticationToken = Tag.forTagNumWithClass(0x85u)
}
