package org.openecard.sc.pace.cvc

import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvConstructed

/**
 * Certificate Extensions for Terminal Authentication Version 2 according to TR-03110-3 Sec. C.3.
 */
class CvcExtensions(
	val dos: List<Tlv>,
) {
	companion object {
		@Throws(IllegalArgumentException::class)
		fun Tlv.toCvcExtensions(tag: Tag = CvcTags.certExtensions): CvcExtensions {
			require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
			return when (this) {
				is TlvConstructed -> {
					// TODO: evaluate child list
					CvcExtensions(this.childList())
				}
				else -> throw IllegalArgumentException("CVC Extensions TLV is not constructed")
			}
		}
	}
}
