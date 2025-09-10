package org.openecard.sc.pace.cvc

import org.openecard.sc.tlv.Tag

object CvcTags {
	val cvc = Tag.forTagNumWithClass(0x7F21u)
	val body = Tag.forTagNumWithClass(0x7F4Eu)
	val signature = Tag.forTagNumWithClass(0x5F37u)

	val certificateAuthorityReference = Tag.forTagNumWithClass(0x42u)
	val certificateHolderReference = Tag.forTagNumWithClass(0x5F20u)
	val expirationDate = Tag.forTagNumWithClass(0x5F24u)
	val generationDate = Tag.forTagNumWithClass(0x5F25u)
	val profileIdentifier = Tag.forTagNumWithClass(0x5F29u)
	val certExtensions = Tag.forTagNumWithClass(0x65u)
	val authentication = Tag.forTagNumWithClass(0x67u)
	val publicKey = Tag.forTagNumWithClass(0x7F49u)
	val chat = Tag.forTagNumWithClass(0x7F4Cu)
}
