package org.openecard.sc.pace.asn1

import org.openecard.sc.tlv.Tag

object GeneralAuthenticateCommandTags {
	val caEphemeralPublicKey = Tag.forTagNumWithClass(0x80u)
	val mappingData = Tag.forTagNumWithClass(0x81u)
	val paceEphemeralPublicKey = Tag.forTagNumWithClass(0x83u)
	val authenticationToken = Tag.forTagNumWithClass(0x85u)
}
