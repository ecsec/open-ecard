package org.openecard.sc.apdu.command

import org.openecard.sc.tlv.Tag

object StandardTags {
	val discretionaryDataObjectInterIndustry = Tag.forTagNumWithClass(0x53u)
	val discretionaryDataObjectPropretiery = Tag.forTagNumWithClass(0x73u)
}
