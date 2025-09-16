package org.openecard.sc.pace.cvc

import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.TagClass
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.findPrimitive
import org.openecard.sc.tlv.toObjectIdentifier

class DiscretionaryDataTemplate
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val oid: ObjectIdentifier,
		val data: UByteArray,
	) {
		companion object {
			val DiscretionaryDataObjectTag = Tag(TagClass.APPLICATION, false, 19u)
			val DiscretionaryDataValueTag = Tag(TagClass.APPLICATION, true, 19u)

			@OptIn(ExperimentalUnsignedTypes::class)
			@Throws(TlvException::class, IllegalArgumentException::class, NoSuchElementException::class)
			fun Tlv.toDiscretionaryDataTemplate(): DiscretionaryDataTemplate {
				val tlv = requireNotNull(this.asConstructed) { "Given tag is not constructed" }
				require(tlv.tag == DiscretionaryDataObjectTag) { "Discretionary Data Template tag is not correct" }
				val content = tlv.childList()

				val oid =
					content.findPrimitive(Tag.OID_TAG)?.toObjectIdentifier()
						?: throw IllegalArgumentException("Description type is missing")
				val data =
					content.findPrimitive(DiscretionaryDataValueTag)?.value
						?: throw IllegalArgumentException("Data value is missing")

				return DiscretionaryDataTemplate(oid, data)
			}
		}
	}
