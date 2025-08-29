package org.openecard.sc.pace.asn1

import org.openecard.sc.tlv.Tag

object GeneralAuthenticateResponseTags {
	val dynamicAuthenticationData = Tag.forTagNumWithClass(0x7Cu)
	val encryptedNonce = Tag.forTagNumWithClass(0x80u)
	val caNonce = Tag.forTagNumWithClass(0x81u)
	val mappingData = Tag.forTagNumWithClass(0x82u)
	val caAuthenticationToken = Tag.forTagNumWithClass(0x82u)
	val ephemeralPublicKey = Tag.forTagNumWithClass(0x84u)
	val paceAuthenticationToken = Tag.forTagNumWithClass(0x86u)
	val curCar = Tag.forTagNumWithClass(0x87u)
	val prevCar = Tag.forTagNumWithClass(0x88u)
}
