package org.openecard.sc.pace.cvc

import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.utils.serialization.PrintableUByteArray

interface CvcExtension {
	val oid: ObjectIdentifier

	data class CertificateDescriptionReference(
		override val oid: ObjectIdentifier,
		val certDescriptionHash: PrintableUByteArray,
	) : CvcExtension
}
