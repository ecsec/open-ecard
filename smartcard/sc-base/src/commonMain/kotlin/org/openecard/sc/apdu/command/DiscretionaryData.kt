package org.openecard.sc.apdu.command

import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.serialization.toPrintable

@OptIn(ExperimentalUnsignedTypes::class)
internal fun makeDataObject(
	content: Tlv,
	proprietaryDataObject: Boolean = false,
): UByteArray {
	val tag =
		if (proprietaryDataObject) {
			StandardTags.discretionaryDataObjectPropretiery
		} else {
			StandardTags.discretionaryDataObjectInterIndustry
		}
	return TlvPrimitive(tag, content.toBer(true).toPrintable()).toBer()
}
