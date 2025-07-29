package org.openecard.sc.iface.info

import org.openecard.sc.tlv.Tag

object StandardTags {
	val discretionaryDataObjectInterIndustry = Tag.forTagNumWithClass(0x53u)
	val discretionaryDataObjectPropretiery = Tag.forTagNumWithClass(0x73u)

	val applicationTemplate = Tag.forTagNumWithClass(0x61u)
	val applicationIdentifier = Tag.forTagNumWithClass(0x4Fu)
	val applicationLabel = Tag.forTagNumWithClass(0x50u)
	val fileReference = Tag.forTagNumWithClass(0x51u)
	val commandApdu = Tag.forTagNumWithClass(0x52u)
	val url = Tag.forTagNumWithClass(0x5F50u)

	val fcp = Tag.forTagNumWithClass(0x62u)
	val fci = Tag.forTagNumWithClass(0x6Fu)
	val fmd = Tag.forTagNumWithClass(0x64u)
}
